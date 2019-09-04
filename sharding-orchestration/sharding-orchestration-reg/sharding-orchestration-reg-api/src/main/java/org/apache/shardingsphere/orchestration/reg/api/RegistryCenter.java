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

package org.apache.shardingsphere.orchestration.reg.api;

import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEventListener;
import org.apache.shardingsphere.spi.TypeBasedSPI;

import java.util.List;

/**
 * Registry center.
 * 
 * @author zhangliang
 * @author zhaojun
 */
public interface RegistryCenter extends TypeBasedSPI {
    
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
     * Judge data is existed or not.
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
    
    /**
     * Close.
      */
    void close();

    /**
     * Initialize the lock of the key.
     *
     * @param key key of data
     */
    void initLock(String key);

    /**
     * Try to get the lock of the key.
     *
     * @return get the lock or not
     */
    boolean tryLock();

    /**
     * Try to release the lock of the key.
     *
     */
    void tryRelease();
}
