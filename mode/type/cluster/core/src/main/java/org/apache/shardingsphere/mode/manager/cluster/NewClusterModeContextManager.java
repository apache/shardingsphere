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

package org.apache.shardingsphere.mode.manager.cluster;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.node.NewDatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataBasedPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionBasedPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerAware;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * TODO Rename to ClusterModeContextManager after meta data refactor completed
 * New cluster mode context manager.
 */
public final class NewClusterModeContextManager implements ModeContextManager, ContextManagerAware {
    
    private ContextManager contextManager;
    
    @Override
    public void createDatabase(final String databaseName) {
        contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService().addDatabase(databaseName);
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService().dropDatabase(databaseName);
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService().addSchema(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        String databaseName = alterSchemaPOJO.getDatabaseName();
        String schemaName = alterSchemaPOJO.getSchemaName();
        ShardingSphereSchema schema = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchema(schemaName);
        DatabaseMetaDataBasedPersistService databaseMetaDataService = contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService();
        databaseMetaDataService.persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema.getViews());
        databaseMetaDataService.dropSchema(databaseName, schemaName);
    }
    
    @Override
    public void dropSchema(final String databaseName, final Collection<String> schemaNames) {
        DatabaseMetaDataBasedPersistService databaseMetaDataService = contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService();
        schemaNames.forEach(each -> databaseMetaDataService.dropSchema(databaseName, each));
    }
    
    @Override
    public void alterSchemaMetaData(final AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO) {
        String databaseName = alterSchemaMetaDataPOJO.getDatabaseName();
        String schemaName = alterSchemaMetaDataPOJO.getSchemaName();
        Map<String, ShardingSphereTable> tables = alterSchemaMetaDataPOJO.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = alterSchemaMetaDataPOJO.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        DatabaseMetaDataBasedPersistService databaseMetaDataService = contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService();
        MetaDataVersionBasedPersistService metaDataVersionBasedPersistService = contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService();
        metaDataVersionBasedPersistService.switchActiveVersion(databaseMetaDataService.getTableMetaDataPersistService().persistSchemaMetaData(databaseName, schemaName, tables));
        metaDataVersionBasedPersistService.switchActiveVersion(databaseMetaDataService.getViewMetaDataPersistService().persistSchemaMetaData(databaseName, schemaName, views));
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourceProperties> toBeRegisterStorageUnitProps) {
        contextManager.getMetaDataContexts().getPersistService().getDataSourceService().append(databaseName, toBeRegisterStorageUnitProps);
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourceProperties> toBeUpdatedStorageUnitProps) {
        contextManager.getMetaDataContexts().getPersistService().getDataSourceService().append(databaseName, toBeUpdatedStorageUnitProps);
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) {
        contextManager.getMetaDataContexts().getPersistService().getDataSourceService().delete(databaseName,
                getToBeReversedDataSourcePropsMap(contextManager.getMetaDataContexts().getPersistService().getDataSourceService().load(databaseName), toBeDroppedStorageUnitNames));
    }
    
    private Map<String, DataSourceProperties> getToBeReversedDataSourcePropsMap(final Map<String, DataSourceProperties> dataSourcePropsMap, final Collection<String> toBeDroppedResourceNames) {
        return dataSourcePropsMap.entrySet().stream().filter(entry -> toBeDroppedResourceNames.contains(entry.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    @Override
    public void alterRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        contextManager.getMetaDataContexts().getPersistService().getDatabaseRulePersistService().persist(databaseName, ruleConfigs);
    }
    
    @Override
    public Collection<MetaDataVersion> alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null != toBeAlteredRuleConfig) {
            return contextManager.getMetaDataContexts().getPersistService().getDatabaseRulePersistService().persistConfig(databaseName, Collections.singleton(toBeAlteredRuleConfig));
        }
        return Collections.emptyList();
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            contextManager.getMetaDataContexts().getPersistService().getDatabaseRulePersistService().delete(databaseName, Collections.singleton(toBeRemovedRuleConfig));
        }
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final Collection<RuleConfiguration> globalRuleConfigs) {
        contextManager.getMetaDataContexts().getPersistService().getGlobalRuleService().persist(globalRuleConfigs);
    }
    
    @Override
    public Collection<MetaDataVersion> alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        return contextManager.getMetaDataContexts().getPersistService().getGlobalRuleService().persistConfig(Collections.singleton(toBeAlteredRuleConfig));
    }
    
    @Override
    public void alterProperties(final Properties props) {
        contextManager.getMetaDataContexts().getPersistService().getPropsService().persist(props);
    }
    
    @Override
    public Collection<MetaDataVersion> newAlterProperties(final Properties props) {
        return contextManager.getMetaDataContexts().getPersistService().getPropsService().persistConfig(props);
    }
    
    @Override
    public String getActiveVersionByKey(final String key) {
        return contextManager.getMetaDataContexts().getPersistService().getRepository().getDirectly(key);
    }
    
    @Override
    public String getVersionPathByActiveVersionKey(final String key, final String activeVersion) {
        return contextManager.getMetaDataContexts().getPersistService().getRepository().getDirectly(NewDatabaseMetaDataNode.getVersionNodeByActiveVersionPath(key, activeVersion));
    }
    
    @Override
    public void setContextManagerAware(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }
}
