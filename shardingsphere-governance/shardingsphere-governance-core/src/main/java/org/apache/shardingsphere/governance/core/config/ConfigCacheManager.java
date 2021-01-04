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
    
    private static final String CACHE_KEY = "cache";
    
    private static final String PATH_SEPARATOR = "/";
    
    private final ConfigurationRepository repository;
    
    public ConfigCacheManager(final ConfigurationRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Cache configuration.
     * 
     * @param key key
     * @param configuration configuration
     * @return cache id
     */
    public String cache(final String key, final String configuration) {
        String cacheId = getCacheId();
        repository.persist(getCacheKey(key, cacheId), configuration);
        return cacheId;
    }
    
    private String getCacheId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Load cached configuration.
     * 
     * @param key key 
     * @param cacheId cache id
     * @return cached configuration
     */
    public String loadCache(final String key, final String cacheId) {
        return repository.get(getCacheKey(key, cacheId));
    }
    
    /**
     * Delete cached configuration.
     * 
     * @param key key
     * @param cacheId cache id
     */
    public void deleteCache(final String key, final String cacheId) {
        repository.delete(getCacheKey(key, cacheId));
    }
    
    private String getCacheKey(final String key, final String cacheId) {
        return Joiner.on(PATH_SEPARATOR).join(key, CACHE_KEY, cacheId);
    }
    
}
