/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.servcie.impl;

import io.shardingsphere.orchestration.internal.registry.state.node.StateNodeStatus;
import io.shardingsphere.shardingui.common.dto.InstanceDTO;
import io.shardingsphere.shardingui.servcie.InstanceService;
import io.shardingsphere.shardingui.servcie.RegistryCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of instance operation service.
 *
 * @author chenqingyang
 */
@Service
public final class InstanceServiceImpl implements InstanceService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Override
    public Collection<InstanceDTO> getALLInstance() {
        List<String> instanceIds = registryCenterService.getActivatedRegistryCenter().getChildrenKeys(getInstancesNodeFullRootPath());
        Collection<InstanceDTO> result = new ArrayList<>(instanceIds.size());
        for (String instanceId : instanceIds) {
            String value = registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivatedStateNode().getInstancesNodeFullPath(instanceId));
            result.add(new InstanceDTO(instanceId, !StateNodeStatus.DISABLED.toString().equalsIgnoreCase(value)));
        }
        return result;
    }
    
    @Override
    public void updateInstanceStatus(final String instanceId, final boolean enabled) {
        String value = enabled ? "" : StateNodeStatus.DISABLED.toString();
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivatedStateNode().getInstancesNodeFullPath(instanceId), value);
    }
    
    private String getInstancesNodeFullRootPath() {
        String result = registryCenterService.getActivatedStateNode().getInstancesNodeFullPath("");
        return result.substring(0, result.length() - 1);
    }
}
