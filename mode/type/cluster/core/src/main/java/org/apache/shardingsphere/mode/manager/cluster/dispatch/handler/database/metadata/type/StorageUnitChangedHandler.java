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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.database.metadata.type;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.checker.ActiveVersionChecker;

import java.util.Collections;

/**
 * Storage unit changed handler.
 */
@RequiredArgsConstructor
public final class StorageUnitChangedHandler {
    
    private final ContextManager contextManager;
    
    /**
     * Handle storage unit registered.
     *
     * @param databaseName database name
     * @param dataSourceUnitName data source unit name
     * @param event data changed event
     */
    public void handleRegistered(final String databaseName, final String dataSourceUnitName, final DataChangedEvent event) {
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDataSourceUnitService().load(databaseName, dataSourceUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().register(databaseName, Collections.singletonMap(dataSourceUnitName, dataSourcePoolProps));
    }
    
    /**
     * Handle storage unit altered.
     *
     * @param databaseName database name
     * @param dataSourceUnitName data source unit name
     * @param event data changed event
     */
    public void handleAltered(final String databaseName, final String dataSourceUnitName, final DataChangedEvent event) {
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        DataSourcePoolProperties dataSourcePoolProps = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getDataSourceUnitService().load(databaseName, dataSourceUnitName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().alter(databaseName, Collections.singletonMap(dataSourceUnitName, dataSourcePoolProps));
    }
    
    /**
     * Handle storage unit unregistered.
     *
     * @param databaseName database name
     * @param dataSourceUnitName data source unit name
     */
    public void handleUnregistered(final String databaseName, final String dataSourceUnitName) {
        Preconditions.checkState(contextManager.getMetaDataContexts().getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        contextManager.getMetaDataContextManager().getStorageUnitManager().unregister(databaseName, dataSourceUnitName);
    }
}
