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

package org.apache.shardingsphere.mode.manager.standalone.persist.service;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.changed.RuleItemChangedNodePathBuilder;
import org.apache.shardingsphere.mode.metadata.manager.ActiveVersionChecker;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.MetaDataVersion;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standalone meta data manager persist service.
 */
public final class StandaloneMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    public StandaloneMetaDataManagerPersistService(final MetaDataContextManager metaDataContextManager) {
        this.metaDataContextManager = metaDataContextManager;
        metaDataPersistFacade = metaDataContextManager.getMetaDataPersistFacade();
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        metaDataContextManager.getDatabaseMetaDataManager().addDatabase(databaseName);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistCreatedDatabaseSchemas(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName));
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void dropDatabase(final ShardingSphereDatabase database) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().drop(database.getName());
        metaDataContextManager.getDatabaseMetaDataManager().dropDatabase(database.getName());
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void createSchema(final ShardingSphereDatabase database, final String schemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().add(database.getName(), schemaName);
        metaDataContextManager.getDatabaseMetaDataManager().addSchema(database.getName(), schemaName);
    }
    
    @Override
    public void renameSchema(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().renameSchema(metaDataContextManager.getMetaDataContexts().getMetaData(), database, schemaName, renameSchemaName);
        metaDataContextManager.getDatabaseMetaDataManager().renameSchema(database.getName(), schemaName, renameSchemaName);
    }
    
    @Override
    public void dropSchema(final ShardingSphereDatabase database, final Collection<String> schemaNames) {
        schemaNames.forEach(each -> dropSchema(database.getName(), each));
    }
    
    private void dropSchema(final String databaseName, final String schemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().drop(databaseName, schemaName);
        metaDataContextManager.getDatabaseMetaDataManager().dropSchema(databaseName, schemaName);
    }
    
    @Override
    public void createTable(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereTable table) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, Collections.singleton(table));
        metaDataContextManager.getDatabaseMetaDataManager().alterTable(database.getName(), schemaName, table);
        if (TableRefreshUtils.isSingleTable(table.getName(), database) && TableRefreshUtils.isNeedRefresh(database.getRuleMetaData(), schemaName, table.getName())) {
            alterSingleRuleConfiguration(database, database.getRuleMetaData());
        }
    }
    
    @Override
    public void dropTables(final ShardingSphereDatabase database, final String schemaName, final Collection<String> tableNames) {
        boolean isNeedRefresh = TableRefreshUtils.isNeedRefresh(database.getRuleMetaData(), schemaName, tableNames);
        tableNames.forEach(each -> {
            metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().drop(database.getName(), schemaName, each);
            metaDataContextManager.getDatabaseMetaDataManager().dropTable(database.getName(), schemaName, each);
        });
        if (isNeedRefresh && tableNames.stream().anyMatch(each -> TableRefreshUtils.isSingleTable(each, database))) {
            alterSingleRuleConfiguration(database, database.getRuleMetaData());
        }
    }
    
    @Override
    public void alterTables(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereTable> alteredTables) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, alteredTables);
        alteredTables.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().alterTable(database.getName(), schemaName, each));
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    @Override
    public void alterViews(final ShardingSphereDatabase database, final String schemaName, final Collection<ShardingSphereView> alteredViews) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getView().persist(database.getName(), schemaName, alteredViews);
        alteredViews.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().alterView(database.getName(), schemaName, each));
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    @Override
    public void dropViews(final ShardingSphereDatabase database, final String schemaName, final Collection<String> droppedViews) {
        droppedViews.forEach(each -> {
            metaDataPersistFacade.getDatabaseMetaDataFacade().getView().drop(database.getName(), schemaName, each);
            metaDataContextManager.getDatabaseMetaDataManager().dropView(database.getName(), schemaName, each);
        });
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDataSourceUnitService().persist(databaseName, toBeRegisteredProps);
        afterStorageUnitsRegistered(databaseName, originalMetaDataContexts, toBeRegisteredProps);
        OrderedServicesCache.clearCache();
    }
    
    private void afterStorageUnitsRegistered(final String databaseName, final MetaDataContexts originalMetaDataContexts,
                                             final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        metaDataContextManager.getStorageUnitManager().register(databaseName, toBeRegisteredProps);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistReloadDatabase(databaseName, metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
    }
    
    @Override
    public void alterStorageUnits(final ShardingSphereDatabase database, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) {
        MetaDataContexts originalMetaDataContexts = new MetaDataContexts(metaDataContextManager.getMetaDataContexts().getMetaData(), metaDataContextManager.getMetaDataContexts().getStatistics());
        metaDataPersistFacade.getDataSourceUnitService().persist(database.getName(), toBeUpdatedProps);
        afterStorageUnitsAltered(database.getName(), originalMetaDataContexts, toBeUpdatedProps);
        OrderedServicesCache.clearCache();
    }
    
    private void afterStorageUnitsAltered(final String databaseName, final MetaDataContexts originalMetaDataContexts, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) {
        metaDataContextManager.getStorageUnitManager().alter(databaseName, toBeRegisteredProps);
        metaDataPersistFacade.getDatabaseMetaDataFacade().persistReloadDatabase(databaseName, metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName),
                originalMetaDataContexts.getMetaData().getDatabase(databaseName));
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
    }
    
    @Override
    public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) {
        for (String each : getToBeDroppedResourceNames(database.getName(), toBeDroppedStorageUnitNames)) {
            metaDataPersistFacade.getDataSourceUnitService().delete(database.getName(), each);
            metaDataContextManager.getStorageUnitManager().unregister(database.getName(), each);
            MetaDataContexts reloadMetaDataContexts = metaDataContextManager.getMetaDataContexts();
            metaDataPersistFacade.getDatabaseMetaDataFacade().unregisterStorageUnits(database.getName(), reloadMetaDataContexts);
        }
        OrderedServicesCache.clearCache();
    }
    
    private Collection<String> getToBeDroppedResourceNames(final String databaseName, final Collection<String> toBeDroppedResourceNames) {
        Map<String, DataSourcePoolProperties> propsMap = metaDataPersistFacade.getDataSourceUnitService().load(databaseName);
        return toBeDroppedResourceNames.stream().filter(propsMap::containsKey).collect(Collectors.toList());
    }
    
    @Override
    public void alterSingleRuleConfiguration(final ShardingSphereDatabase database, final RuleMetaData ruleMetaData) {
        SingleRuleConfiguration singleRuleConfig = ruleMetaData.getSingleRule(SingleRule.class).getConfiguration();
        metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(singleRuleConfig));
        try {
            metaDataContextManager.getDatabaseRuleConfigurationManager().refresh(database.getName(), singleRuleConfig, true);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void alterRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null == toBeAlteredRuleConfig) {
            return;
        }
        Collection<String> needReloadTables = getNeedReloadTables(database, toBeAlteredRuleConfig);
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(toBeAlteredRuleConfig));
        alterRuleItem(database.getName(), metaDataVersions);
        Map<String, Collection<ShardingSphereTable>> schemaAndTablesMap = metaDataPersistFacade.getDatabaseMetaDataFacade().persistAlteredTables(
                database.getName(), metaDataContextManager.getMetaDataContexts(), needReloadTables);
        alterSchemaTables(database, schemaAndTablesMap);
        OrderedServicesCache.clearCache();
    }
    
    private void alterRuleItem(final String databaseName, final Collection<MetaDataVersion> metaDataVersions) {
        RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = new RuleItemChangedNodePathBuilder();
        ActiveVersionChecker activeVersionChecker = new ActiveVersionChecker(metaDataPersistFacade.getRepository());
        for (MetaDataVersion each : metaDataVersions) {
            Optional<DatabaseRuleNodePath> databaseRuleNodePath = ruleItemChangedNodePathBuilder.build(databaseName, new VersionNodePath(each.getNodePath()).getActiveVersionPath(), Type.UPDATED);
            if (databaseRuleNodePath.isPresent() && activeVersionChecker.checkSame(new VersionNodePath(databaseRuleNodePath.get()), each.getActiveVersion())) {
                metaDataContextManager.getDatabaseRuleItemManager().alter(databaseRuleNodePath.get());
            }
        }
    }
    
    @Override
    public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleItemConfig) {
        if (null == toBeRemovedRuleItemConfig) {
            return;
        }
        Collection<String> needReloadTables = getNeedReloadTables(database, toBeRemovedRuleItemConfig);
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), Collections.singleton(toBeRemovedRuleItemConfig));
        removeRuleItem(database.getName(), metaDataVersions);
        Map<String, Collection<ShardingSphereTable>> schemaAndTablesMap = metaDataPersistFacade.getDatabaseMetaDataFacade().persistAlteredTables(
                database.getName(), metaDataContextManager.getMetaDataContexts(), needReloadTables);
        alterSchemaTables(database, schemaAndTablesMap);
        OrderedServicesCache.clearCache();
    }
    
    private void removeRuleItem(final String databaseName, final Collection<MetaDataVersion> metaDataVersions) {
        RuleItemChangedNodePathBuilder ruleItemChangedNodePathBuilder = new RuleItemChangedNodePathBuilder();
        for (MetaDataVersion each : metaDataVersions) {
            ruleItemChangedNodePathBuilder.build(databaseName, NodePathGenerator.toPath(each.getNodePath()), Type.DELETED)
                    .ifPresent(optional -> metaDataContextManager.getDatabaseRuleItemManager().drop(optional));
        }
    }
    
    private Collection<String> getNeedReloadTables(final ShardingSphereDatabase originalDatabase, final RuleConfiguration toBeAlteredRuleConfig) {
        if (toBeAlteredRuleConfig instanceof SingleRuleConfiguration) {
            Collection<String> originalSingleTables = originalDatabase.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration().getLogicTableNames();
            return toBeAlteredRuleConfig.getLogicTableNames().stream().filter(each -> !originalSingleTables.contains(each)).collect(Collectors.toList());
        }
        return toBeAlteredRuleConfig.getLogicTableNames();
    }
    
    private void alterSchemaTables(final ShardingSphereDatabase database, final Map<String, Collection<ShardingSphereTable>> schemaAndTablesMap) {
        for (Entry<String, Collection<ShardingSphereTable>> entry : schemaAndTablesMap.entrySet()) {
            for (ShardingSphereTable each : entry.getValue()) {
                metaDataContextManager.getDatabaseMetaDataManager().alterTable(database.getName(), entry.getKey(), each);
            }
        }
    }
    
    @Override
    public void removeRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig, final String ruleType) {
        Collection<String> needReloadTables = getNeedReloadTables(database, toBeRemovedRuleConfig);
        metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), ruleType);
        metaDataContextManager.getDatabaseRuleItemManager().drop(new DatabaseRuleNodePath(database.getName(), ruleType, null));
        Map<String, Collection<ShardingSphereTable>> schemaAndTablesMap = metaDataPersistFacade.getDatabaseMetaDataFacade().persistAlteredTables(
                database.getName(), metaDataContextManager.getMetaDataContexts(), needReloadTables);
        alterSchemaTables(database, schemaAndTablesMap);
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        metaDataPersistFacade.getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
        metaDataContextManager.getGlobalConfigurationManager().alterGlobalRuleConfiguration(toBeAlteredRuleConfig);
        OrderedServicesCache.clearCache();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        metaDataPersistFacade.getPropsService().persist(props);
        metaDataContextManager.getGlobalConfigurationManager().alterProperties(props);
        OrderedServicesCache.clearCache();
    }
}
