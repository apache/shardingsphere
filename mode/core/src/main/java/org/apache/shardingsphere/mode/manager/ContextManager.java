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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNodes;
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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.exception.LoadTableMetaDataFailedException;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListenerFactory;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.state.StateContext;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Context manager.
 */
@Getter
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
        ShardingSphereDatabase result = metaData.getDatabase(name);
        ShardingSpherePreconditions.checkState(null != result, () -> new UnknownDatabaseException(name));
        return result;
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
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadDatabase(final ShardingSphereDatabase database) {
        Map<String, DataSourcePoolProperties> dataSourcePoolProps = persistServiceFacade.getMetaDataFacade().getDataSourceUnitService().load(database.getName());
        boolean isInstanceConnectionEnabled = metaDataContexts.getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED);
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager()
                .switchByAlterStorageUnit(database.getResourceMetaData(), dataSourcePoolProps, isInstanceConnectionEnabled);
        MetaDataContexts reloadedMetaDataContexts;
        boolean loaded = false;
        try {
            reloadedMetaDataContexts = createMetaDataContexts(database, switchingResource);
            dropSchemas(database.getName(), reloadedMetaDataContexts.getMetaData().getDatabase(database.getName()), database);
            loaded = true;
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(database.getName(), ex);
        } finally {
            if (!loaded) {
                switchingResource.closeNewDataSources();
            }
        }
        metaDataContexts.update(reloadedMetaDataContexts);
        switchingResource.closeStaleDataSources();
        metaDataContexts.getMetaData().getDatabase(database.getName()).getAllSchemas()
                .forEach(each -> persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema().alterByRefresh(database.getName(), each));
    }
    
    private MetaDataContexts createMetaDataContexts(final ShardingSphereDatabase database, final SwitchingResource switchingResource) throws SQLException {
        Collection<RuleConfiguration> ruleConfigs = persistServiceFacade.getMetaDataFacade().getDatabaseRuleService().load(database.getName());
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(persistServiceFacade.getMetaDataFacade(), computeNodeInstanceContext)
                .createChangedDatabaseByRebuild(database.getName(), switchingResource, ruleConfigs, metaDataContexts);
        Collection<ShardingSphereDatabase> databases = metaDataContexts.getMetaData().getAllDatabases().stream()
                .filter(each -> !each.getName().equals(database.getName())).collect(Collectors.toList());
        databases.add(changedDatabase);
        ConfigurationProperties props = new ConfigurationProperties(persistServiceFacade.getMetaDataFacade().getPropsService().load());
        Collection<RuleConfiguration> globalRuleConfigs = persistServiceFacade.getMetaDataFacade().getGlobalRuleService().load();
        RuleMetaData changedGlobalMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, metaDataContexts.getMetaData().getGlobalResourceMetaData(),
                changedGlobalMetaData, props, metaDataContexts.getMetaData().getProtocolType());
        return new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, persistServiceFacade.getMetaDataFacade().getStatisticsService().load(metaData)));
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
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadSchema(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName) {
        ShardingSphereSchema reloadedSchema;
        try {
            reloadedSchema = loadSchema(database, schemaName, dataSourceName);
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(database.getName(), ex);
        }
        database.reloadRules();
        if (reloadedSchema.isEmpty()) {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().dropSchema(database, Collections.singleton(schemaName));
            return;
        }
        ShardingSphereSchema currentSchema = database.getSchema(schemaName);
        Collection<String> droppedTables = GenericSchemaManager.getToBeDroppedTables(reloadedSchema, currentSchema).stream().map(ShardingSphereTable::getName).collect(Collectors.toList());
        Collection<ShardingSphereTable> alteredTables = GenericSchemaManager.getToBeAddedTables(reloadedSchema, currentSchema);
        if (!droppedTables.isEmpty()) {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().dropTables(database, schemaName, droppedTables);
        }
        if (!alteredTables.isEmpty()) {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().alterTables(database, schemaName, alteredTables);
        }
    }
    
    private ShardingSphereSchema loadSchema(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName) throws SQLException {
        ShardingSphereDatabase candidate = new ShardingSphereDatabase(database.getName(), database.getProtocolType(), database.getResourceMetaData(),
                database.getRuleMetaData(), Collections.emptyList(), metaDataContexts.getMetaData().getProps());
        candidate.reloadRules();
        Set<String> tableNames = new LinkedHashSet<>(getTableNames(database, dataSourceName));
        tableNames.addAll(getTableNames(candidate, dataSourceName));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(getRefreshStorageUnits(database, dataSourceName), candidate.getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getProps(), schemaName, database.getIdentifierContext(), database.getAllSchemas());
        ShardingSphereSchema reloadedSchema = GenericSchemaBuilder.buildForRefresh(tableNames, database.getProtocolType(), material)
                .getOrDefault(schemaName, new ShardingSphereSchema(schemaName, database.getProtocolType()));
        ShardingSphereSchema currentSchema = database.getSchema(schemaName);
        Collection<ShardingSphereTable> tables = new LinkedList<>(currentSchema.getAllTables());
        tableNames.forEach(each -> tables.remove(currentSchema.getTable(each)));
        tables.addAll(reloadedSchema.getAllTables());
        return new ShardingSphereSchema(schemaName, database.getProtocolType(), tables, currentSchema.getAllViews());
    }
    
    private Collection<String> getTableNames(final ShardingSphereDatabase database, final String dataSourceName) {
        Set<String> result = new LinkedHashSet<>();
        database.getRuleMetaData().getRules().forEach(each -> each.getAttributes().findAttribute(TableMapperRuleAttribute.class).ifPresent(attribute -> result.addAll(attribute.getLogicTableNames())));
        DataNodes dataNodes = new DataNodes(database.getRuleMetaData().getRules());
        return result.stream().filter(each -> dataNodes.getDataNodes(each).isEmpty()
                || dataNodes.getDataNodes(each).stream().anyMatch(dataNode -> dataSourceName.equals(dataNode.getDataSourceName()))).collect(Collectors.toList());
    }
    
    private Map<String, StorageUnit> getRefreshStorageUnits(final ShardingSphereDatabase database, final String dataSourceName) {
        Map<String, StorageUnit> result = new LinkedHashMap<>(database.getResourceMetaData().getStorageUnits().size(), 1F);
        result.put(dataSourceName, database.getResourceMetaData().getStorageUnits().get(dataSourceName));
        result.putAll(database.getResourceMetaData().getStorageUnits());
        return result;
    }
    
    /**
     * Reload table.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final String tableName) {
        reloadTable(database, schemaName, new IdentifierValue(tableName));
    }
    
    /**
     * Reload table.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableName to be reloaded table name
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final IdentifierValue tableName) {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                database.getResourceMetaData().getStorageUnits(), database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName,
                database.getIdentifierContext(), database.getAllSchemas());
        try {
            persistTable(database, schemaName, tableName, material);
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(database.getName(), Collections.singleton(tableName.getValue()), ex);
        }
    }
    
    /**
     * Reload table from single data source.
     *
     * @param database database
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName to be reloaded table name
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName, final String tableName) {
        reloadTable(database, schemaName, dataSourceName, new IdentifierValue(tableName));
    }
    
    /**
     * Reload table from single data source.
     *
     * @param database database
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param tableName to be reloaded table name
     * @throws LoadTableMetaDataFailedException if loading physical table metadata fails
     */
    public void reloadTable(final ShardingSphereDatabase database, final String schemaName, final String dataSourceName, final IdentifierValue tableName) {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                getRefreshStorageUnits(database, dataSourceName), database.getRuleMetaData().getRules(), metaDataContexts.getMetaData().getProps(), schemaName,
                database.getIdentifierContext(), database.getAllSchemas());
        try {
            persistTable(database, schemaName, tableName, material);
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(database.getName(), Collections.singleton(tableName.getValue()), ex);
        }
    }
    
    private void persistTable(final ShardingSphereDatabase database, final String schemaName, final IdentifierValue tableName, final GenericSchemaBuilderMaterial material) throws SQLException {
        ShardingSphereSchema schema = GenericSchemaBuilder.buildForRefresh(Collections.singleton(tableName.getValue()), database.getProtocolType(), material)
                .getOrDefault(schemaName, new ShardingSphereSchema(schemaName, database.getProtocolType()));
        if (schema.containsTable(tableName)) {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().alterTables(database, schemaName, Collections.singleton(schema.getTable(tableName)));
        } else {
            persistServiceFacade.getModeFacade().getMetaDataManagerService().dropTables(database, schemaName, Collections.singleton(tableName.getValue()));
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
