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

package org.apache.shardingsphere.distsql.handler.executor.configuration.imports;

import org.apache.shardingsphere.distsql.handler.executor.configuration.export.model.ExportedMetaData;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDatabaseConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlExportedServerConfiguration;
import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.swapper.YamlDatabaseConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * Metadata import executor.
 */
public final class MetaDataImportExecutor {
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlDatabaseConfigurationSwapper databaseConfigSwapper = new YamlDatabaseConfigurationSwapper();
    
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
        Collection<DatabaseConfigurationImportInput> databaseConfigs = getDatabaseConfigurations(exportedMetaData);
        YamlExportedServerConfiguration yamlServerConfig = getYamlServerConfiguration(exportedMetaData);
        if (null != yamlServerConfig) {
            Collection<RuleConfiguration> globalRuleConfigs = ruleConfigSwapperEngine.swapToRuleConfigurations(yamlServerConfig.getRules());
            importServerConfiguration(globalRuleConfigs, yamlServerConfig.getProps());
        }
        importDatabaseConfigurations(databaseConfigs);
    }
    
    private Collection<DatabaseConfigurationImportInput> getDatabaseConfigurations(final ExportedMetaData exportedMetaData) {
        Collection<DatabaseConfigurationImportInput> result = new ArrayList<>(exportedMetaData.getDatabases().size());
        for (String each : exportedMetaData.getDatabases().values()) {
            result.add(databaseConfigSwapper.swap(YamlEngine.unmarshal(each, YamlDatabaseConfiguration.class)));
        }
        return result;
    }
    
    private YamlExportedServerConfiguration getYamlServerConfiguration(final ExportedMetaData exportedMetaData) {
        return YamlEngine.unmarshal(exportedMetaData.getRules() + System.lineSeparator() + exportedMetaData.getProps(), YamlExportedServerConfiguration.class);
    }
    
    private void importServerConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props) {
        for (RuleConfiguration each : globalRuleConfigs) {
            contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterGlobalRuleConfiguration(each);
        }
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().alterProperties(props);
    }
    
    /**
     * Import database configurations.
     *
     * @param databaseConfigs database configuration inputs
     */
    public void importDatabaseConfigurations(final Collection<DatabaseConfigurationImportInput> databaseConfigs) {
        for (DatabaseConfigurationImportInput each : databaseConfigs) {
            databaseConfigImportExecutor.importDatabaseConfiguration(each);
        }
    }
}
