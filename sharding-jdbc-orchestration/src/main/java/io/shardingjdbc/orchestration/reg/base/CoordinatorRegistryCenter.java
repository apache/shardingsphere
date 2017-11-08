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
     * Persist ephemeral data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persistEphemeral(String key, String value);
    
    /**
     * Add cache data.
     * 
     * @param cachePath cache path
     */
    void addCacheData(String cachePath);
    
    /**
     * Get node's sub-nodes list.
     *
     * @param path key
     * @return sub-nodes name list
     */
    List<String> getChildrenKeys(String path);
    
    /**
     * Get cache's raw object.
     * 
     * @param cachePath cache path
     * @return cache's raw object
     */
    Object getRawCache(String cachePath);
}
