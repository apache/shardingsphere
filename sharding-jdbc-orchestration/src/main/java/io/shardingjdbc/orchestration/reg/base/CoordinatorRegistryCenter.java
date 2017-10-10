/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.reg.base;

import java.util.List;

/**
 * Coordinator based registry center.
 * 
 * @author zhangliang
 */
public interface CoordinatorRegistryCenter extends RegistryCenter {
    
    /**
     * Get data from registry center directly.
     *
     * @param key key of data
     * @return value of data
     */
    String getDirectly(String key);
    
    /**
     * Get children keys.
     * 
     * @param key key of data
     * @return children keys list
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * Get number of children keys.
     *
     * @param key key of data
     * @return number of children keys
     */
    int getNumChildren(String key);
    
    /**
     * Persist ephemeral data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persistEphemeral(String key, String value);
    
    /**
     * Persist sequential data.
     *
     * @param key key of data
     * @param value value of data
     * @return znonde name include 10 sequential digital
     */
    String persistSequential(String key, String value);
    
    /**
     * Persist ephemeral sequential data.
     * 
     * @param key key of data
     */
    void persistEphemeralSequential(String key);
    
    /**
     * Add cache data.
     * 
     * @param cachePath cache path
     */
    void addCacheData(String cachePath);
    
    /**
     * Evict cache data.
     *
     * @param cachePath cache path
     */
    void evictCacheData(String cachePath);
    
    /**
     * Get cache's raw object.
     * 
     * @param cachePath cache path
     * @return cache's raw object
     */
    Object getRawCache(String cachePath);
}
