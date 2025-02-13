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
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.manager.cluster.exception.ReloadMetaDataContextFailedException;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

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
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    private final ClusterDatabaseListenerPersistCoordinator clusterDatabaseListenerPersistCoordinator;
    
    public ClusterMetaDataManagerPersistService(final MetaDataContextManager metaDataContextManager, final PersistRepository repository) {
        this.metaDataContextManager = metaDataContextManager;
        metaDataPersistFacade = metaDataContextManager.getMetaDataPersistFacade();
        clusterDatabaseListenerPersistCoordinator = new ClusterDatabaseListenerPersistCoordinator(repository);
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        clusterDatabaseListenerPersistCoordinator.persist(databaseName, ClusterDatabaseListenerCoordinatorType.CREATE);
    }
    
    @Override
    public void dropDatabase(final ShardingSphereDatabase database) {
        clusterDatabaseListenerPersistCoordinator.persist(database.getName(), ClusterDatabaseListenerCoordinatorType.DROP);
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().drop(database.getName());
    }
    
    @Override
    public void createSchema(final ShardingSphereDatabase database, final String schemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().add(database.getName(), schemaName);
    }
    
    @Override
    public void alterSchema(final ShardingSphereDatabase database, final String schemaName,
                            final Collection<ShardingSphereTable> alteredTables, final Collection<ShardingSphereView> alteredViews,
                            final Collection<String> droppedTables, final Collection<String> droppedViews) {
        DatabaseMetaDataPersistFacade databaseMetaDataFacade = metaDataPersistFacade.getDatabaseMetaDataFacade();
        databaseMetaDataFacade.getTable().persist(database.getName(), schemaName, alteredTables);
        databaseMetaDataFacade.getView().persist(database.getName(), schemaName, alteredViews);
        droppedTables.forEach(each -> databaseMetaDataFacade.getTable().drop(database.getName(), schemaName, each));
        droppedViews.forEach(each -> databaseMetaDataFacade.getView().drop(database.getName(), schemaName, each));
    }
    
    @Override
    public void renameSchema(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
        ShardingSphereSchema schema = metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(database.getName()).getSchema(schemaName);
        if (schema.isEmpty()) {
            metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().add(database.getName(), renameSchemaName);
        } else {
            metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(database.getName(), renameSchemaName, schema.getAllTables());
            metaDataPersistFacade.getDatabaseMetaDataFacade().getView().persist(database.getName(), renameSchemaName, schema.getAllViews());
        }
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().drop(database.getName(), schemaName);
    }
    
    @Override
    public void dropSchema(final ShardingSphereDatabase database, final Collection<String> schemaNames) {
        schemaNames.forEach(each -> dropSchema(database.getName(), each));
    }
    
    private void dropSchema(final String databaseName, final String schemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().drop(databaseName, schemaName);
    }
    
    @Override
    public void createTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable table) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, Collections.singleton(table));
    }
    
    @Override
    public void dropTable(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().drop(database.getName(), schemaName, tableName);
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDataSourceUnitService().persist(databaseName, toBeRegisteredProps);
        afterStorageUnitsAltered(databaseName, originalMetaDataContexts);
    }
    
    @Override
    public void alterStorageUnits(final ShardingSphereDatabase database, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDataSourceUnitService().persist(database.getName(), toBeUpdatedProps);
        afterStorageUnitsAltered(database.getName(), originalMetaDataContexts);
    }
    
    @Override
    public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) {
        for (String each : getToBeDroppedResourceNames(database.getName(), toBeDroppedStorageUnitNames)) {
            MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
            metaDataPersistFacade.getDataSourceUnitService().delete(database.getName(), each);
            afterStorageUnitsDropped(database.getName(), originalMetaDataContexts);
        }
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = metaDataPersistFacade.getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
    }
    
    private void afterStorageUnitsAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        MetaDataContexts reloadMetaDataContexts = getReloadMetaDataContexts(originalMetaDataContexts);
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseStatistics(databaseName))
                .ifPresent(optional -> optional.getSchemaStatisticsMap().forEach((schemaName, schemaStatistics) -> metaDataPersistFacade.getStatisticsService()
                        .persist(originalMetaDataContexts.getMetaData().getDatabase(databaseName), schemaName, schemaStatistics)));
        metaDataPersistFacade.persistReloadDatabaseByAlter(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    private void afterStorageUnitsDropped(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        MetaDataContexts reloadMetaDataContexts = getReloadMetaDataContexts(originalMetaDataContexts);
        reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getAllSchemas().forEach(each -> metaDataPersistFacade.getDatabaseMetaDataFacade()
                .getSchema().alterByRuleDropped(reloadMetaDataContexts.getMetaData().getDatabase(databaseName).getName(), each));
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseStatistics(databaseName))
                .ifPresent(optional -> optional.getSchemaStatisticsMap().forEach((schemaName, schemaStatistics) -> metaDataPersistFacade.getStatisticsService()
                        .persist(originalMetaDataContexts.getMetaData().getDatabase(databaseName), schemaName, schemaStatistics)));
        metaDataPersistFacade.persistReloadDatabaseByDrop(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void alterSingleRuleConfiguration(final ShardingSphereDatabase database, final RuleMetaData ruleMetaData) {
        SingleRuleConfiguration singleRuleConfig = ruleMetaData.getSingleRule(SingleRule.class).getConfiguration();
        metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(singleRuleConfig));
    }
    
    @Override
    public void alterRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null == toBeAlteredRuleConfig) {
            return;
        }
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(toBeAlteredRuleConfig));
        afterRuleConfigurationAltered(database.getName(), originalMetaDataContexts);
    }
    
    private void afterRuleConfigurationAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        MetaDataContexts reloadMetaDataContexts = getReloadMetaDataContexts(originalMetaDataContexts);
        metaDataPersistFacade.persistReloadDatabaseByAlter(
                databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), Collections.singleton(toBeRemovedRuleConfig));
        }
    }
    
    @Override
    public void removeRuleConfiguration(final ShardingSphereDatabase database, final String ruleType) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), ruleType);
        afterRuleConfigurationDropped(database.getName(), originalMetaDataContexts);
    }
    
    private void afterRuleConfigurationDropped(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        MetaDataContexts reloadMetaDataContexts = getReloadMetaDataContexts(originalMetaDataContexts);
        metaDataPersistFacade.persistReloadDatabaseByDrop(
                databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        metaDataPersistFacade.getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
    }
    
    @Override
    public void alterProperties(final Properties props) {
        metaDataPersistFacade.getPropsService().persist(props);
    }
    
    @SneakyThrows(InterruptedException.class)
    private MetaDataContexts getReloadMetaDataContexts(final MetaDataContexts originalMetaDataContexts) {
        Thread.sleep(3000L);
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
        if (reloadMetaDataContexts != originalMetaDataContexts) {
            return reloadMetaDataContexts;
        }
        long startTime = System.currentTimeMillis();
        long timeout = 30000;
        while (System.currentTimeMillis() - startTime < timeout) {
            reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
            if (reloadMetaDataContexts != originalMetaDataContexts) {
                return reloadMetaDataContexts;
            }
            Thread.sleep(1000L);
        }
        throw new ReloadMetaDataContextFailedException();
    }
}
