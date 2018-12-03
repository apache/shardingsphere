/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.orchestration.reg.api;

import io.shardingsphere.orchestration.reg.listener.DataChangedEventListener;

import java.util.List;

/**
 * Registry center.
 * 
 * @author zhangliang
 */
public interface RegistryCenter extends AutoCloseable {
    
    /**
     * Initialize registry center.
     * 
     * @param config registry center configuration
     */
    void init(RegistryCenterConfiguration config);
    
    /**
     * Get data from registry center.
     * 
     * <p>Maybe use cache if existed.</p>
     * 
     * @param key key of data
     * @return value of data
     */
    String get(String key);
    
    /**
     * Get data from registry center directly.
     * 
     * <p>Cannot use cache.</p>
     *
     * @param key key of data
     * @return value of data
     */
    String getDirectly(String key);
    
    /**
     * Adjust data is existed or not.
     * 
     * @param key key of data
     * @return data is existed or not
     */
    boolean isExisted(String key);
    
    /**
     * Get node's sub-nodes list.
     *
     * @param key key of data
     * @return sub-nodes name list
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * Persist data.
     * 
     * @param key key of data
     * @param value value of data
     */
    void persist(String key, String value);
    
    /**
     * Update data.
     *
     * @param key key of data
     * @param value value of data
     */
    void update(String key, String value);
    
    /**
     * Persist ephemeral data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persistEphemeral(String key, String value);
    
    /**
     * Watch key or path of the registry.
     *
     * @param key key of data
     * @param dataChangedEventListener data changed event listener
     */
    void watch(String key, DataChangedEventListener dataChangedEventListener);
}
