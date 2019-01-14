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

import com.google.common.base.Optional;
import io.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import io.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.shardingui.common.domain.RegistryCenterConfig;
import io.shardingsphere.shardingui.common.exception.ShardingUIException;
import io.shardingsphere.shardingui.servcie.RegistryCenterConfigService;
import io.shardingsphere.shardingui.servcie.RegistryCenterService;
import io.shardingsphere.shardingui.util.RegistryCenterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Registry center service.
 * 
 * @author chenqingyang
 */
@Service
public final class RegistryCenterServiceImpl implements RegistryCenterService {
    
    @Autowired
    private RegistryCenterConfigService registryCenterConfigService;
    
    @Override
    public RegistryCenter getActivatedRegistryCenter() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return RegistryCenterFactory.createRegistryCenter(optional.get());
        }
        throw new ShardingUIException(ShardingUIException.SERVER_ERROR, "No activated registry center!");
    }
    
    @Override
    public ConfigurationNode getActivateConfigurationNode() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return new ConfigurationNode(optional.get().getOrchestrationName());
        }
        throw new ShardingUIException(ShardingUIException.SERVER_ERROR, "No activated registry center!");
    }
    
    @Override
    public StateNode getActivatedStateNode() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return new StateNode(optional.get().getOrchestrationName());
        }
        throw new ShardingUIException(ShardingUIException.SERVER_ERROR, "No activated registry center!");
    }
}
