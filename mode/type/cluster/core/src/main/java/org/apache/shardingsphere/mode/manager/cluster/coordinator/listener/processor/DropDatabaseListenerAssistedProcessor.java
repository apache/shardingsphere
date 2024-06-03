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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.processor;

import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.listener.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.processor.ListenerAssistedProcessor;

/**
 * Drop database listener assisted processor.
 */
public final class DropDatabaseListenerAssistedProcessor implements ListenerAssistedProcessor<DropDatabaseListenerAssistedEvent> {
    
    @Override
    public String getListenerKey(final DropDatabaseListenerAssistedEvent event) {
        return DatabaseMetaDataNode.getDatabaseNamePath(event.getDatabaseName());
    }
    
    @Override
    public void processor(final ContextManager contextManager, final DropDatabaseListenerAssistedEvent event) {
        contextManager.getMetaDataContextManager().getResourceMetaDataManager().dropDatabase(event.getDatabaseName());
        contextManager.getPersistServiceFacade().getListenerAssistedPersistService().deleteDatabaseNameListenerAssisted(event.getDatabaseName());
    }
    
    @Override
    public String getType() {
        return DropDatabaseListenerAssistedEvent.class.getName();
    }
}
