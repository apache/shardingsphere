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
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.state.StateContext;

import java.sql.SQLException;
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
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ExecutorEngine executorEngine;
    
    private final StateContext stateContext;
    
    private final PersistServiceFacade persistServiceFacade;
    
    private final MetaDataContextManager metaDataContextManager;
    
    public ContextManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext, final PersistRepository repository) {
        this.metaDataContexts = new AtomicReference<>(metaDataContexts);
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        metaDataContextManager = new MetaDataContextManager(this.metaDataContexts, computeNodeInstanceContext, repository);
        persistServiceFacade = new PersistServiceFacade(repository, computeNodeInstanceContext.getModeConfiguration(), metaDataContextManager);
        stateContext = new StateContext(persistServiceFacade.getStatePersistService().loadClusterState().orElse(ClusterState.OK));
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        for (ContextManagerLifecycleListener each : ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)) {
            each.onInitialized(this);
        }
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
    public void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts.set(metaDataContexts);
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
            if (reloadedSchema.getTables().isEmpty()) {
                database.dropSchema(schemaName);
                persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataService().dropSchema(database.getName(),
                        schemaName);
            } else {
                database.addSchema(schemaName, reloadedSchema);
                persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataService()
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
        result.getViews().putAll(persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataService().getViewMetaDataPersistService().load(database.getName(), schemaName));
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
        persistServiceFacade.getMetaDataPersistService().getDatabaseMetaDataService().getTableMetaDataPersistService()
                .persist(database.getName(), schemaName, Collections.singletonMap(tableName, schema.getTable(tableName)));
    }
    
    /**
     * Get pre-selected database name.
     *
     * @return pre-selected database name
     */
    public String getPreSelectedDatabaseName() {
        return InstanceType.JDBC == computeNodeInstanceContext.getInstance().getMetaData().getType() ? metaDataContexts.get().getMetaData().getDatabases().keySet().iterator().next() : null;
    }
    
    @Override
    public void close() {
        for (ContextManagerLifecycleListener each : ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class)) {
            each.onDestroyed(this);
        }
        executorEngine.close();
        metaDataContexts.get().close();
        persistServiceFacade.getComputeNodePersistService().offline(computeNodeInstanceContext.getInstance());
        persistServiceFacade.getRepository().close();
    }
}
