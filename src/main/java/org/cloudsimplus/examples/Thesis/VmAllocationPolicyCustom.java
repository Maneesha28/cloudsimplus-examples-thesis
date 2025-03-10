package org.cloudsimplus.examples.Thesis;

/*
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
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Optional;

/**
 * A <b>Round-Robin VM allocation policy</b>
 * which finds the next Host having suitable resources to place a given VM
 * in a circular way. That means when it selects a suitable Host to place a VM,
 * it moves to the next suitable Host when a new VM has to be placed.
 * This is a high time-efficient policy with a best-case complexity O(1)
 * and a worst-case complexity O(N), where N is the number of Hosts.
 *
 * <p>
 *     <b>NOTES:</b>
 *     <ul>
 *         <li>This policy doesn't perform optimization of VM allocation by means of VM migration.</li>
 *         <li>It has a low computational complexity (high time-efficient) but may return
 *         and inactive Host that will be activated, while there may be active Hosts
 *         suitable for the VM.</li>
 *         <li>Despite the low computational complexity, such a policy will increase the number of active Hosts,
 *         that increases power consumption.</li>
 *     </ul>
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 4.4.2
 */
public class VmAllocationPolicyCustom extends VmAllocationPolicyAbstract implements VmAllocationPolicy {
    /**
     * The index of the last host used to place a VM.
     */
    private int lastHostIndex;

    @Override
    protected Optional<Host> defaultFindHostForVm(final Vm vm) {
        final var hostList = this.getHostList();
        long hostId = vm.getId()/3;
        System.out.println(vm.getId()+" "+hostId);
        final Host host = hostList.get((int)hostId);
        if(host.isSuitableForVm(vm)){
            return Optional.of(host);
        }
        return Optional.empty();
    }
}

