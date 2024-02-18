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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.Optional;

/**
 * Governance watcher.
 * 
 * @param <T> type of event
 */
@SingletonSPI
public interface GovernanceWatcher<T> {
    
    /**
     * Get watching keys.
     *
     * @param databaseName database name
     * @return watching keys
     */
    Collection<String> getWatchingKeys(String databaseName);
    
    /**
     * Get watching types.
     *
     * @return watching types
     */
    Collection<Type> getWatchingTypes();
    
    /**
     * Create governance event.
     * 
     * @param event registry center data changed event
     * @return governance event
     */
    Optional<T> createGovernanceEvent(DataChangedEvent event);
}
