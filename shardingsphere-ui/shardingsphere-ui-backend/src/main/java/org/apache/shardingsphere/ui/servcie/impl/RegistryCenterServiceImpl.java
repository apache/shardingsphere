/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.ui.servcie.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.orchestration.center.api.RegistryCenterRepository;
import org.apache.shardingsphere.ui.common.domain.RegistryCenterConfig;
import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import org.apache.shardingsphere.ui.servcie.RegistryCenterConfigService;
import org.apache.shardingsphere.ui.servcie.RegistryCenterService;
import org.apache.shardingsphere.ui.util.RegistryCenterRepositoryFactory;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.internal.registry.state.node.StateNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Registry center service.
 */
@Service
public final class RegistryCenterServiceImpl implements RegistryCenterService {
    
    @Autowired
    private RegistryCenterConfigService registryCenterConfigService;
    
    @Override
    public RegistryCenterRepository getActivatedRegistryCenter() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return RegistryCenterRepositoryFactory.createRegistryCenter(optional.get());
        }
        throw new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "No activated registry center!");
    }
    
    @Override
    public ConfigurationNode getActivateConfigurationNode() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return new ConfigurationNode(optional.get().getOrchestrationName());
        }
        throw new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "No activated registry center!");
    }
    
    @Override
    public StateNode getActivatedStateNode() {
        Optional<RegistryCenterConfig> optional = registryCenterConfigService.loadActivated();
        if (optional.isPresent()) {
            return new StateNode(optional.get().getOrchestrationName());
        }
        throw new ShardingSphereUIException(ShardingSphereUIException.SERVER_ERROR, "No activated registry center!");
    }
}
