//package org.cloudbus.cloudsim.examples;
//
//import org.cloudbus.cloudsim.*;
//import org.cloudbus.cloudsim.core.CloudSim;
//import org.cloudbus.cloudsim.core.SimEvent;
//import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
//import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
//import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
//
//import java.text.DecimalFormat;
//import java.util.*;
//
//public class FaultTolerantSimulationTest {
//
//    private static List<Vm> vmList;
//    private static List<Cloudlet> cloudletList;
//    private static FaultTolerantBroker broker;
//
//    public static void main(String[] args) {
//        Log.printLine("Starting CloudSimExample11...");
//
//        try {
//            int num_user = 1;
//            Calendar calendar = Calendar.getInstance();
//            boolean trace_flag = false;
//
//            CloudSim.init(num_user, calendar, trace_flag);
//
//            Datacenter datacenter0 = createDatacenter("Datacenter_0");
//
//            broker = new FaultTolerantBroker("Broker");
//            int brokerId = broker.getId();
//
//            vmList = createVM(brokerId, 5, 0);
//            cloudletList = createCloudlet(brokerId, 10, 0);
//
//            broker.setVmList(vmList); // ✅ Sets both internal and custom VM lists
//            broker.submitCloudletList(cloudletList);
//
//            // Schedule VM failure at 100 simulation time units
//            CloudSim.send(broker.getId(), broker.getId(), 100.0, 999, null);
//
//            CloudSim.startSimulation();
//
//            List<Cloudlet> newList = broker.getCloudletReceivedList();
//            CloudSim.stopSimulation();
//
//            printCloudletList(newList);
//
//            Log.printLine("CloudSimExample11 finished!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.printLine("Unwanted errors happened");
//        }
//    }
//
//    private static Datacenter createDatacenter(String name) throws Exception {
//        List<Host> hostList = new ArrayList<>();
//
//        int mips = 1000;
//        List<Pe> peList = new ArrayList<>();
//        for (int i = 0; i < 4; i++) {
//            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
//        }
//
//        int ram = 16384;
//        long storage = 1000000;
//        int bw = 10000;
//
//        hostList.add(new Host(0,
//                new RamProvisionerSimple(ram),
//                new BwProvisionerSimple(bw),
//                storage,
//                peList,
//                new VmSchedulerTimeShared(peList)));
//
//        String arch = "x86";
//        String os = "Linux";
//        String vmm = "Xen";
//        double time_zone = 10.0;
//        double cost = 3.0;
//        double costPerMem = 0.05;
//        double costPerStorage = 0.1;
//        double costPerBw = 0.1;
//
//        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
//                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
//
//        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<>(), 0);
//    }
//
//    private static List<Vm> createVM(int userId, int vms, int idShift) {
//        List<Vm> list = new ArrayList<>();
//
//        long size = 10000;
//        int ram = 512;
//        int mips = 250;
//        long bw = 1000;
//        int pesNumber = 1;
//        String vmm = "Xen";
//
//        for (int i = 0; i < vms; i++) {
//            Vm vm = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
//            list.add(vm);
//        }
//
//        return list;
//    }
//
//    private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift) {
//        List<Cloudlet> list = new ArrayList<>();
//
//        long length = 40000;
//        long fileSize = 300;
//        long outputSize = 300;
//        int pesNumber = 1;
//        UtilizationModel utilizationModel = new UtilizationModelFull();
//
//        for (int i = 0; i < cloudlets; i++) {
//            Cloudlet cloudlet = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize,
//                    utilizationModel, utilizationModel, utilizationModel);
//            cloudlet.setUserId(userId);
//            list.add(cloudlet);
//        }
//
//        return list;
//    }
//
//    private static void printCloudletList(List<Cloudlet> list) {
//        int size = list.size();
//        Cloudlet cloudlet;
//        String indent = "    ";
//        DecimalFormat dft = new DecimalFormat("###.##");
//
//        Log.printLine("\n========== OUTPUT ==========");
//        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
//                "Data center ID" + indent + "VM ID" + indent + "Time" +
//                indent + "Start Time" + indent + "Finish Time");
//
//        for (int i = 0; i < size; i++) {
//            cloudlet = list.get(i);
//            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
//
//            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
//                Log.print("SUCCESS");
//                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() +
//                        indent + indent + dft.format(cloudlet.getActualCPUTime()) +
//                        indent + indent + dft.format(cloudlet.getExecStartTime()) +
//                        indent + indent + dft.format(cloudlet.getFinishTime()));
//            } else {
//                Log.print("FAILED");
//                Log.printLine();
//            }
//        }
//    }
//
//    // ✅ Fixed FaultTolerantBroker with correct setVmList()
//    public static class FaultTolerantBroker extends DatacenterBroker {
//        private List<Vm> allVmList;
//
//        public FaultTolerantBroker(String name) throws Exception {
//            super(name);
//        }
//
//        @Override
//        @SuppressWarnings("unchecked")
//        public <T extends Vm> void setVmList(List<T> list) {
//            super.setVmList(list);
//            this.allVmList = (List<Vm>) list;  // Cast once for internal tracking
//        }
//
//        @Override
//        public void processEvent(SimEvent ev) {
//            switch (ev.getTag()) {
//                case 999: // Custom failure injection
//                    injectFailure();
//                    break;
//                default:
//                    super.processEvent(ev);
//                    break;
//            }
//        }
//
//
//        private void injectFailure() {
//            Log.printLine(CloudSim.clock() + ": Injecting VM failure...");
//
//            if (allVmList.size() <= 2) {
//                Log.printLine("Not enough VMs to simulate failure.");
//                return;
//            }
//
//            Vm vmToFail = allVmList.get(2); // Simulate failure on VM #2
//            getVmsCreatedList().remove(vmToFail);
//            allVmList.remove(vmToFail);
//
//            List<Cloudlet> toReschedule = new ArrayList<>();
//            for (Cloudlet c : getCloudletSubmittedList()) {
//                if (c.getVmId() == vmToFail.getId()) {
//                    toReschedule.add(c);
//                }
//            }
//
//            for (Cloudlet c : toReschedule) {
//                Vm alternativeVm = allVmList.stream()
//                        .filter(vm -> vm.getId() != vmToFail.getId())
//                        .findFirst()
//                        .orElse(null);
//
//                if (alternativeVm != null) {
//                    c.setVmId(alternativeVm.getId());
//                    submitCloudletList(Collections.singletonList(c));
//                    Log.printLine("Rescheduled Cloudlet " + c.getCloudletId() + " to VM #" + alternativeVm.getId());
//                } else {
//                    Log.printLine("No VM available to reschedule Cloudlet " + c.getCloudletId());
//                }
//            }
//        }
//    }
//}


//package org.cloudbus.cloudsim.examples;
//
//import org.cloudbus.cloudsim.*;
//import org.cloudbus.cloudsim.core.CloudSim;
//import org.cloudbus.cloudsim.provisioners.*;
//
//import java.text.DecimalFormat;
//import java.util.*;
//import java.util.logging.*;
//
//public class FaultTolerantSimulationTest {
//
//    private static final Logger logger = Logger.getLogger(FaultTolerantSimulationTest.class.getName());
//
//    public static void main(String[] args) {
//        Log.printLine("Starting FaultTolerantSimulationTest...");
//        logger.setLevel(Level.INFO);
//
//        try {
//            int numUser = 1;
//            Calendar calendar = Calendar.getInstance();
//            boolean traceFlag = false;
//            CloudSim.init(numUser, calendar, traceFlag);
//
//            Log.printLine("Initialising...");
//
//            Datacenter datacenter0 = createDatacenter("Datacenter_0");
//
//            FaultTolerantBroker broker = new FaultTolerantBroker("Broker");
//            int brokerId = broker.getId();
//
//            List<Vm> vmList = new ArrayList<>();
//            List<Cloudlet> cloudletList = new ArrayList<>();
//
//            int mips = 1000;
//            int ram = 512;
//            long size = 10000;
//            int bw = 1000;
//            int pesNumber = 1;
//
//            for (int i = 0; i < 5; i++) {
//                Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, "Xen", new CloudletSchedulerTimeShared());
//                vmList.add(vm);
//            }
//
//            broker.submitVmList(vmList);
//
//            long cloudletLength = 6400000;
//            long fileSize = 300;
//            long outputSize = 300;
//
//            for (int i = 0; i < 10; i++) {
//                Cloudlet cloudlet = new Cloudlet(i, cloudletLength, pesNumber, fileSize, outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
//                cloudlet.setUserId(brokerId);
//                cloudlet.setVmId(i % 5); // initially map 2 cloudlets per VM
//                cloudletList.add(cloudlet);
//            }
//
//            broker.submitCloudletList(cloudletList);
//
//            broker.scheduleFailureEvent(100.0);
//
//            CloudSim.startSimulation();
//
//            List<Cloudlet> newList = broker.getCloudletReceivedList();
//
//            CloudSim.stopSimulation();
//
//            Log.printLine("\n========== FINAL CLOUDLET OUTPUT ==========\n");
//            printCloudletList(newList);
//            Log.printLine("Simulation finished!");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.printLine("An error occurred.");
//        }
//    }
//
//    private static Datacenter createDatacenter(String name) throws Exception {
//        List<Host> hostList = new ArrayList<>();
//
//        int mips = 10000; // boosted MIPS to allow multiple VMs
//        int ram = 20480;  // increased RAM
//        long storage = 1000000;
//        int bw = 10000;
//
//        List<Pe> peList = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
//        }
//
//        hostList.add(new Host(0,
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
//        double timeZone = 10.0;
//        double costPerSec = 3.0;
//        double costPerMem = 0.05;
//        double costPerStorage = 0.001;
//        double costPerBw = 0.0;
//
//        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
//                arch, os, vmm, hostList, timeZone, costPerSec,
//                costPerMem, costPerStorage, costPerBw);
//
//        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
//    }
//
//    private static void printCloudletList(List<Cloudlet> list) {
//        String indent = "    ";
//        Log.printLine("Cloudlet ID    STATUS    Data center ID    VM ID    Time    Start Time    Finish Time");
//
//        DecimalFormat dft = new DecimalFormat("###.##");
//
//        for (Cloudlet cloudlet : list) {
//            Log.print(indent + cloudlet.getCloudletId() + "\t");
//            Log.print((cloudlet.getStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "FAILED"));
//            Log.printLine(
//                    indent + cloudlet.getResourceId() +
//                            indent + cloudlet.getVmId() +
//                            indent + dft.format(cloudlet.getActualCPUTime()) +
//                            indent + dft.format(cloudlet.getExecStartTime()) +
//                            indent + dft.format(cloudlet.getFinishTime())
//            );
//        }
//    }
//
//    public static class FaultTolerantBroker extends DatacenterBroker {
//
//        private final List<Integer> failedVmIds = new ArrayList<>();
//
//        public FaultTolerantBroker(String name) throws Exception {
//            super(name);
//        }
//
//        public void scheduleFailureEvent(double time) {
//            send(getId(), time, 9999); // Send custom event
//        }
//
//        @Override
//        public void processEvent(SimEvent ev) {
//            if (ev.getTag() == 9999) {
//                injectFailure();
//            } else {
//                super.processEvent(ev);
//            }
//        }
//
//        private void injectFailure() {
//            Log.printLine("WARNING: " + CloudSim.clock() + ": Injecting VM failure...");
//            int vmIdToFail = 2;
//
//            for (Vm vm : getVmsCreatedList()) {
//                if (vm.getId() == vmIdToFail) {
//                    Log.printLine("WARNING: VM #" + vmIdToFail + " has failed and removed.");
//                    getVmsCreatedList().remove(vm);
//                    failedVmIds.add(vmIdToFail);
//
//                    // Reschedule its cloudlets
//                    for (Cloudlet cl : getCloudletList()) {
//                        if (cl.getVmId() == vmIdToFail) {
//                            int backupVmId = 0;
//                            cl.setVmId(backupVmId);
//                            Log.printLine("INFO: Rescheduled Cloudlet " + cl.getCloudletId() + " to VM #" + backupVmId);
//                        }
//                    }
//
//                    break;
//                }
//            }
//        }
//    }
//}

package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;
import java.util.logging.*;

public class FaultTolerantSimulationTest {

    private static final Logger logger = Logger.getLogger(FaultTolerantSimulationTest.class.getName());

    public static void main(String[] args) {
        Log.printLine("Starting FaultTolerantSimulationTest...");

        try {
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            DatacenterBrokerFaultTolerant broker = new DatacenterBrokerFaultTolerant("Broker");

            int brokerId = broker.getId();

            List<Vm> vmList = new ArrayList<>();
            int vmCount = 5;
            for (int i = 0; i < vmCount; i++) {
                Vm vm = new Vm(i, brokerId, 1000, 1, 1024, 1000, 10000,
                        "Xen", new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }

            List<Cloudlet> cloudletList = new ArrayList<>();
            int cloudletCount = 10;
            long length = 80000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            for (int i = 0; i < cloudletCount; i++) {
                Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize,
                        utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                cloudletList.add(cloudlet);
            }

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);

            // Schedule VM failure at 30.0 (early enough)
            broker.scheduleFailureEvent(30.0);

            CloudSim.startSimulation();

            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();

            printCloudletResults(resultList);
            Log.printLine("Simulation finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(2000)));
        }

        int hostId = 0;
        int ram = 8192;
        long storage = 1000000;
        int bw = 10000;

        hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
                storage, peList, new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timeZone = 10.0;
        double costPerSec = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;

        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, timeZone, costPerSec, costPerMem,
                costPerStorage, costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
    }

    private static void printCloudletResults(List<Cloudlet> list) {
        String indent = "    ";
        Log.printLine("\n========== FINAL CLOUDLET OUTPUT ==========\n");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent +
                "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() +
                        indent + indent + cloudlet.getVmId() +
                        indent + indent + cloudlet.getActualCPUTime() +
                        indent + indent + cloudlet.getExecStartTime() +
                        indent + indent + cloudlet.getFinishTime());
            }
        }
    }

    public static class DatacenterBrokerFaultTolerant extends DatacenterBroker {

        private int vmIdToFail = 2;
        private boolean hasFailed = false;
        private List<Cloudlet> rescheduledCloudlets = new ArrayList<>();

        public DatacenterBrokerFaultTolerant(String name) throws Exception {
            super(name);
        }

        public void scheduleFailureEvent(double time) {
            send(getId(), time, 9999); // 9999 = custom tag for failure
        }

        @Override
        public void processEvent(SimEvent ev) {
            if (ev.getTag() == 9999 && !hasFailed) {
                injectFailure();
                return;
            }
            super.processEvent(ev);
        }

        private void injectFailure() {
            hasFailed = true;
            Log.printLine("WARNING: VM #" + vmIdToFail + " failed at time: " + CloudSim.clock());

            List<Cloudlet> cloudletList = getCloudletList();

            for (Cloudlet cl : new ArrayList<>(cloudletList)) {
                if (cl.getVmId() == vmIdToFail) {
                    Log.printLine("INFO: Rescheduling Cloudlet " + cl.getCloudletId() +
                            " originally on failed VM #" + vmIdToFail);

                    cl.setVmId(-1);
                    rescheduledCloudlets.add(cl);
                }
            }

            int backupVmId = 0;
            for (Cloudlet cl : rescheduledCloudlets) {
                cl.setVmId(backupVmId);
                sendNow(getVmsToDatacentersMap().get(backupVmId), 21, cl); // 21 = CLOUDLET_SUBMIT
                Log.printLine("INFO: Resubmitted Cloudlet " + cl.getCloudletId() + " to VM #" + backupVmId);
            }

            rescheduledCloudlets.clear();
        }
    }
}
