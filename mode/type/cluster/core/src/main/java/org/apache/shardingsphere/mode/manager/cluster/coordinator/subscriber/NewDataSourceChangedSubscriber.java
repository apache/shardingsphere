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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.mode.event.datasource.AlterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.RegisterStorageUnitEvent;
import org.apache.shardingsphere.mode.event.datasource.UnregisterStorageUnitEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * TODO Rename DataSourceChangedSubscriber when metadata structure adjustment completed. #25485
 * New data source changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NewDataSourceChangedSubscriber {
    
    private final ContextManager contextManager;
    
    public NewDataSourceChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Renew for register storage unit.
     *
     * @param event register storage unit event
     */
    @Subscribe
    public void renew(final RegisterStorageUnitEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        contextManager.registerStorageUnit(event.getDatabaseName(),
                contextManager.getMetaDataContexts().getPersistService().getDataSourceService().load(event.getDatabaseName(), event.getStorageUnitName()));
    }
    
    /**
     * Renew for alter storage unit.
     *
     * @param event register storage unit event
     */
    @Subscribe
    public void renew(final AlterStorageUnitEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        contextManager.alterStorageUnit(event.getDatabaseName(), event.getStorageUnitName(),
                contextManager.getMetaDataContexts().getPersistService().getDataSourceService().load(event.getDatabaseName(), event.getStorageUnitName()));
    }
    
    /**
     * Renew for unregister storage unit.
     *
     * @param event register storage unit event
     */
    @Subscribe
    public void renew(final UnregisterStorageUnitEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        contextManager.unregisterStorageUnit(event.getDatabaseName(), event.getStorageUnitName());
    }
}
