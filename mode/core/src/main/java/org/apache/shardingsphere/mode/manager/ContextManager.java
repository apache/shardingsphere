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
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.persist.coordinator.PersistCoordinatorFacade;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.state.ClusterStateContext;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Context manager.
 */
@Getter
@Slf4j
public final class ContextManager implements AutoCloseable {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ExecutorEngine executorEngine;
    
    private final ClusterStateContext stateContext;
    
    private final PersistServiceFacade persistServiceFacade;
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final PersistCoordinatorFacade persistCoordinatorFacade;
    
    public ContextManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext, final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataContextManager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, repository);
        persistServiceFacade = new PersistServiceFacade(repository, computeNodeInstanceContext.getModeConfiguration(), metaDataContextManager);
        stateContext = new ClusterStateContext(persistServiceFacade.getStatePersistService().load());
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        for (ContextManagerLifecycleListener each : ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)) {
            each.onInitialized(this);
        }
        persistCoordinatorFacade = new PersistCoordinatorFacade(repository, computeNodeInstanceContext.getModeConfiguration());
    }
    
    /**
     * Get database.
     *
     * @param name database name
     * @return got database
     */
    public ShardingSphereDatabase getDatabase(final String name) {
        ShardingSpherePreconditions.checkNotEmpty(name, NoDatabaseSelectedException::new);
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
        return getDatabase(databaseName).getResourceMetaData().getStorageUnits();
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
                persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataFacade().getSchema().drop(database.getName(), schemaName);
            } else {
                database.addSchema(reloadedSchema);
                persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataFacade().getSchema().alterByRefresh(database.getName(), reloadedSchema);
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
        persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataFacade().getView().load(database.getName(), schemaName).forEach(result::putView);
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
        persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataFacade().getTable().persist(database.getName(), schemaName, Collections.singleton(schema.getTable(tableName)));
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
        for (ContextManagerLifecycleListener each : ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)) {
            each.onDestroyed(this);
        }
        executorEngine.close();
        metaDataContexts.getMetaData().close();
        persistServiceFacade.close(computeNodeInstanceContext.getInstance());
    }
}
