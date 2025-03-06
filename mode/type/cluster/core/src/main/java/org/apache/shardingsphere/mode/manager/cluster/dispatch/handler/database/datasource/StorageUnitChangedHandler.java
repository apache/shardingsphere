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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.datasource;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseChangedHandler;
import org.apache.shardingsphere.mode.metadata.manager.ActiveVersionChecker;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.datasource.StorageUnitNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Collections;

/**
 * Storage unit changed handler.
 */
public final class StorageUnitChangedHandler implements DatabaseChangedHandler {
    
    private final ContextManager contextManager;
    
    private final ActiveVersionChecker activeVersionChecker;
    
    public StorageUnitChangedHandler(final ContextManager contextManager) {
        this.contextManager = contextManager;
        activeVersionChecker = new ActiveVersionChecker(contextManager.getPersistServiceFacade().getRepository());
    }
    
    @Override
    public boolean isSubscribed(final String databaseName, final String path) {
        return new VersionNodePathParser(new StorageUnitNodePath(databaseName, NodePathPattern.IDENTIFIER)).isActiveVersionPath(path);
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String storageUnitName = NodePathSearcher.get(event.getKey(), StorageUnitNodePath.createStorageUnitSearchCriteria(databaseName));
        switch (event.getType()) {
            case ADDED:
                if (activeVersionChecker.checkSame(event)) {
                    handleRegistered(databaseName, storageUnitName);
                }
                break;
            case UPDATED:
                if (activeVersionChecker.checkSame(event)) {
                    handleAltered(databaseName, storageUnitName);
                }
                break;
            case DELETED:
                handleUnregistered(databaseName, storageUnitName);
                break;
            default:
                break;
        }
    }
    
    private void handleRegistered(final String databaseName, final String storageUnitName) {
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDataSourceUnitService().load(databaseName, storageUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().register(databaseName, Collections.singletonMap(storageUnitName, dataSourcePoolProps));
    }
    
    private void handleAltered(final String databaseName, final String storageUnitName) {
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDataSourceUnitService().load(databaseName, storageUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().alter(databaseName, Collections.singletonMap(storageUnitName, dataSourcePoolProps));
    }
    
    private void handleUnregistered(final String databaseName, final String storageUnitName) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().unregister(databaseName, storageUnitName);
    }
}
