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

package org.apache.shardingsphere.mode.manager.cluster.listener;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

/**
 * Data changed event listener manager.
 */
@RequiredArgsConstructor
public final class DataChangedEventListenerManager {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Add listener.
     *
     * @param listenerKey listener key
     * @param dataChangedEventListener data changed event listener
     */
    public void addListener(final String listenerKey, final DataChangedEventListener dataChangedEventListener) {
        repository.watch(listenerKey, dataChangedEventListener);
    }
    
    /**
     * Remove listener.
     *
     * @param listenerKey listener key
     */
    public void removeListener(final String listenerKey) {
        repository.removeDataListener(listenerKey);
    }
}
