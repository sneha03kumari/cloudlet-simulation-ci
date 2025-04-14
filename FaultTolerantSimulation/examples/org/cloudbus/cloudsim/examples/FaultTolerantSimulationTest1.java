package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.*;

public class FaultTolerantSimulationTest1 {

    private static List<Vm> vmlist;
    private static List<Cloudlet> cloudletList;
    private static final Logger logger = Logger.getLogger(FaultTolerantSimulationTest1.class.getName());

    private static final int CLOUDLET_SUBMIT = 21;
    private static final int VM_FAILURE = 999;

    public static void main(String[] args) {
        Log.printLine("Starting Fault-Tolerant FaultTolerantSimulationTest1...");

        try {
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            Log.setDisabled(false);
            logger.setLevel(Level.INFO);

            CloudSim.init(numUser, calendar, traceFlag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            DatacenterBroker broker = createBroker("Broker_0");
            int brokerId = broker.getId();

            vmlist = createVM(brokerId, 5, 0);
            cloudletList = createCloudlet(brokerId, 10, 0);

            broker.submitVmList(vmlist);
            broker.submitCloudletList(cloudletList);

            CloudSim.addEntity(new VmFailureInjector("VmFailureInjector", vmlist, cloudletList, broker));

            CloudSim.startSimulation();

            List<Cloudlet> receivedList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletList(receivedList);
            Log.printLine("Fault-Tolerant FaultTolerantSimulationTest1 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Simulation terminated due to error.");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        int mips = 1000;
        for (int i = 0; i < 4; i++) peList.add(new Pe(i, new PeProvisionerSimple(mips)));

        int ram = 16384;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(0, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));

        String arch = "x86", os = "Linux", vmm = "Xen";
        double timeZone = 10.0, cost = 3.0, costPerMem = 0.05, costPerStorage = 0.1, costPerBw = 0.1;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) throws Exception {
        return new DatacenterBroker(name);
    }

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        List<Vm> list = new ArrayList<>();
        long size = 10000;
        int ram = 512, mips = 250, pesNumber = 1;
        long bw = 1000;
        String vmm = "Xen";
        for (int i = 0; i < vms; i++) {
            list.add(new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared()));
        }
        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
        List<Cloudlet> list = new ArrayList<>();
        long length = 40000, fileSize = 300, outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for (int i = 0; i < cloudlets; i++) {
            Cloudlet cloudlet = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            list.add(cloudlet);
        }
        return list;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        String indent = "    ";
        DecimalFormat dft = new DecimalFormat("###.##");
        Log.printLine("\n========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Datacenter ID" + indent + "VM ID" + indent +
                "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent);
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + cloudlet.getResourceId() + indent + cloudlet.getVmId() +
                        indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    private static class VmFailureInjector extends SimEntity {

        private final List<Vm> vms;
        private final List<Cloudlet> cloudlets;
        private final DatacenterBroker broker;

        public VmFailureInjector(String name, List<Vm> vms, List<Cloudlet> cloudlets, DatacenterBroker broker) {
            super(name);
            this.vms = vms;
            this.cloudlets = cloudlets;
            this.broker = broker;
        }

        @Override
        public void startEntity() {
            int failureTime = 100;
            for (Vm vm : vms) {
                if (vm.getId() % 2 == 0) { // Simulate failure for even ID VMs
                    send(getId(), failureTime, VM_FAILURE, vm);
                    failureTime += 50;
                }
            }
        }

        @Override
        public void processEvent(SimEvent ev) {
            switch (ev.getTag()) {
                case VM_FAILURE:
                    Vm failedVm = (Vm) ev.getData();
                    Log.printLine("VM Failure injected at time " + CloudSim.clock() + " for VM ID " + failedVm.getId());
                    logger.info("VM ID " + failedVm.getId() + " marked as failed.");

                    // Reschedule cloudlets
                    List<Cloudlet> toReschedule = new ArrayList<>();
                    for (Cloudlet c : cloudlets) {
                        if (c.getVmId() == failedVm.getId() && c.getCloudletStatus() != Cloudlet.SUCCESS) {
                            Cloudlet newC = new Cloudlet(c.getCloudletId() + 1000, c.getCloudletLength(),
                                    c.getNumberOfPes(), c.getCloudletFileSize(), c.getCloudletOutputSize(),
                                    c.getUtilizationModelCpu(), c.getUtilizationModelRam(), c.getUtilizationModelBw());
                            newC.setUserId(broker.getId());
                            toReschedule.add(newC);
                            logger.info("Rescheduling cloudlet " + c.getCloudletId() + " -> " + newC.getCloudletId());
                        }
                    }
                    broker.submitCloudletList(toReschedule);
                    break;

                default:
                    break;
            }
        }

        @Override
        public void shutdownEntity() {
            Log.printLine(getName() + " shutting down.");
        }
    }
}
