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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.metadata.persist.node.CacheNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Registry cache manager.
 */
@RequiredArgsConstructor
public final class RegistryCacheManager {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Load cached configuration.
     * 
     * @param path path 
     * @param cacheId cache id
     * @return cached configuration
     */
    public String loadCache(final String path, final String cacheId) {
        return repository.get(CacheNode.getCachePath(path, cacheId));
    }
    
    /**
     * Delete cached configuration.
     * 
     * @param path path
     * @param cacheId cache id
     */
    public void deleteCache(final String path, final String cacheId) {
        repository.delete(CacheNode.getCachePath(path, cacheId));
    }
}
