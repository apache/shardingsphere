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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.manager.DatabaseRuleConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.GlobalConfigurationManager;
import org.apache.shardingsphere.mode.metadata.manager.ResourceSwitchManager;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemManager;
import org.apache.shardingsphere.mode.metadata.manager.SchemaMetaDataManager;
import org.apache.shardingsphere.mode.metadata.manager.ShardingSphereDatabaseDataManager;
import org.apache.shardingsphere.mode.metadata.manager.StorageUnitManager;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Meta data context manager..
 */
@Getter
@Slf4j
public class MetaDataContextManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ShardingSphereDatabaseDataManager databaseManager;
    
    private final SchemaMetaDataManager schemaMetaDataManager;
    
    private final RuleItemManager ruleItemManager;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final StorageUnitManager storageUnitManager;
    
    private final DatabaseRuleConfigurationManager databaseRuleConfigurationManager;
    
    private final GlobalConfigurationManager globalConfigurationManager;
    
    public MetaDataContextManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext, final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        resourceSwitchManager = new ResourceSwitchManager();
        databaseManager = new ShardingSphereDatabaseDataManager(metaDataContexts);
        storageUnitManager = new StorageUnitManager(metaDataContexts, computeNodeInstanceContext, repository, resourceSwitchManager);
        databaseRuleConfigurationManager = new DatabaseRuleConfigurationManager(metaDataContexts, computeNodeInstanceContext, repository);
        schemaMetaDataManager = new SchemaMetaDataManager(metaDataContexts, repository);
        ruleItemManager = new RuleItemManager(metaDataContexts, repository, databaseRuleConfigurationManager);
        globalConfigurationManager = new GlobalConfigurationManager(metaDataContexts, repository);
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Drop schemas.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void dropSchemas(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        GenericSchemaManager.getToBeDroppedSchemaNames(reloadDatabase, currentDatabase).forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getSchema().drop(databaseName, each));
    }
    
    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    public void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts.update(metaDataContexts);
    }
    
    /**
     * Force refresh database meta data.
     *
     * @param database to be reloaded database
     */
    public void forceRefreshDatabaseMetaData(final ShardingSphereDatabase database) {
        try {
            metaDataContexts.update(createMetaDataContexts(database));
            metaDataContexts.getMetaData().getDatabase(database.getName()).getAllSchemas()
                    .forEach(each -> {
                        if (each.isEmpty()) {
                            metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(database.getName(), each.getName());
                        }
                        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(database.getName(), each.getName(), each.getAllTables());
                    });
        } catch (final SQLException ex) {
            log.error("Refresh database meta data: {} failed", database.getName(), ex);
        }
    }
    
    /**
     * Refresh table meta data.
     *
     * @param database to be reloaded database
     */
    public void refreshTableMetaData(final ShardingSphereDatabase database) {
        try {
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(database);
            dropSchemas(database.getName(), reloadedMetaDataContexts.getMetaData().getDatabase(database.getName()), database);
            metaDataContexts.update(reloadedMetaDataContexts);
            metaDataContexts.getMetaData().getDatabase(database.getName()).getAllSchemas()
                    .forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getSchema().alterByRefresh(database.getName(), each));
        } catch (final SQLException ex) {
            log.error("Refresh table meta data: {} failed", database.getName(), ex);
        }
    }
    
    private MetaDataContexts createMetaDataContexts(final ShardingSphereDatabase database) throws SQLException {
        Map<String, DataSourcePoolProperties> dataSourcePoolPropsFromRegCenter = metaDataPersistService.getDataSourceUnitService().load(database.getName());
        SwitchingResource switchingResource = resourceSwitchManager.switchByAlterStorageUnit(database.getResourceMetaData(), dataSourcePoolPropsFromRegCenter);
        Collection<RuleConfiguration> ruleConfigs = metaDataPersistService.getDatabaseRulePersistService().load(database.getName());
        ShardingSphereDatabase changedDatabase = MetaDataContextsFactory
                .createChangedDatabase(database.getName(), false, switchingResource, ruleConfigs, metaDataContexts, metaDataPersistService, computeNodeInstanceContext);
        metaDataContexts.getMetaData().putDatabase(changedDatabase);
        ConfigurationProperties props = new ConfigurationProperties(metaDataPersistService.getPropsService().load());
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, metaDataContexts.getMetaData().getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getAllDatabases(), metaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        MetaDataContexts result = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaDataPersistService, metaData));
        switchingResource.closeStaleDataSources();
        return result;
    }
}
