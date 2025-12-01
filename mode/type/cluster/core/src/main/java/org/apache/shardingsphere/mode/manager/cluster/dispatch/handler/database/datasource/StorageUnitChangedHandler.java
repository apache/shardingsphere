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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.DatabaseLeafValueChangedHandler;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.datasource.StorageUnitNodePath;

import java.util.Collections;

/**
 * Storage unit changed handler.
 */
@RequiredArgsConstructor
public final class StorageUnitChangedHandler implements DatabaseLeafValueChangedHandler {
    
    private final ContextManager contextManager;
    
    @Override
    public NodePath getSubscribedNodePath(final String databaseName) {
        return new StorageUnitNodePath(databaseName, NodePathPattern.IDENTIFIER);
    }
    
    @Override
    public void handle(final String databaseName, final DataChangedEvent event) {
        String storageUnitName = NodePathSearcher.get(event.getKey(), StorageUnitNodePath.createStorageUnitSearchCriteria(databaseName));
        switch (event.getType()) {
            case ADDED:
                handleRegistered(databaseName, storageUnitName);
                break;
            case UPDATED:
                handleAltered(databaseName, storageUnitName);
                break;
            case DELETED:
                handleUnregistered(databaseName, storageUnitName);
                break;
            default:
                break;
        }
    }
    
    private void handleRegistered(final String databaseName, final String storageUnitName) {
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataFacade().getDataSourceUnitService().load(databaseName, storageUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().register(databaseName, Collections.singletonMap(storageUnitName, dataSourcePoolProps));
    }
    
    private void handleAltered(final String databaseName, final String storageUnitName) {
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataFacade().getDataSourceUnitService().load(databaseName, storageUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().alter(databaseName, Collections.singletonMap(storageUnitName, dataSourcePoolProps));
    }
    
    private void handleUnregistered(final String databaseName, final String storageUnitName) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().unregister(databaseName, storageUnitName);
    }
}
