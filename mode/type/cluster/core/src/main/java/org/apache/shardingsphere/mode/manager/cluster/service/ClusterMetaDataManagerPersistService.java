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

package org.apache.shardingsphere.mode.manager.cluster.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster meta data manager persist service.
 */
@RequiredArgsConstructor
public final class ClusterMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final ContextManager contextManager;
    
    @Override
    public void createDatabase(final String databaseName) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().addDatabase(databaseName);
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().dropDatabase(databaseName);
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().addSchema(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        String databaseName = alterSchemaPOJO.getDatabaseName();
        String schemaName = alterSchemaPOJO.getSchemaName();
        ShardingSphereSchema schema = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchema(schemaName);
        DatabaseMetaDataPersistService databaseMetaDataService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService();
        databaseMetaDataService.persistByAlterConfiguration(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema.getViews());
        databaseMetaDataService.dropSchema(databaseName, schemaName);
    }
    
    @Override
    public void dropSchema(final String databaseName, final Collection<String> schemaNames) {
        DatabaseMetaDataPersistService databaseMetaDataService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService();
        schemaNames.forEach(each -> databaseMetaDataService.dropSchema(databaseName, each));
    }
    
    @Override
    public void alterSchemaMetaData(final AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO) {
        String databaseName = alterSchemaMetaDataPOJO.getDatabaseName();
        String schemaName = alterSchemaMetaDataPOJO.getSchemaName();
        Map<String, ShardingSphereTable> tables = alterSchemaMetaDataPOJO.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = alterSchemaMetaDataPOJO.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        DatabaseMetaDataPersistService databaseMetaDataService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService();
        databaseMetaDataService.getTableMetaDataPersistService().persist(databaseName, schemaName, tables);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, schemaName, views);
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().persistConfigurations(databaseName, toBeRegisteredProps);
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
        DataSourceUnitPersistService dataSourceService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService();
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeUpdatedProps));
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) {
        for (String each : getToBeDroppedResourceNames(databaseName, toBeDroppedStorageUnitNames)) {
            contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().delete(databaseName, each);
        }
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.removeIf(each -> !each.getClass().isAssignableFrom(SingleRuleConfiguration.class));
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .switchActiveVersion(contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService().persistConfigurations(databaseName, ruleConfigs));
    }
    
    @Override
    public Collection<MetaDataVersion> alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null != toBeAlteredRuleConfig) {
            return contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService()
                    .persistConfigurations(databaseName, Collections.singleton(toBeAlteredRuleConfig));
        }
        return Collections.emptyList();
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService().deleteConfigurations(databaseName, Collections.singleton(toBeRemovedRuleConfig));
        }
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final String ruleName) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService().delete(databaseName, ruleName);
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
    }
    
    @Override
    public void alterProperties(final Properties props) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getPropsService().persist(props);
    }
}
