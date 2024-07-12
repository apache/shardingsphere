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

package org.apache.shardingsphere.mode.manager.cluster.persist;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssisted;
import org.apache.shardingsphere.mode.persist.pojo.ListenerAssistedType;
import org.apache.shardingsphere.mode.persist.service.ListenerAssistedPersistService;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Cluster meta data manager persist service.
 */
public final class ClusterMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ListenerAssistedPersistService listenerAssistedPersistService;
    
    private final MetaDataContextManager metaDataContextManager;
    
    public ClusterMetaDataManagerPersistService(final PersistRepository repository, final MetaDataContextManager metaDataContextManager) {
        metaDataPersistService = new MetaDataPersistService(repository);
        listenerAssistedPersistService = new ListenerAssistedPersistService(repository);
        this.metaDataContextManager = metaDataContextManager;
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataPersistService.getDatabaseMetaDataService().addDatabase(databaseName);
        listenerAssistedPersistService.persistDatabaseNameListenerAssisted(new ListenerAssisted(databaseName, ListenerAssistedType.CREATE_DATABASE));
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        String droppedDatabaseName = metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName).getName();
        listenerAssistedPersistService.persistDatabaseNameListenerAssisted(new ListenerAssisted(droppedDatabaseName, ListenerAssistedType.DROP_DATABASE));
        metaDataPersistService.getDatabaseMetaDataService().dropDatabase(droppedDatabaseName);
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        metaDataPersistService.getDatabaseMetaDataService().addSchema(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        String databaseName = alterSchemaPOJO.getDatabaseName();
        String schemaName = alterSchemaPOJO.getSchemaName();
        ShardingSphereSchema schema = metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName).getSchema(schemaName);
        DatabaseMetaDataPersistService databaseMetaDataService = metaDataPersistService.getDatabaseMetaDataService();
        databaseMetaDataService.persistByAlterConfiguration(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema.getViews());
        databaseMetaDataService.dropSchema(databaseName, schemaName);
    }
    
    @Override
    public void dropSchema(final String databaseName, final Collection<String> schemaNames) {
        DatabaseMetaDataPersistService databaseMetaDataService = metaDataPersistService.getDatabaseMetaDataService();
        schemaNames.forEach(each -> databaseMetaDataService.dropSchema(databaseName, each));
    }
    
    @Override
    public void alterSchemaMetaData(final AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO) {
        String databaseName = alterSchemaMetaDataPOJO.getDatabaseName();
        String schemaName = alterSchemaMetaDataPOJO.getSchemaName();
        Map<String, ShardingSphereTable> tables = alterSchemaMetaDataPOJO.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = alterSchemaMetaDataPOJO.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        DatabaseMetaDataPersistService databaseMetaDataService = metaDataPersistService.getDatabaseMetaDataService();
        databaseMetaDataService.getTableMetaDataPersistService().persist(databaseName, schemaName, tables);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, schemaName, views);
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        metaDataPersistService.getDataSourceUnitService().persistConfigurations(databaseName, toBeRegisteredProps);
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
        DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeUpdatedProps));
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) {
        for (String each : getToBeDroppedResourceNames(databaseName, toBeDroppedStorageUnitNames)) {
            metaDataPersistService.getDataSourceUnitService().delete(databaseName, each);
        }
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = metaDataPersistService.getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.removeIf(each -> !each.getClass().isAssignableFrom(SingleRuleConfiguration.class));
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(metaDataPersistService.getDatabaseRulePersistService().persistConfigurations(databaseName, ruleConfigs));
    }
    
    @Override
    public Collection<MetaDataVersion> alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null != toBeAlteredRuleConfig) {
            return metaDataPersistService.getDatabaseRulePersistService()
                    .persistConfigurations(databaseName, Collections.singleton(toBeAlteredRuleConfig));
        }
        return Collections.emptyList();
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            metaDataPersistService.getDatabaseRulePersistService().deleteConfigurations(databaseName, Collections.singleton(toBeRemovedRuleConfig));
        }
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final String ruleName) {
        metaDataPersistService.getDatabaseRulePersistService().delete(databaseName, ruleName);
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        metaDataPersistService.getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
    }
    
    @Override
    public void alterProperties(final Properties props) {
        metaDataPersistService.getPropsService().persist(props);
    }
    
    @Override
    public void afterStorageUnitsAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts, final boolean isDropConfig) {
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts().get();
        // TODO Confirm if it should be persisted twice
        persistSchemaMetaData(databaseName, reloadMetaDataContexts, isDropConfig);
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseData().get(databaseName))
                .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> metaDataPersistService.getShardingSphereDataPersistService()
                        .persist(databaseName, schemaName, schemaData, originalMetaDataContexts.getMetaData().getDatabases())));
        metaDataPersistService.persistMetaDataByReloadDatabase(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName), isDropConfig);
    }
    
    @Override
    public void afterRuleConfigurationAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts, final boolean isDropConfig) {
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts().get();
        metaDataPersistService.persistMetaDataByReloadDatabase(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName), isDropConfig);
    }
    
    private void persistSchemaMetaData(final String databaseName, final MetaDataContexts reloadMetaDataContexts, final boolean isDropConfig) {
        if (isDropConfig) {
            reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> metaDataPersistService.getDatabaseMetaDataService()
                    .persistByDropConfiguration(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), schemaName, schema));
        } else {
            reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getSchemas().forEach((schemaName, schema) -> metaDataPersistService.getDatabaseMetaDataService()
                    .persistByAlterConfiguration(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), schemaName, schema));
        }
    }
}
