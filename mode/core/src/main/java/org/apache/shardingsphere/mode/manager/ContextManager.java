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

package org.apache.shardingsphere.mode.manager;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.infra.state.cluster.ClusterStateContext;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.manager.context.ConfigurationContextManager;
import org.apache.shardingsphere.mode.manager.context.ResourceMetaDataContextManager;
import org.apache.shardingsphere.mode.manager.context.ShardingSphereDatabaseContextManager;
import org.apache.shardingsphere.mode.manager.switcher.ResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final InstanceContext instanceContext;
    
    private final ShardingSphereDatabaseContextManager shardingSphereDatabaseContextManager;
    
    private final ConfigurationContextManager configurationContextManager;
    
    private final ResourceMetaDataContextManager resourceMetaDataContextManager;
    
    private final ExecutorEngine executorEngine;
    
    private final ClusterStateContext clusterStateContext = new ClusterStateContext();
    
    public ContextManager(final MetaDataContexts metaDataContexts, final InstanceContext instanceContext) {
        this.metaDataContexts = new AtomicReference<>(metaDataContexts);
        this.instanceContext = instanceContext;
        shardingSphereDatabaseContextManager = new ShardingSphereDatabaseContextManager(this.metaDataContexts);
        configurationContextManager = new ConfigurationContextManager(this.metaDataContexts, instanceContext);
        resourceMetaDataContextManager = new ResourceMetaDataContextManager(this.metaDataContexts);
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
    }
    
    /**
     * Get meta data contexts.
     * 
     * @return meta data contexts
     */
    public MetaDataContexts getMetaDataContexts() {
        return metaDataContexts.get();
    }
    
    /**
     * Renew meta data contexts.
     * 
     * @param metaDataContexts meta data contexts
     */
    public synchronized void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts.set(metaDataContexts);
    }
    
    /**
     * Get database.
     *
     * @param name database name
     * @return got database
     */
    public ShardingSphereDatabase getDatabase(final String name) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(name), NoDatabaseSelectedException::new);
        ShardingSphereMetaData metaData = getMetaDataContexts().getMetaData();
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(name), () -> new UnknownDatabaseException(name));
        return metaData.getDatabase(name);
    }
    
    /**
     * Get storage units.
     *
     * @param databaseName database name
     * @return storage units
     */
    public Map<String, StorageUnit> getStorageUnits(final String databaseName) {
        return metaDataContexts.get().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits();
    }
    
    /**
     * Reload database meta data.
     *
     * @param database to be reloaded database
     * @param force whether to force refresh table metadata
     */
    public void refreshDatabaseMetaData(final ShardingSphereDatabase database, final boolean force) {
        try {
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(database);
            MetaDataBasedPersistService persistService = metaDataContexts.get().getPersistService();
            if (force) {
                metaDataContexts.set(reloadedMetaDataContexts);
                database.getSchemas().forEach((schemaName, schema) -> persistService.getDatabaseMetaDataService().persist(database.getName(), schemaName, schema));
            } else {
                deletedSchemaNames(database.getName(), reloadedMetaDataContexts.getMetaData().getDatabase(database.getName()), database);
                metaDataContexts.set(reloadedMetaDataContexts);
                database.getSchemas().forEach((schemaName, schema) -> persistService.getDatabaseMetaDataService().compareAndPersist(database.getName(), schemaName, schema));
            }
        } catch (final SQLException ex) {
            log.error("Refresh database meta data: {} failed", database.getName(), ex);
        }
    }
    
    /**
     * Reload table meta data.
     * 
     * @param database to be reloaded database
     */
    public void refreshTableMetaData(final ShardingSphereDatabase database) {
        try {
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(database);
            deletedSchemaNames(database.getName(), database, database);
            metaDataContexts.set(reloadedMetaDataContexts);
            database.getSchemas().forEach((schemaName, schema) -> metaDataContexts.get().getPersistService().getDatabaseMetaDataService().compareAndPersist(database.getName(), schemaName, schema));
        } catch (final SQLException ex) {
            log.error("Refresh table meta data: {} failed", database.getName(), ex);
        }
    }
    
    private MetaDataContexts createMetaDataContexts(final ShardingSphereDatabase database) throws SQLException {
        MetaDataBasedPersistService metaDataPersistService = metaDataContexts.get().getPersistService();
        Map<String, DataSourcePoolProperties> dataSourcePoolPropsFromRegCenter = metaDataPersistService.getDataSourceUnitService().load(database.getName());
        SwitchingResource switchingResource = new ResourceSwitchManager().alterStorageUnit(database.getResourceMetaData(), dataSourcePoolPropsFromRegCenter);
        metaDataContexts.get().getMetaData().getDatabases().putAll(configurationContextManager.renewDatabase(database, switchingResource));
        Collection<RuleConfiguration> ruleConfigs = metaDataPersistService.getDatabaseRulePersistService().load(database.getName());
        Map<String, ShardingSphereDatabase> changedDatabases = configurationContextManager.createChangedDatabases(database.getName(), false, switchingResource, ruleConfigs);
        ConfigurationProperties props = new ConfigurationProperties(metaDataPersistService.getPropsService().load());
        Collection<RuleConfiguration> globalRuleConfigs = metaDataPersistService.getGlobalRuleService().load();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, changedDatabases, props));
        MetaDataContexts result = new MetaDataContexts(metaDataPersistService,
                new ShardingSphereMetaData(changedDatabases, metaDataContexts.get().getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props));
        switchingResource.closeStaleDataSources();
        return result;
    }
    
    /**
     * Delete schema names.
     * 
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void deletedSchemaNames(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        GenericSchemaManager.getToBeDeletedSchemaNames(reloadDatabase.getSchemas(), currentDatabase.getSchemas()).keySet()
                .forEach(each -> metaDataContexts.get().getPersistService().getDatabaseMetaDataService().dropSchema(databaseName, each));
    }
    
    /**
     * Reload schema.
     * 
     * @param database database
     * @param schemaName to be reloaded schema name
     * @param dataSourceName data source name
     */
    public void reloadSchema(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName) {
        try {
            ShardingSphereSchema reloadedSchema = loadSchema(database, schemaName, dataSourceName);
            if (reloadedSchema.getTables().isEmpty()) {
                database.dropSchema(schemaName);
                metaDataContexts.get().getPersistService().getDatabaseMetaDataService().dropSchema(database.getName(),
                        schemaName);
            } else {
                database.addSchema(schemaName, reloadedSchema);
                metaDataContexts.get().getPersistService().getDatabaseMetaDataService()
                        .compareAndPersist(database.getName(), schemaName, reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload meta data of database: {} schema: {} with data source: {} failed", database.getName(), schemaName, dataSourceName, ex);
        }
    }
    
    private ShardingSphereSchema loadSchema(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName) throws SQLException {
        database.reloadRules();
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                Collections.singletonMap(dataSourceName, database.getResourceMetaData().getStorageUnits().get(dataSourceName).getStorageType()),
                Collections.singletonMap(dataSourceName, database.getResourceMetaData().getStorageUnits().get(dataSourceName).getDataSource()),
                database.getRuleMetaData().getRules(), metaDataContexts.get().getMetaData().getProps(), schemaName);
        ShardingSphereSchema result = GenericSchemaBuilder.build(material).get(schemaName);
        result.getViews().putAll(metaDataContexts.get().getPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), schemaName));
        return result;
    }
    
    /**
     * Reload table.
     * 
     * @param database database
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                database.getResourceMetaData().getStorageUnits(), database.getRuleMetaData().getRules(), metaDataContexts.get().getMetaData().getProps(), schemaName);
        try {
            persistTable(database, schemaName, tableName, material);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} failed", tableName, database.getName(), schemaName, ex);
        }
    }
    
    /**
     * Reload table from single data source.
     * 
     * @param database database
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName to be reloaded table name
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName, final String tableName) {
        StorageUnit storageUnit = database.getResourceMetaData().getStorageUnits().get(dataSourceName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getProtocolType(),
                Collections.singletonMap(dataSourceName, storageUnit.getStorageType()), Collections.singletonMap(dataSourceName, storageUnit.getDataSource()),
                database.getRuleMetaData().getRules(), metaDataContexts.get().getMetaData().getProps(), schemaName);
        try {
            persistTable(database, schemaName, tableName, material);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} with data source: {} failed", tableName, database.getName(), schemaName, dataSourceName, ex);
        }
    }
    
    private void persistTable(final ShardingSphereDatabase database, final String schemaName, final String tableName, final GenericSchemaBuilderMaterial material) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singleton(tableName), material).getOrDefault(schemaName, new ShardingSphereSchema());
        metaDataContexts.get().getPersistService().getDatabaseMetaDataService().getTableMetaDataPersistService()
                .persist(database.getName(), schemaName, Collections.singletonMap(tableName, schema.getTable(tableName)));
    }
    
    /**
     * Update cluster state.
     * 
     * @param status status
     */
    public void updateClusterState(final String status) {
        try {
            clusterStateContext.switchState(ClusterState.valueOf(status));
        } catch (final IllegalArgumentException ignore) {
        }
    }
    
    @Override
    public void close() {
        executorEngine.close();
        metaDataContexts.get().close();
    }
}
