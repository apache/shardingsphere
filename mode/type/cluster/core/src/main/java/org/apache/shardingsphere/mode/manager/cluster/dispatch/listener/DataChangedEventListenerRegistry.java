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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.listener;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.DatabaseMetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.GlobalMetaDataChangedListener;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * Data changed event listener registry.
 */
public final class DataChangedEventListenerRegistry {
    
    private final ContextManager contextManager;
    
    private final ClusterPersistRepository repository;
    
    private final Collection<String> databaseNames;
    
    public DataChangedEventListenerRegistry(final ContextManager contextManager, final Collection<String> databaseNames) {
        this.contextManager = contextManager;
        repository = (ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository();
        this.databaseNames = databaseNames;
    }
    
    /**
     * Register data changed event listeners.
     */
    public void register() {
        databaseNames.forEach(this::registerDatabaseListeners);
        ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).forEach(this::registerGlobalListeners);
    }
    
    private void registerDatabaseListeners(final String databaseName) {
        repository.watch(NodePathGenerator.toPath(new DatabaseMetaDataNodePath(databaseName)), new DatabaseMetaDataChangedListener(contextManager));
    }
    
    private void registerGlobalListeners(final GlobalDataChangedEventHandler handler) {
        repository.watch(NodePathGenerator.toPath(handler.getSubscribedNodePath()), new GlobalMetaDataChangedListener(contextManager, handler));
    }
}
