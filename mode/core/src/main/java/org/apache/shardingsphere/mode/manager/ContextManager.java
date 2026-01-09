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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListenerFactory;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.state.StateContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ExclusiveOperatorEngine exclusiveOperatorEngine;
    
    private final ExecutorEngine executorEngine;
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final PersistServiceFacade persistServiceFacade;
    
    private final StateContext stateContext;
    
    public ContextManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext, final ExclusiveOperatorEngine exclusiveOperatorEngine,
                          final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        this.exclusiveOperatorEngine = exclusiveOperatorEngine;
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        metaDataContextManager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, repository);
        persistServiceFacade = new PersistServiceFacade(repository, computeNodeInstanceContext.getModeConfiguration(), metaDataContextManager);
        stateContext = new StateContext(persistServiceFacade.getStateService().load());
        ContextManagerLifecycleListenerFactory.getListeners(this).forEach(each -> each.onInitialized(this));
    }
    
    /**
     * Get database type.
     *
     * @return database type
     */
    public DatabaseType getDatabaseType() {
        Collection<ShardingSphereDatabase> databases = metaDataContexts.getMetaData().getAllDatabases();
        return databases.stream().flatMap(each -> each.getResourceMetaData().getStorageUnits().values().stream()).findFirst().map(StorageUnit::getStorageType)
                .orElseGet(DatabaseTypeEngine::getDefaultStorageType);
    }
    
    /**
     * Get all database names.
     *
     * @return all database names
     */
    public Collection<String> getAllDatabaseNames() {
        return metaDataContexts.getMetaData().getAllDatabases().stream().map(ShardingSphereDatabase::getName).collect(Collectors.toList());
    }
    
    /**
     * Get database.
     *
     * @param name database name
     * @return got database
     */
    public ShardingSphereDatabase getDatabase(final String name) {
        ShardingSpherePreconditions.checkNotEmpty(name, NoDatabaseSelectedException::new);
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData();
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
        return getDatabase(databaseName).getResourceMetaData().getStorageUnits();
    }
    
    /**
     * Reload database.
     *
     * @param database to be reloaded database
     */
    public void reloadDatabase(final ShardingSphereDatabase database) {
        try {
            MetaDataContexts reloadedMetaDataContexts = createMetaDataContexts(database);
            dropSchemas(database.getName(), reloadedMetaDataContexts.getMetaData().getDatabase(database.getName()), database);
            metaDataContexts.update(reloadedMetaDataContexts);
            metaDataContexts.getMetaData().getDatabase(database.getName()).getAllSchemas()
                    .forEach(each -> persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema().alterByRefresh(database.getName(), each));
        } catch (final SQLException ex) {
            log.error("Refresh database meta data: {} failed", database.getName(), ex);
        }
    }
    
    private MetaDataContexts createMetaDataContexts(final ShardingSphereDatabase database) throws SQLException {
        Map<String, DataSourcePoolProperties> dataSourcePoolProps = persistServiceFacade.getMetaDataFacade().getDataSourceUnitService().load(database.getName());
        boolean isInstanceConnectionEnabled = metaDataContexts.getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED);
        SwitchingResource switchingResource =
                metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(database.getResourceMetaData(), dataSourcePoolProps, isInstanceConnectionEnabled);
        Collection<RuleConfiguration> ruleConfigs = persistServiceFacade.getMetaDataFacade().getDatabaseRuleService().load(database.getName());
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(persistServiceFacade.getMetaDataFacade(), computeNodeInstanceContext)
                .createChangedDatabase(database.getName(), false, switchingResource, ruleConfigs, metaDataContexts);
        metaDataContexts.getMetaData().putDatabase(changedDatabase);
        ConfigurationProperties props = new ConfigurationProperties(persistServiceFacade.getMetaDataFacade().getPropsService().load());
        Collection<RuleConfiguration> globalRuleConfigs = persistServiceFacade.getMetaDataFacade().getGlobalRuleService().load();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, metaDataContexts.getMetaData().getAllDatabases(), props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                metaDataContexts.getMetaData().getAllDatabases(), metaDataContexts.getMetaData().getGlobalResourceMetaData(), changedGlobalMetaData, props);
        MetaDataContexts result =
                new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistServiceFacade.getMetaDataFacade().getStatisticsService().load(metaData)));
        switchingResource.closeStaleDataSources();
        return result;
    }
    
    private void dropSchemas(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        GenericSchemaManager.getToBeDroppedSchemaNames(reloadDatabase, currentDatabase)
                .forEach(each -> persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema().drop(databaseName, each));
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
            if (reloadedSchema.getAllTables().isEmpty()) {
                database.dropSchema(schemaName);
                persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema().drop(database.getName(), schemaName);
            } else {
                database.addSchema(reloadedSchema);
                persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema().alterByRefresh(database.getName(), reloadedSchema);
            }
        } catch (final SQLException ex) {
            log.error("Reload meta data of database: {} schema: {} with data source: {} failed", database.getName(), schemaName, dataSourceName, ex);
        }
    }
    
    private ShardingSphereSchema loadSchema(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName) throws SQLException {
        database.reloadRules();
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(Collections.singletonMap(dataSourceName, database.getResourceMetaData().getStorageUnits().get(dataSourceName)),
                database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        ShardingSphereSchema result = GenericSchemaBuilder.build(database.getProtocolType(), material).get(schemaName);
        persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getView().load(database.getName(), schemaName).forEach(result::putView);
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
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                database.getResourceMetaData().getStorageUnits(), database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
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
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                Collections.singletonMap(dataSourceName, storageUnit), database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName);
        try {
            persistTable(database, schemaName, tableName, material);
        } catch (final SQLException ex) {
            log.error("Reload table: {} meta data of database: {} schema: {} with data source: {} failed", tableName, database.getName(), schemaName, dataSourceName, ex);
        }
    }
    
    private void persistTable(final ShardingSphereDatabase database, final String schemaName, final String tableName, final GenericSchemaBuilderMaterial material) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.build(Collections.singleton(tableName), database.getProtocolType(), material).getOrDefault(schemaName, new ShardingSphereSchema(schemaName));
        if (schema.containsTable(tableName)) {
            persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, Collections.singleton(schema.getTable(tableName)));
        } else {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().dropTables(database, schemaName, Collections.singleton(tableName));
        }
    }
    
    /**
     * Get pre-selected database name.
     *
     * @return pre-selected database name
     */
    public String getPreSelectedDatabaseName() {
        return InstanceType.JDBC == computeNodeInstanceContext.getInstance().getMetaData().getType() ? metaDataContexts.getMetaData().getAllDatabases().iterator().next().getName() : null;
    }
    
    @Override
    public void close() {
        ContextManagerLifecycleListenerFactory.getListeners(this).forEach(each -> each.onDestroyed(this));
        executorEngine.close();
        metaDataContexts.getMetaData().close();
        persistServiceFacade.close();
    }
}
