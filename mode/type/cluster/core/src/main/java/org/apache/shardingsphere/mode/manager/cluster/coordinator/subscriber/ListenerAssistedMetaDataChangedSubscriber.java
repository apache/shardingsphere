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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.MetaDataWatchListenerManager;
import org.apache.shardingsphere.mode.processor.ListenerAssistedProcessor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.listener.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.listener.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.listener.MetaDataChangedListener;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Optional;

/**
 * Listener assisted meta data changed subscriber.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@RequiredArgsConstructor
public final class ListenerAssistedMetaDataChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew to persist meta data.
     *
     * @param event database added event
     */
    @Subscribe
    public synchronized void renew(final CreateDatabaseListenerAssistedEvent event) {
        Optional<ListenerAssistedProcessor> processor = TypedSPILoader.findService(ListenerAssistedProcessor.class, event.getClass().getName());
        if (!processor.isPresent()) {
            return;
        }
        new MetaDataWatchListenerManager((ClusterPersistRepository) contextManager.getRepository())
                .addListener(processor.get().getListenerKey(event), new MetaDataChangedListener(contextManager.getComputeNodeInstanceContext().getEventBusContext()));
        processor.get().processor(contextManager, event);
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DropDatabaseListenerAssistedEvent event) {
        Optional<ListenerAssistedProcessor> processor = TypedSPILoader.findService(ListenerAssistedProcessor.class, event.getClass().getName());
        if (!processor.isPresent()) {
            return;
        }
        new MetaDataWatchListenerManager((ClusterPersistRepository) contextManager.getRepository()).removeListener(processor.get().getListenerKey(event));
        processor.get().processor(contextManager, event);
    }
}
