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

package org.apache.shardingsphere.governance.core.config;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;

import java.util.UUID;

/**
 * Config cache manager.
 */
public final class ConfigCacheManager {
    
    private static final String PATH_SEPARATOR = "/";
    
    private final ConfigurationRepository repository;
    
    private final ConfigCenterNode node;
    
    public ConfigCacheManager(final ConfigurationRepository repository, final ConfigCenterNode node) {
        this.repository = repository;
        this.node = node;
    }
    
    /**
     * Cache configuration.
     * 
     * @param path path
     * @param configuration configuration
     * @return cache id
     */
    public String cache(final String path, final String configuration) {
        String cacheId = getCacheId();
        repository.persist(getCachePath(path, cacheId), configuration);
        return cacheId;
    }
    
    private String getCacheId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Load cached configuration.
     * 
     * @param path path 
     * @param cacheId cache id
     * @return cached configuration
     */
    public String loadCache(final String path, final String cacheId) {
        return repository.get(getCachePath(path, cacheId));
    }
    
    /**
     * Delete cached configuration.
     * 
     * @param path path
     * @param cacheId cache id
     */
    public void deleteCache(final String path, final String cacheId) {
        repository.delete(getCachePath(path, cacheId));
    }
    
    private String getCachePath(final String path, final String cacheId) {
        return Joiner.on(PATH_SEPARATOR).join(node.getCachePath(path), cacheId);
    }
}
