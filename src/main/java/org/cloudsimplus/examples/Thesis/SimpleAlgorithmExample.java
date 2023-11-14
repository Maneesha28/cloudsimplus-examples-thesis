package org.cloudsimplus.examples.Thesis;/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2021 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class SimpleAlgorithmExample {
    private static final int HOSTS = 3;
    private static final int HOST_PES = 6;

    private static final int VMS = HOSTS * 3;
//    private static final int VM_PES = 2;

    private static final int CLOUDLETS = 8;
    private static final int CLOUDLET_PES = 2;
    private static final int CLOUDLET_LENGTH = 10000;

    private final CloudSimPlus simulation;
    private final DatacenterBroker broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;

    public static void main(String[] args) {
        new SimpleAlgorithmExample();
    }

    private SimpleAlgorithmExample() {
        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        simulation = new CloudSimPlus();
        datacenter0 = createDatacenter();

        //Creates a broker that is a software acting on behalf of a cloud customer to manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerSimple(simulation);

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();

        final var cloudletFinishedList = broker0.getCloudletFinishedList();
        cloudletFinishedList.sort(Comparator.comparingLong(cloudlet -> cloudlet.getVm().getId()));
        new CloudletsTableBuilder(cloudletFinishedList).build();
    }

    /**
     * Creates a Datacenter and its Hosts.
     */
    private Datacenter createDatacenter() {
        final var hostList = new ArrayList<Host>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            final var host = createHost();
            hostList.add(host);
        }
        final var vmAllocationPolicyCustom = new VmAllocationPolicyCustom();
        //Replaces the default method that allocates Hosts to VMs by our own implementation
        vmAllocationPolicyCustom.setFindHostForVmFunction(this::findPrioritySuitableHostForVm);
        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicyCustom());
    }

    private Optional<Host> findPrioritySuitableHostForVm(final VmAllocationPolicy vmAllocationPolicy, final Vm vm) {
        final var hostList = vmAllocationPolicy.getHostList();
        long hostId = vm.getId()/3;
        final Host host = hostList.get((int)hostId);
        if(host.isSuitableForVm(vm)){
                return Optional.of(host);
        }
        return Optional.empty();
    }

    private Host createHost() {
        final var peList = new ArrayList<Pe>(HOST_PES);
        //List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < HOST_PES; i++) {
            //Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(1000));
        }

        final long ram = 8 * 1024; //in Megabytes
        final long bw = 10000; //in Megabits/s
        final long storage = 1000000; //in Megabytes

        /*
        Uses ResourceProvisionerSimple by default for RAM and BW provisioning
        and VmSchedulerSpaceShared for VM scheduling.
        */
        return new HostSimple(ram, bw, storage, peList, false);
    }

    /**
     * Creates a list of VMs.
     */
    private List<Vm> createVms() {
        final var list = new ArrayList<Vm>(VMS);
        for (int i = 0; i < VMS; i++) {
            //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
            int VM_PES;
            if(i % 3 == 0) {
                VM_PES = 3;
            } else if(i % 3 == 1) {
                VM_PES = 2;
            } else {
                VM_PES = 1;
            }
            final Vm vm = new VmSimple(i,1000, VM_PES);
            vm.setRam(2048).setBw(1000).setSize(10000);
            list.add(vm);
        }

        return list;
    }

    /**
     * Creates a list of Cloudlets.
     */
    private List<Cloudlet> createCloudlets() {
        final var list = new ArrayList<Cloudlet>(CLOUDLETS);

        //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
        final var utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < CLOUDLETS; i++) {
            final var cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
            cloudlet.setSizes(1024);
            list.add(cloudlet);
        }

        return list;
    }
}
