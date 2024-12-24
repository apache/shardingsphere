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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Metadata import executor.
 */
@RequiredArgsConstructor
public final class MetaDataImportExecutor {
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor;
    
    private final ContextManager contextManager;
    
    public MetaDataImportExecutor(final ContextManager contextManager) {
        this.contextManager = contextManager;
        this.databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor(contextManager);
    }
    
    /**
     * Import cluster configurations.
     *
     * @param exportedMetaData exported metadata
     * @throws SQLException SQL exception
     */
    public void importClusterConfigurations(final ExportedMetaData exportedMetaData) throws SQLException {
        Collection<YamlProxyDatabaseConfiguration> databaseConfigs = getYamlProxyDatabaseConfigurations(exportedMetaData);
        importServerConfiguration(exportedMetaData);
        importDatabaseConfigurations(databaseConfigs);
    }
    
    private void importServerConfiguration(final ExportedMetaData exportedMetaData) throws SQLException {
        YamlProxyServerConfiguration yamlServerConfig = YamlEngine.unmarshal(exportedMetaData.getRules() + System.lineSeparator() + exportedMetaData.getProps(), YamlProxyServerConfiguration.class);
        if (null == yamlServerConfig) {
            return;
        }
        importGlobalRules(yamlServerConfig);
        importProps(yamlServerConfig);
    }
    
    private void importGlobalRules(final YamlProxyServerConfiguration yamlServerConfig) throws SQLException {
        Collection<RuleConfiguration> rules = ruleConfigSwapperEngine.swapToRuleConfigurations(yamlServerConfig.getRules());
        for (RuleConfiguration each : rules) {
            contextManager.getPersistServiceFacade().getMetaDataManagerPersistService().alterGlobalRuleConfiguration(each);
        }
    }
    
    private void importProps(final YamlProxyServerConfiguration yamlServerConfig) throws SQLException {
        contextManager.getPersistServiceFacade().getMetaDataManagerPersistService().alterProperties(yamlServerConfig.getProps());
    }
    
    private Collection<YamlProxyDatabaseConfiguration> getYamlProxyDatabaseConfigurations(final ExportedMetaData exportedMetaData) {
        Collection<YamlProxyDatabaseConfiguration> result = new LinkedList<>();
        for (String each : exportedMetaData.getDatabases().values()) {
            result.add(YamlEngine.unmarshal(each, YamlProxyDatabaseConfiguration.class));
        }
        return result;
    }
    
    /**
     * Import database configurations.
     *
     * @param databaseConfigs YAML proxy database configuration
     * @throws SQLException SQL exception
     */
    public void importDatabaseConfigurations(final Collection<YamlProxyDatabaseConfiguration> databaseConfigs) throws SQLException {
        for (YamlProxyDatabaseConfiguration each : databaseConfigs) {
            databaseConfigImportExecutor.importDatabaseConfiguration(each);
        }
    }
}
