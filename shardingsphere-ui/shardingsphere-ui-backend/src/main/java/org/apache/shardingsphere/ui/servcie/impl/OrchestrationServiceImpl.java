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

import org.apache.shardingsphere.orchestration.core.registrycenter.RegistryCenterNodeStatus;
import org.apache.shardingsphere.ui.common.dto.InstanceDTO;
import org.apache.shardingsphere.ui.common.dto.SlaveDataSourceDTO;
import org.apache.shardingsphere.ui.servcie.OrchestrationService;
import org.apache.shardingsphere.ui.servcie.RegistryCenterService;
import org.apache.shardingsphere.ui.servcie.ShardingSchemaService;
import org.apache.shardingsphere.ui.util.ConfigurationYamlConverter;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of orchestration operation service.
 */
@Service
public final class OrchestrationServiceImpl implements OrchestrationService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Autowired
    private ShardingSchemaService shardingSchemaService;
    
    @Override
    public Collection<InstanceDTO> getALLInstance() {
        List<String> instanceIds = registryCenterService.getActivatedRegistryCenter().getChildrenKeys(getInstancesNodeFullRootPath());
        Collection<InstanceDTO> result = new ArrayList<>(instanceIds.size());
        for (String instanceId : instanceIds) {
            String value = registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivatedStateNode().getInstancesNodeFullPath(instanceId));
            result.add(new InstanceDTO(instanceId, !RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(value)));
        }
        return result;
    }
    
    @Override
    public void updateInstanceStatus(final String instanceId, final boolean enabled) {
        String value = enabled ? "" : RegistryCenterNodeStatus.DISABLED.toString();
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivatedStateNode().getInstancesNodeFullPath(instanceId), value);
    }
    
    @Override
    public Collection<SlaveDataSourceDTO> getAllSlaveDataSource() {
        Collection<SlaveDataSourceDTO> result = new ArrayList<>();
        for (String schemaName : shardingSchemaService.getAllSchemaNames()) {
            String configData = shardingSchemaService.getRuleConfiguration(schemaName);
            if (!configData.contains("encryptors:\n")) {
                if (configData.contains("tables:\n")) {
                    handleShardingRuleConfiguration(result, configData, schemaName);
                } else {
                    handleMasterSlaveRuleConfiguration(result, configData, schemaName);
                }
            }
        }
        return result;
    }
    
    @Override
    public void updateSlaveDataSourceStatus(final String schemaNames, final String slaveDataSourceName, final boolean enabled) {
        String value = enabled ? "" : RegistryCenterNodeStatus.DISABLED.toString();
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivatedStateNode().getDataSourcesNodeFullPath(schemaNames + "." + slaveDataSourceName), value);
    }
    
    private String getInstancesNodeFullRootPath() {
        String result = registryCenterService.getActivatedStateNode().getInstancesNodeFullPath("");
        return result.substring(0, result.length() - 1);
    }
    
    private void handleShardingRuleConfiguration(final Collection<SlaveDataSourceDTO> slaveDataSourceDTOS, final String configData, final String schemaName) {
        ShardingRuleConfiguration shardingRuleConfiguration = ConfigurationYamlConverter.loadShardingRuleConfiguration(configData);
        Collection<MasterSlaveRuleConfiguration> masterSlaveRuleConfigs = shardingRuleConfiguration.getMasterSlaveRuleConfigs();
        for (MasterSlaveRuleConfiguration masterSlaveRuleConfiguration : masterSlaveRuleConfigs) {
            addSlaveDataSource(slaveDataSourceDTOS, masterSlaveRuleConfiguration, schemaName);
        }
    }
    
    private void handleMasterSlaveRuleConfiguration(final Collection<SlaveDataSourceDTO> slaveDataSourceDTOS, final String configData, final String schemaName) {
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(configData);
        addSlaveDataSource(slaveDataSourceDTOS, masterSlaveRuleConfiguration, schemaName);
    }
    
    private void addSlaveDataSource(final Collection<SlaveDataSourceDTO> slaveDataSourceDTOS, final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration, final String schemaName) {
        Collection<String> disabledSchemaDataSourceNames = getDisabledSchemaDataSourceNames();
        for (String slaveDateSourceName : masterSlaveRuleConfiguration.getSlaveDataSourceNames()) {
            slaveDataSourceDTOS.add(new SlaveDataSourceDTO(schemaName, masterSlaveRuleConfiguration.getMasterDataSourceName(),
                    slaveDateSourceName, !disabledSchemaDataSourceNames.contains(schemaName + "." + slaveDateSourceName)));
        }
    }
    
    private Collection<String> getDisabledSchemaDataSourceNames() {
        List<String> result = new ArrayList<>();
        List<String> schemaDataSourceNames = registryCenterService.getActivatedRegistryCenter().getChildrenKeys(registryCenterService.getActivatedStateNode().getDataSourcesNodeFullRootPath());
        for (String schemaDataSourceName : schemaDataSourceNames) {
            String value = registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivatedStateNode().getDataSourcesNodeFullPath(schemaDataSourceName));
            if (RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(value)) {
                result.add(schemaDataSourceName);
            }
        }
        return result;
    }
}
