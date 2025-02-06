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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.manager.standalone.changed.RuleItemChangedBuilder;
import org.apache.shardingsphere.mode.manager.standalone.changed.executor.type.RuleItemAlteredBuildExecutor;
import org.apache.shardingsphere.mode.manager.standalone.changed.executor.type.RuleItemDroppedBuildExecutor;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Standalone meta data manager persist service.
 */
public final class StandaloneMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    private final RuleItemChangedBuilder ruleItemChangedBuilder;
    
    public StandaloneMetaDataManagerPersistService(final MetaDataContextManager metaDataContextManager) {
        this.metaDataContextManager = metaDataContextManager;
        metaDataPersistFacade = metaDataContextManager.getMetaDataPersistFacade();
        ruleItemChangedBuilder = new RuleItemChangedBuilder();
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        metaDataContextManager.getDatabaseMetaDataManager().addDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void dropDatabase(final ShardingSphereDatabase database) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase().drop(database.getName());
        metaDataContextManager.getDatabaseMetaDataManager().dropDatabase(database.getName());
        clearServiceCache();
    }
    
    @Override
    public void createSchema(final ShardingSphereDatabase database, final String schemaName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().add(database.getName(), schemaName);
        metaDataContextManager.getDatabaseMetaDataManager().addSchema(database.getName(), schemaName);
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
        alteredTables.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().alterTable(database.getName(), schemaName, each));
        alteredViews.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().alterView(database.getName(), schemaName, each));
        droppedTables.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().dropTable(database.getName(), schemaName, each));
        droppedViews.forEach(each -> metaDataContextManager.getDatabaseMetaDataManager().dropView(database.getName(), schemaName, each));
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
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
    }
    
    @Override
    public void dropTable(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().drop(database.getName(), schemaName, tableName);
        metaDataContextManager.getDatabaseMetaDataManager().dropTable(database.getName(), schemaName, tableName);
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager()
                .switchByRegisterStorageUnit(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisteredProps);
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(metaDataPersistFacade, metaDataContextManager.getComputeNodeInstanceContext()).createChangedDatabase(
                databaseName, false, switchingResource, null, metaDataContextManager.getMetaDataContexts());
        metaDataContextManager.getMetaDataContexts().getMetaData().putDatabase(changedDatabase);
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getAllSchemas()
                .forEach(each -> {
                    if (each.isEmpty()) {
                        metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().add(databaseName, each.getName());
                    } else {
                        metaDataPersistFacade.getDatabaseMetaDataFacade().getTable().persist(databaseName, each.getName(), each.getAllTables());
                    }
                });
        DataSourceUnitPersistService dataSourceService = metaDataPersistFacade.getDataSourceUnitService();
        metaDataPersistFacade.getMetaDataVersionService().switchActiveVersion(dataSourceService.persist(databaseName, toBeRegisteredProps));
        clearServiceCache();
    }
    
    @Override
    public void alterStorageUnits(final ShardingSphereDatabase database, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(metaDataContextManager.getMetaDataContexts().getMetaData()
                .getDatabase(database.getName()).getResourceMetaData(), toBeUpdatedProps);
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(metaDataPersistFacade, metaDataContextManager.getComputeNodeInstanceContext()).createChangedDatabase(
                database.getName(), true, switchingResource, null, metaDataContextManager.getMetaDataContexts());
        metaDataContextManager.getMetaDataContexts().getMetaData().putDatabase(changedDatabase);
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        DataSourceUnitPersistService dataSourceService = metaDataPersistFacade.getDataSourceUnitService();
        metaDataPersistFacade.getMetaDataVersionService().switchActiveVersion(dataSourceService.persist(database.getName(), toBeUpdatedProps));
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void unregisterStorageUnits(final ShardingSphereDatabase database, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByUnregisterStorageUnit(metaDataContextManager.getMetaDataContexts().getMetaData()
                .getDatabase(database.getName()).getResourceMetaData(), toBeDroppedStorageUnitNames);
        MetaDataContexts reloadMetaDataContexts = new MetaDataContextsFactory(metaDataPersistFacade, metaDataContextManager.getComputeNodeInstanceContext()).createBySwitchResource(
                database.getName(), false, switchingResource, metaDataContextManager.getMetaDataContexts());
        ShardingSphereDatabase reloadDatabase = reloadMetaDataContexts.getMetaData().getDatabase(database.getName());
        ShardingSphereDatabase currentDatabase = metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(database.getName());
        metaDataPersistFacade.persistReloadDatabaseByDrop(database.getName(), reloadDatabase, currentDatabase);
        GenericSchemaManager.getToBeDroppedSchemaNames(reloadDatabase, currentDatabase).forEach(each -> metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema().drop(database.getName(), each));
        metaDataContextManager.getMetaDataContexts().update(reloadMetaDataContexts);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void alterSingleRuleConfiguration(final ShardingSphereDatabase database, final RuleMetaData ruleMetaData) throws SQLException {
        SingleRuleConfiguration singleRuleConfig = ruleMetaData.getSingleRule(SingleRule.class).getConfiguration();
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(singleRuleConfig));
        metaDataPersistFacade.getMetaDataVersionService().switchActiveVersion(metaDataVersions);
        metaDataContextManager.getDatabaseRuleConfigurationManager().alter(database.getName(), singleRuleConfig);
        clearServiceCache();
    }
    
    @Override
    public void alterRuleConfiguration(final ShardingSphereDatabase database, final RuleConfiguration toBeAlteredRuleConfig) throws SQLException {
        if (null == toBeAlteredRuleConfig) {
            return;
        }
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistFacade.getDatabaseRuleService().persist(database.getName(), Collections.singleton(toBeAlteredRuleConfig));
        metaDataPersistFacade.getMetaDataVersionService().switchActiveVersion(metaDataVersions);
        for (MetaDataVersion each : metaDataVersions) {
            Optional<AlterRuleItem> alterRuleItem = ruleItemChangedBuilder.build(database.getName(), each, new RuleItemAlteredBuildExecutor());
            if (alterRuleItem.isPresent()) {
                metaDataContextManager.getDatabaseRuleItemManager().alter(alterRuleItem.get());
            }
        }
        clearServiceCache();
    }
    
    @Override
    public void removeRuleConfigurationItem(final ShardingSphereDatabase database, final RuleConfiguration toBeRemovedRuleConfig) throws SQLException {
        if (null == toBeRemovedRuleConfig) {
            return;
        }
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), Collections.singleton(toBeRemovedRuleConfig));
        for (MetaDataVersion each : metaDataVersions) {
            Optional<DropRuleItem> dropRuleItem = ruleItemChangedBuilder.build(database.getName(), each, new RuleItemDroppedBuildExecutor());
            if (dropRuleItem.isPresent()) {
                metaDataContextManager.getDatabaseRuleItemManager().drop(dropRuleItem.get());
            }
        }
        clearServiceCache();
    }
    
    @Override
    public void removeRuleConfiguration(final ShardingSphereDatabase database, final String ruleName) {
        metaDataPersistFacade.getDatabaseRuleService().delete(database.getName(), ruleName);
        clearServiceCache();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        metaDataPersistFacade.getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
        metaDataContextManager.getGlobalConfigurationManager().alterGlobalRuleConfiguration(toBeAlteredRuleConfig);
        clearServiceCache();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        metaDataPersistFacade.getPropsService().persist(props);
        metaDataContextManager.getGlobalConfigurationManager().alterProperties(props);
        clearServiceCache();
    }
    
    private void clearServiceCache() {
        OrderedServicesCache.clearCache();
    }
}
