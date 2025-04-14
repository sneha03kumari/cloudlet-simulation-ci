package org.cloudbus.cloudsim.examples;

//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Random;
//
//import org.cloudbus.cloudsim.Cloudlet;
//import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
//import org.cloudbus.cloudsim.Datacenter;
//import org.cloudbus.cloudsim.DatacenterBroker;
//import org.cloudbus.cloudsim.DatacenterCharacteristics;
//import org.cloudbus.cloudsim.Host;
//import org.cloudbus.cloudsim.Log;
//import org.cloudbus.cloudsim.Pe;
//import org.cloudbus.cloudsim.Storage;
//import org.cloudbus.cloudsim.UtilizationModel;
//import org.cloudbus.cloudsim.UtilizationModelFull;
//import org.cloudbus.cloudsim.Vm;
//import org.cloudbus.cloudsim.VmAllocationPolicySimple;
//import org.cloudbus.cloudsim.VmSchedulerTimeShared;
//import org.cloudbus.cloudsim.core.CloudSim;
//import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
//import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
//import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
//
//public class CloudSimExample11 {
//
//    private static List<Cloudlet> cloudletList;
//    private static List<Vm> vmlist;
//    private static DatacenterBroker broker;
//
//    public static void main(String[] args) {
//
//        Log.printLine("Starting CloudSimExample11...");
//
//        try {
//            int num_user = 1;
//            Calendar calendar = Calendar.getInstance();
//            boolean trace_flag = false;
//
//            CloudSim.init(num_user, calendar, trace_flag);
//            Datacenter datacenter0 = createDatacenter("Datacenter_0");
//
//            broker = createBroker();
//            int brokerId = broker.getId();
//
//            vmlist = new ArrayList<Vm>();
//
//            int vmid = 0;
//            int mips = 1000;
//            long size = 10000;
//            int ram = 512;
//            long bw = 1000;
//            int pesNumber = 1;
//            String vmm = "Xen";
//
//            Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
//
//            vmlist.add(vm);
//            broker.submitVmList(vmlist);
//
//            cloudletList = new ArrayList<Cloudlet>();
//
//            int id = 0;
//            long length = 400000;
//            long fileSize = 300;
//            long outputSize = 300;
//            UtilizationModel utilizationModel = new UtilizationModelFull();
//
//            Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
//            cloudlet.setUserId(brokerId);
//            cloudlet.setVmId(vmid);
//
//            cloudletList.add(cloudlet);
//            broker.submitCloudletList(cloudletList);
//
//            CloudSim.startSimulation();
//
//            Random rand = new Random();
//            int failureTime = rand.nextInt(50) + 50;
//            Log.printLine("Simulating VM failure at time: " + failureTime);
//
//            for (int currentTime = 0; currentTime < failureTime; currentTime++) {
//                if (currentTime == failureTime) {
//                    Vm failedVm = vmlist.get(0);
//                    monitorAndRecoverVM(failedVm);
//                }
//            }
//
//            CloudSim.stopSimulation();
//
//            List<Cloudlet> newList = broker.getCloudletReceivedList();
//            printCloudletList(newList);
//
//            Log.printLine("CloudSimExample11 finished!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.printLine("Unwanted errors happen");
//        }
//    }
//
//    private static Datacenter createDatacenter(String name) {
//        List<Host> hostList = new ArrayList<Host>();
//        List<Pe> peList = new ArrayList<Pe>();
//        int mips = 1000;
//        peList.add(new Pe(0, new PeProvisionerSimple(mips)));
//
//        int hostId = 0;
//        int ram = 2048;
//        long storage = 1000000;
//        int bw = 10000;
//
//        hostList.add(new Host(
//                hostId,
//                new RamProvisionerSimple(ram),
//                new BwProvisionerSimple(bw),
//                storage,
//                peList,
//                new VmSchedulerTimeShared(peList)
//        ));
//
//        String arch = "x86";
//        String os = "Linux";
//        String vmm = "Xen";
//        double time_zone = 10.0;
//        double cost = 3.0;
//        double costPerMem = 0.05;
//        double costPerStorage = 0.001;
//        double costPerBw = 0.0;
//        LinkedList<Storage> storageList = new LinkedList<Storage>();
//
//        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
//                arch, os, vmm, hostList, time_zone, cost, costPerMem,
//                costPerStorage, costPerBw);
//
//        VmAllocationPolicySimple vmAllocationPolicy = new VmAllocationPolicySimple(hostList);
//
//        Datacenter datacenter = null;
//        try {
//            datacenter = new Datacenter(name, characteristics, vmAllocationPolicy, storageList, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return datacenter;
//    }
//
//    private static DatacenterBroker createBroker() {
//        DatacenterBroker broker = null;
//        try {
//            broker = new DatacenterBroker("Broker");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return broker;
//    }
//
//    private static void monitorAndRecoverVM(Vm failedVm) {
//        if (failedVm.getStatus() == Vm.Status.RUNNING) {
//            failedVm.setStatus(Vm.Status.IDLE); // Simulate failure
//            Log.printLine("VM " + failedVm.getId() + " has failed and is now IDLE.");
//            failedVm.setStatus(Vm.Status.RUNNING); // Simulate recovery
//            Log.printLine("VM " + failedVm.getId() + " is being recovered and re-enabled.");
//        }
//    }
//
//    private static void printCloudletList(List<Cloudlet> list) {
//        int size = list.size();
//        Cloudlet cloudlet;
//        String indent = "    ";
//        Log.printLine();
//        Log.printLine("========== OUTPUT ==========");
//        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
//                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
//                + "Start Time" + indent + "Finish Time");
//
//        DecimalFormat dft = new DecimalFormat("###.##");
//        for (int i = 0; i < size; i++) {
//            cloudlet = list.get(i);
//            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
//
//            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
//                Log.print("SUCCESS");
//
//                Log.printLine(indent + indent + cloudlet.getResourceId()
//                        + indent + indent + indent + cloudlet.getVmId()
//                        + indent + indent
//                        + dft.format(cloudlet.getActualCPUTime()) + indent
//                        + indent + dft.format(cloudlet.getExecStartTime())
//                        + indent + indent
//                        + dft.format(cloudlet.getFinishTime()));
//            }
//        }
//    }
//}




import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.*;

import java.text.DecimalFormat;
import java.util.*;

/**
 * CloudSimExample11 - VM failure + auto-recovery (rescheduling cloudlets)
 */
public class CloudSimExample11 {

    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;
    private static DatacenterBroker broker;

    public static void main(String[] args) {
        Log.printLine("Starting CloudSimExample11...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            broker = createBroker();
            int brokerId = broker.getId();

            vmList = createVM(brokerId, 5, 0);
            cloudletList = createCloudlet(brokerId, 10, 0);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            CloudSim.addEntity(new VMFailureInjector("FailureInjector", vmList.get(2), 100.0));

            CloudSim.startSimulation();

            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("CloudSimExample11 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();

        int mips = 1000;
        List<Pe> peList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        int ram = 16384;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(0,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
    }

    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("Broker");
    }

    private static List<Vm> createVM(int userId, int vms, int idShift) {
        List<Vm> list = new ArrayList<>();

        long size = 10000;
        int ram = 512;
        int mips = 250;
        long bw = 1000;
        int pesNumber = 1;
        String vmm = "Xen";

        for (int i = 0; i < vms; i++) {
            Vm vm = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            list.add(vm);
        }

        return list;
    }

    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
        List<Cloudlet> list = new ArrayList<>();

        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
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
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.printLine("\n========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" +
                indent + "Start Time" + indent + "Finish Time");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    // VM Failure Injector Class
    private static class VMFailureInjector extends SimEntity {
        private Vm vmToFail;
        private double failTime;

        public VMFailureInjector(String name, Vm vmToFail, double failTime) {
            super(name);
            this.vmToFail = vmToFail;
            this.failTime = failTime;
        }

        @Override
        public void startEntity() {
            Log.printLine(getName() + " started.");
            schedule(getId(), failTime, 0);
        }

        @Override
        public void processEvent(SimEvent ev) {
            Log.printLine(CloudSim.clock() + ": " + getName() + " injecting failure into VM #" + vmToFail.getId());
            List<Cloudlet> toReschedule = new ArrayList<>();

            for (Cloudlet c : broker.getCloudletSubmittedList()) {
                if (c.getVmId() == vmToFail.getId()) {
                    toReschedule.add(c);
                }
            }

            vmList.remove(vmToFail);
            broker.getVmList().remove(vmToFail);

            // Reschedule cloudlets
            for (Cloudlet c : toReschedule) {
                int newVmId = findAlternativeVm(c.getVmId());
                if (newVmId != -1) {
                    c.setVmId(newVmId);
                    broker.submitCloudletList(Collections.singletonList(c));
                    Log.printLine("Rescheduled Cloudlet " + c.getCloudletId() + " to VM #" + newVmId);
                } else {
                    Log.printLine("No VM available to reschedule Cloudlet " + c.getCloudletId());
                }
            }
        }

        private int findAlternativeVm(int failedVmId) {
            for (Vm vm : vmList) {
                if (vm.getId() != failedVmId) {
                    return vm.getId();
                }
            }
            return -1;
        }

        @Override
        public void shutdownEntity() {
            Log.printLine(getName() + " shutting down.");
        }
    }
}
