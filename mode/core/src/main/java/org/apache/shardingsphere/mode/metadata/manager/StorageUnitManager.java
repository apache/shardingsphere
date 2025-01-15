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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Storage unit manager.
 */
@Slf4j
public final class StorageUnitManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private final ResourceSwitchManager resourceSwitchManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public StorageUnitManager(final MetaDataContexts metaDataContexts, final ComputeNodeInstanceContext computeNodeInstanceContext,
                              final PersistRepository repository, final ResourceSwitchManager resourceSwitchManager) {
        this.metaDataContexts = metaDataContexts;
        this.computeNodeInstanceContext = computeNodeInstanceContext;
        this.resourceSwitchManager = resourceSwitchManager;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Register storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void registerStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByRegisterStorageUnit(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * Alter storage unit.
     *
     * @param databaseName database name
     * @param propsMap data source pool properties map
     */
    public synchronized void alterStorageUnit(final String databaseName, final Map<String, DataSourcePoolProperties> propsMap) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByAlterStorageUnit(metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), propsMap);
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    /**
     * UnRegister storage unit.
     *
     * @param databaseName database name
     * @param storageUnitName storage unit name
     */
    public synchronized void unregisterStorageUnit(final String databaseName, final String storageUnitName) {
        try {
            closeStaleRules(databaseName);
            SwitchingResource switchingResource = resourceSwitchManager.switchByUnregisterStorageUnit(
                    metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), Collections.singletonList(storageUnitName));
            buildNewMetaDataContext(databaseName, switchingResource);
        } catch (final SQLException ex) {
            log.error("Alter database: {} register storage unit failed", databaseName, ex);
        }
    }
    
    private void buildNewMetaDataContext(final String databaseName, final SwitchingResource switchingResource) throws SQLException {
        MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createBySwitchResource(
                databaseName, true, switchingResource, metaDataContexts, metaDataPersistService, computeNodeInstanceContext);
        metaDataContexts.update(reloadMetaDataContexts);
        metaDataContexts.getMetaData().putDatabase(buildDatabase(reloadMetaDataContexts.getMetaData().getDatabase(databaseName)));
        switchingResource.closeStaleDataSources();
    }
    
    private ShardingSphereDatabase buildDatabase(final ShardingSphereDatabase originalDatabase) {
        return new ShardingSphereDatabase(
                originalDatabase.getName(), originalDatabase.getProtocolType(), originalDatabase.getResourceMetaData(), originalDatabase.getRuleMetaData(), buildSchemas(originalDatabase));
    }
    
    private Collection<ShardingSphereSchema> buildSchemas(final ShardingSphereDatabase originalDatabase) {
        return originalDatabase.getAllSchemas().stream().map(each -> new ShardingSphereSchema(
                each.getName(), each.getAllTables(), metaDataPersistService.getDatabaseMetaDataFacade().getView().load(originalDatabase.getName(), each.getName()))).collect(Collectors.toList());
    }
    
    @SneakyThrows(Exception.class)
    private void closeStaleRules(final String databaseName) {
        for (ShardingSphereRule each : metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules()) {
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
    }
}
