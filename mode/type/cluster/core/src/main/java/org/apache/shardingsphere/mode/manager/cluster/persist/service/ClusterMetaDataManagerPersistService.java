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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.mode.manager.cluster.exception.ReloadMetaDataContextFailedException;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.retry.RetryExecutor;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
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
    
    public ClusterMetaDataManagerPersistService(final MetaDataContextManager metaDataContextManager, final ClusterPersistRepository repository) {
        this.metaDataContextManager = metaDataContextManager;
        metaDataPersistFacade = metaDataContextManager.getMetaDataPersistFacade();
        clusterDatabaseListenerPersistCoordinator = new ClusterDatabaseListenerPersistCoordinator(repository);
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        clusterDatabaseListenerPersistCoordinator.persist(databaseName, ClusterDatabaseListenerCoordinatorType.CREATE);
        ShardingSphereDatabase reloadDatabase = getReloadedMetaDataContexts(originalMetaDataContexts).getMetaData().getDatabase(databaseName);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistCreatedDatabaseSchemas(reloadDatabase);
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
    public void renameSchema(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().renameSchema(metaDataContextManager.getMetaDataContexts().getMetaData(), database, schemaName, renameSchemaName);
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
        if (TableRefreshUtils.isSingleTable(table.getName(), database) && TableRefreshUtils.isNeedRefresh(database.getRuleMetaData(), schemaName, table.getName())) {
            alterSingleRuleConfiguration(database, database.getRuleMetaData());
        }
    }
    
    @Override
    public void dropTables(final ShardingSphereDatabase database, final String schemaName, final Collection<String> tableNames) {
        boolean isNeedRefresh = TableRefreshUtils.isNeedRefresh(database.getRuleMetaData(), schemaName, tableNames);
        tableNames.forEach(each -> metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().drop(database.getName(), schemaName, each));
        if (isNeedRefresh && tableNames.stream().anyMatch(each -> TableRefreshUtils.isSingleTable(each, database))) {
            alterSingleRuleConfiguration(database, database.getRuleMetaData());
        }
    }
    
    @Override
    public void alterTables(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereTable> alteredTables) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, alteredTables);
    }
    
    @Override
    public void alterViews(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereView> alteredViews) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getView().persist(database.getName(), schemaName, alteredViews);
    }
    
    @Override
    public void dropViews(final ShardingSphereDatabase database, final String schemaName, final Collection<String> droppedViews) {
        droppedViews.forEach(each -> metaDataPersistFacade.getDatabaseMetaDataFacade().getView().drop(database.getName(), schemaName, each));
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
    
    private void afterStorageUnitsAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts) {
        MetaDataContexts reloadMetaDataContexts = getReloadedMetaDataContexts(originalMetaDataContexts);
        Optional.ofNullable(reloadMetaDataContexts.getStatistics().getDatabaseStatistics(databaseName))
                .ifPresent(optional -> optional.getSchemaStatisticsMap().forEach((schemaName, schemaStatistics) -> metaDataPersistFacade.getStatisticsService()
                        .persist(originalMetaDataContexts.getMetaData().getDatabase(databaseName), schemaName, schemaStatistics)));
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistReloadDatabase(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
    }
    
    @Override
    public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) {
        for (String each : getToBeDroppedResourceNames(database.getName(), toBeDroppedStorageUnitNames)) {
            String databaseName = database.getName();
            MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
            metaDataPersistFacade.getDataSourceUnitService().delete(database.getName(), each);
            MetaDataContexts reloadMetaDataContexts = getReloadedMetaDataContexts(originalMetaDataContexts);
            metaDataPersistFacade.getDatabaseMetaDataFacade().persistReloadDatabase(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                    originalMetaDataContexts.getMetaData().getDatabase(databaseName));
            DatabaseStatistics databaseStatistics = reloadMetaDataContexts.getStatistics().getDatabaseStatistics(database.getName());
            if (null != databaseStatistics) {
                for (Entry<String, SchemaStatistics> entry : databaseStatistics.getSchemaStatisticsMap().entrySet()) {
                    metaDataPersistFacade.getStatisticsService().persist(originalMetaDataContexts.getMetaData().getDatabase(database.getName()), entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = metaDataPersistFacade.getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
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
        MetaDataContexts reloadMetaDataContexts = getReloadedMetaDataContexts(originalMetaDataContexts);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistReloadDatabase(
                database.getName(), reloadMetaDataContexts.getMetaData().getDatabase(database.getName()), originalMetaDataContexts.getMetaData().getDatabase(database.getName()));
    }
    
    @Override
    public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleItemConfig) {
        if (null == toBeRemovedRuleItemConfig) {
            return;
        }
        Collection<String> needReloadTables = getNeedReloadTables(database, toBeRemovedRuleItemConfig);
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), Collections.singleton(toBeRemovedRuleItemConfig));
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistAlteredTables(database.getName(), getReloadedMetaDataContexts(originalMetaDataContexts), needReloadTables);
    }
    
    private Collection<String> getNeedReloadTables(final ShardingSphereDatabase originalDatabase, final RuleConfiguration toBeAlteredRuleConfig) {
        if (toBeAlteredRuleConfig instanceof SingleRuleConfiguration) {
            Collection<String> originalSingleTables = originalDatabase.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration().getLogicTableNames();
            return toBeAlteredRuleConfig.getLogicTableNames().stream().filter(each -> !originalSingleTables.contains(each)).collect(Collectors.toList());
        }
        return toBeAlteredRuleConfig.getLogicTableNames();
    }
    
    @Override
    public void removeRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig, final String ruleType) {
        Collection<String> needReloadTables = getNeedReloadTables(database, toBeRemovedRuleConfig);
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), ruleType);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistAlteredTables(database.getName(), getReloadedMetaDataContexts(originalMetaDataContexts), needReloadTables);
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
    private MetaDataContexts getReloadedMetaDataContexts(final MetaDataContexts originalMetaDataContexts) {
        Thread.sleep(3000L);
        MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
        if (reloadMetaDataContexts.getMetaData() != originalMetaDataContexts.getMetaData() && reloadMetaDataContexts.getStatistics() != originalMetaDataContexts.getStatistics()) {
            return reloadMetaDataContexts;
        }
        RetryExecutor retryExecutor = new RetryExecutor(30000L, 1000L);
        ShardingSpherePreconditions.checkState(retryExecutor.execute(arg -> metaDataContextManager.getMetaDataContexts() != arg, originalMetaDataContexts), ReloadMetaDataContextFailedException::new);
        return metaDataContextManager.getMetaDataContexts();
    }
}
