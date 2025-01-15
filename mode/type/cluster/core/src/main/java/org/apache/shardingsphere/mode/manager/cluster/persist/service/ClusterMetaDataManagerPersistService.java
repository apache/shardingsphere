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

package org.apache.shardingsphere.mode.manager.cluster.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.destroyer.DataSourcePoolDestroyer;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.persist.service.unified.ListenerAssistedType;
import org.apache.shardingsphere.mode.persist.service.unified.ListenerAssistedPersistService;
import org.apache.shardingsphere.mode.persist.service.divided.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
        metaDataPersistService.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        listenerAssistedPersistService.persistDatabaseNameListenerAssisted(databaseName, ListenerAssistedType.CREATE_DATABASE);
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        String droppedDatabaseName = metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName();
        listenerAssistedPersistService.persistDatabaseNameListenerAssisted(droppedDatabaseName, ListenerAssistedType.DROP_DATABASE);
        metaDataPersistService.getDatabaseMetaDataFacade().getDatabase().drop(droppedDatabaseName);
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        String databaseName = alterSchemaPOJO.getDatabaseName();
        String schemaName = alterSchemaPOJO.getSchemaName();
        ShardingSphereSchema schema = metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchema(schemaName);
        if (schema.isEmpty()) {
            metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(databaseName, alterSchemaPOJO.getRenameSchemaName());
        }
        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema.getAllTables());
        metaDataPersistService.getDatabaseMetaDataFacade().getView().persist(databaseName, alterSchemaPOJO.getRenameSchemaName(), schema.getAllViews());
        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().drop(databaseName, schemaName);
    }
    
    @Override
    public void dropSchema(final String databaseName, final Collection<String> schemaNames) {
        schemaNames.forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getSchema().drop(databaseName, each));
    }
    
    @Override
    public void alterSchemaMetaData(final AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO) {
        String databaseName = alterSchemaMetaDataPOJO.getDatabaseName();
        String schemaName = alterSchemaMetaDataPOJO.getSchemaName();
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = metaDataPersistService.getDatabaseMetaDataFacade();
        databaseMetaDataFacade.getTable().persist(databaseName, schemaName, alterSchemaMetaDataPOJO.getAlteredTables());
        databaseMetaDataFacade.getView().persist(databaseName, schemaName, alterSchemaMetaDataPOJO.getAlteredViews());
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataFacade.getTable().drop(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataFacade.getView().delete(databaseName, schemaName, each));
    }
    
    @SneakyThrows
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        MetaDataContexts originalMetaDataContexts = buildOriginalMetaDataContexts();
        Map<StorageNode, DataSource> newDataSources = new HashMap<>(toBeRegisteredProps.size());
        try {
            SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager()
                    .switchByRegisterStorageUnit(originalMetaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisteredProps);
            newDataSources.putAll(switchingResource.getNewDataSources());
            MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createBySwitchResource(databaseName, false,
                    switchingResource, originalMetaDataContexts, metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
            metaDataPersistService.getDataSourceUnitService().persist(databaseName, toBeRegisteredProps);
            afterStorageUnitsAltered(databaseName, originalMetaDataContexts, reloadMetaDataContexts);
            reloadMetaDataContexts.getMetaData().close();
        } finally {
            closeNewDataSources(newDataSources);
        }
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException {
        MetaDataContexts originalMetaDataContexts = buildOriginalMetaDataContexts();
        Map<StorageNode, DataSource> newDataSources = new HashMap<>(toBeUpdatedProps.size());
        try {
            SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager()
                    .switchByAlterStorageUnit(originalMetaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeUpdatedProps);
            newDataSources.putAll(switchingResource.getNewDataSources());
            MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createBySwitchResource(databaseName, false,
                    switchingResource, originalMetaDataContexts, metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
            DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
            metaDataPersistService.getMetaDataVersionPersistService()
                    .switchActiveVersion(dataSourceService.persist(databaseName, toBeUpdatedProps));
            afterStorageUnitsAltered(databaseName, originalMetaDataContexts, reloadMetaDataContexts);
            reloadMetaDataContexts.getMetaData().close();
        } finally {
            closeNewDataSources(newDataSources);
        }
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        for (String each : getToBeDroppedResourceNames(databaseName, toBeDroppedStorageUnitNames)) {
            MetaDataContexts originalMetaDataContexts = buildOriginalMetaDataContexts();
            SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager()
                    .createByUnregisterStorageUnit(originalMetaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), Collections.singletonList(each));
            MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createBySwitchResource(databaseName, false,
                    switchingResource, originalMetaDataContexts, metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
            metaDataPersistService.getDataSourceUnitService().delete(databaseName, each);
            afterStorageUnitsDropped(databaseName, originalMetaDataContexts, reloadMetaDataContexts);
            reloadMetaDataContexts.getMetaData().close();
        }
    }
    
    private void closeNewDataSources(final Map<StorageNode, DataSource> newDataSources) {
        for (Map.Entry<StorageNode, DataSource> entry : newDataSources.entrySet()) {
            if (null != entry.getValue()) {
                new DataSourcePoolDestroyer(entry.getValue()).asyncDestroy();
            }
        }
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = metaDataPersistService.getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
    }
    
    private void afterStorageUnitsAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts, final MetaDataContexts reloadMetaDataContexts) {
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseData().get(databaseName))
                .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> metaDataPersistService.getShardingSphereDataPersistService()
                        .persist(originalMetaDataContexts.getMetaData().getDatabase(databaseName), schemaName, schemaData)));
        metaDataPersistService.persistReloadDatabaseByAlter(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    private void afterStorageUnitsDropped(final String databaseName, final MetaDataContexts originalMetaDataContexts, final MetaDataContexts reloadMetaDataContexts) {
        reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getAllSchemas().forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade()
                .getSchema().alterByRuleDropped(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), each));
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseData().get(databaseName))
                .ifPresent(optional -> optional.getSchemaData().forEach((schemaName, schemaData) -> metaDataPersistService.getShardingSphereDataPersistService()
                        .persist(originalMetaDataContexts.getMetaData().getDatabase(databaseName), schemaName, schemaData)));
        metaDataPersistService.persistReloadDatabaseByDrop(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.removeIf(each -> !each.getClass().isAssignableFrom(SingleRuleConfiguration.class));
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(metaDataPersistService.getDatabaseRulePersistService().persist(databaseName, ruleConfigs));
    }
    
    @Override
    public void alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null == toBeAlteredRuleConfig) {
            return;
        }
        MetaDataContexts originalMetaDataContexts = buildOriginalMetaDataContexts();
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService().persist(databaseName, Collections.singleton(toBeAlteredRuleConfig));
        metaDataPersistService.getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
        afterRuleConfigurationAltered(databaseName, originalMetaDataContexts);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void afterRuleConfigurationAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        Thread.sleep(3000L);
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
        metaDataPersistService.persistReloadDatabaseByAlter(
                databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            metaDataPersistService.getDatabaseRulePersistService().delete(databaseName, Collections.singleton(toBeRemovedRuleConfig));
        }
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final String ruleName) {
        MetaDataContexts originalMetaDataContexts = buildOriginalMetaDataContexts();
        metaDataPersistService.getDatabaseRulePersistService().delete(databaseName, ruleName);
        afterRuleConfigurationDropped(databaseName, originalMetaDataContexts);
    }
    
    @SneakyThrows(InterruptedException.class)
    private void afterRuleConfigurationDropped(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        Thread.sleep(3000L);
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
        metaDataPersistService.persistReloadDatabaseByDrop(
                databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), originalMetaDataContexts.getMetaData().getDatabase(databaseName));
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
    public void createTable(final String databaseName, final String schemaName, final ShardingSphereTable table, final String logicDataSourceName) {
        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, schemaName, Collections.singleton(table));
    }
    
    @Override
    public void dropTables(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        tableNames.forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getTable().drop(databaseName, schemaName, each));
    }
    
    private MetaDataContexts buildOriginalMetaDataContexts() {
        ShardingSphereMetaData originalMetaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereStatistics originalStatistics = metaDataContextManager.getMetaDataContexts().getStatistics();
        return new MetaDataContexts(originalMetaData, originalStatistics);
    }
}
