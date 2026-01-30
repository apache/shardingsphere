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

package org.apache.shardingsphere.mode.metadata.manager.resource;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Storage unit manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class StorageUnitManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    /**
     * Register storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void register(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        try {
            closeStaleRules(database);
            boolean isInstanceConnectionEnabled = metaDataContexts.getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED);
            SwitchingResource switchingResource = resourceSwitchManager.switchByRegisterStorageUnit(database.getResourceMetaData(), propsMap, isInstanceConnectionEnabled);
            buildNewMetaDataContext(databaseName, switchingResource, true);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed.", databaseName, ex);
        }
    }
    
    /**
     * Alter storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void alter(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        try {
            closeStaleRules(database);
            boolean isInstanceConnectionEnabled = metaDataContexts.getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.INSTANCE_CONNECTION_ENABLED);
            SwitchingResource switchingResource = resourceSwitchManager.switchByAlterStorageUnit(database.getResourceMetaData(), propsMap, isInstanceConnectionEnabled);
            buildNewMetaDataContext(databaseName, switchingResource, true);
        } catch (final SQLException ex) {
            log.error("Alter database: {} alter storage unit failed.", databaseName, ex);
        }
    }
    
    /**
     * UnRegister storage unit.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     */
    public synchronized void unregister(final String databaseName, final String storageUnitName) {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        try {
            closeStaleRules(database);
            SwitchingResource switchingResource = resourceSwitchManager.switchByUnregisterStorageUnit(database.getResourceMetaData(), Collections.singleton(storageUnitName));
            buildNewMetaDataContext(databaseName, switchingResource, false);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed.", databaseName, ex);
        }
    }
    
    private void buildNewMetaDataContext(final String databaseName, final SwitchingResource switchingResource, final boolean isLoadSchemasFromRegisterCenter) throws SQLException {
        MetaDataContexts reloadMetaDataContexts = new MetaDataContextsFactory(metaDataPersistFacade, computeNodeInstanceContext).createBySwitchResource(
                databaseName, isLoadSchemasFromRegisterCenter, switchingResource, metaDataContexts);
        metaDataContexts.update(reloadMetaDataContexts);
        metaDataContexts.getMetaData().putDatabase(buildDatabase(reloadMetaDataContexts.getMetaData().getDatabase(databaseName)));
        switchingResource.closeStaleDataSources();
    }
    
    private ShardingSphereDatabase buildDatabase(final ShardingSphereDatabase originalDatabase) {
        return new ShardingSphereDatabase(
                originalDatabase.getName(), originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(), buildSchemas(originalDatabase));
    }
    
    private Collection<ShardingSphereSchema> buildSchemas(final ShardingSphereDatabase originalDatabase) {
        return originalDatabase.getAllSchemas().stream().map(each -> buildSchema(originalDatabase, each)).collect(Collectors.toList());
    }
    
    private ShardingSphereSchema buildSchema(final ShardingSphereDatabase originalDatabase, final ShardingSphereSchema schema) {
        return new ShardingSphereSchema(
                schema.getName(), schema.getProtocolType(), schema.getAllTables(), metaDataPersistFacade.getDatabaseMetaDataFacade().getView().load(originalDatabase.getName(), schema.getName()));
    }
    
    @SneakyThrows(Exception.class)
    private void closeStaleRules(final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
}
