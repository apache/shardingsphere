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

package org.apache.shardingsphere.distsql.handler.executor.util;

import org.apache.shardingsphere.distsql.handler.executor.config.yaml.YamlExportedServerConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.export.ExportedMetaData;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Metadata import executor.
 */
public final class MetaDataImportExecutor {
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor;
    
    private final ContextManager contextManager;
    
    public MetaDataImportExecutor(final ContextManager contextManager) {
        this.contextManager = contextManager;
        databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor(contextManager);
    }
    
    /**
     * Import cluster configurations.
     *
     * @param exportedMetaData exported metadata
     */
    public void importClusterConfigurations(final ExportedMetaData exportedMetaData) {
        Map<String, YamlProxyDatabaseConfiguration> yamlDatabaseConfigs = getYamlProxyDatabaseConfigurations(exportedMetaData);
        YamlExportedServerConfiguration yamlServerConfig = getYamlServerConfiguration(exportedMetaData);
        importServerConfiguration(yamlServerConfig);
        importDatabaseConfigurations(yamlDatabaseConfigs.values());
    }
    
    private void importServerConfiguration(final YamlExportedServerConfiguration yamlServerConfig) {
        if (null == yamlServerConfig) {
            return;
        }
        importGlobalRules(yamlServerConfig);
        importProps(yamlServerConfig);
    }
    
    private void importGlobalRules(final YamlExportedServerConfiguration yamlServerConfig) {
        Collection<RuleConfiguration> rules = ruleConfigSwapperEngine.swapToRuleConfigurations(yamlServerConfig.getRules());
        for (RuleConfiguration each : rules) {
            contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterGlobalRuleConfiguration(each);
        }
    }
    
    private void importProps(final YamlExportedServerConfiguration yamlServerConfig) {
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterProperties(yamlServerConfig.getProps());
    }
    
    private Map<String, YamlProxyDatabaseConfiguration> getYamlProxyDatabaseConfigurations(final ExportedMetaData exportedMetaData) {
        return exportedMetaData.getDatabases().entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> YamlEngine.unmarshal(entry.getValue(), YamlProxyDatabaseConfiguration.class), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private YamlExportedServerConfiguration getYamlServerConfiguration(final ExportedMetaData exportedMetaData) {
        return YamlEngine.unmarshal(exportedMetaData.getRules() + System.lineSeparator() + exportedMetaData.getProps(), YamlExportedServerConfiguration.class);
    }
    
    /**
     * Import database configurations.
     *
     * @param databaseConfigs YAML proxy database configuration
     */
    public void importDatabaseConfigurations(final Collection<YamlProxyDatabaseConfiguration> databaseConfigs) {
        for (YamlProxyDatabaseConfiguration each : databaseConfigs) {
            databaseConfigImportExecutor.importDatabaseConfiguration(each);
        }
    }
}
