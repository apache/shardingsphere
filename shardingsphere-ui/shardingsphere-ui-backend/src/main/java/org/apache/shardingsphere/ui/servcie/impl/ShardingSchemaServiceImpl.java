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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.ui.servcie.RegistryCenterService;
import org.apache.shardingsphere.ui.servcie.ShardingSchemaService;
import org.apache.shardingsphere.ui.util.ConfigurationYamlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Map;

/**
 * Implementation of sharding schema service.
 *
 * @author chenqingyang
 */
@Service
public final class ShardingSchemaServiceImpl implements ShardingSchemaService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Override
    public Collection<String> getAllSchemaNames() {
        return registryCenterService.getActivatedRegistryCenter().getChildrenKeys(registryCenterService.getActivateConfigurationNode().getSchemaPath());
    }
    
    @Override
    public String getRuleConfiguration(final String schemaName) {
        return registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getRulePath(schemaName));
    }
    
    @Override
    public String getDataSourceConfiguration(final String schemaName) {
        return registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getDataSourcePath(schemaName));
    }
    
    @Override
    public void updateRuleConfiguration(final String schemaName, final String configData) {
        checkRuleConfiguration(configData);
        persistRuleConfiguration(schemaName, configData);
    }
    
    @Override
    public void updateDataSourceConfiguration(final String schemaName, final String configData) {
        checkDataSourceConfiguration(configData);
        persistDataSourceConfiguration(schemaName, configData);
    }
    
    @Override
    public void addSchemaConfiguration(final String schemaName, final String ruleConfiguration, final String dataSourceConfiguration) {
        checkSchemaName(schemaName, getAllSchemaNames());
        checkRuleConfiguration(ruleConfiguration);
        checkDataSourceConfiguration(dataSourceConfiguration);
        persistRuleConfiguration(schemaName, ruleConfiguration);
        persistDataSourceConfiguration(schemaName, dataSourceConfiguration);
    }
    
    private void checkRuleConfiguration(final String configData) {
        try {
            if (configData.contains("encryptors:\n")) {
                ConfigurationYamlConverter.loadEncryptRuleConfiguration(configData);
            } else if (configData.contains("tables:\n")) {
                ConfigurationYamlConverter.loadShardingRuleConfiguration(configData);
            } else {
                ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(configData);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalArgumentException("rule configuration is invalid.");
        }
    }
    
    private void persistRuleConfiguration(final String schemaName, final String ruleConfiguration) {
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivateConfigurationNode().getRulePath(schemaName), ruleConfiguration);
    }
    
    private void checkDataSourceConfiguration(final String configData) {
        try {
            Map<String, DataSourceConfiguration> dataSourceConfigs = ConfigurationYamlConverter.loadDataSourceConfigurations(configData);
            Preconditions.checkState(!dataSourceConfigs.isEmpty(), "data source configuration is invalid.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalArgumentException("data source configuration is invalid.");
        }
    }
    
    private void persistDataSourceConfiguration(final String schemaName, final String dataSourceConfiguration) {
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivateConfigurationNode().getDataSourcePath(schemaName), dataSourceConfiguration);
    }
    
    private void checkSchemaName(final String schemaName, final Collection<String> existedSchemaNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(schemaName), "schema name is invalid.");
        Preconditions.checkArgument(!existedSchemaNames.contains(schemaName), "schema name already exists.");
    }
    
}
