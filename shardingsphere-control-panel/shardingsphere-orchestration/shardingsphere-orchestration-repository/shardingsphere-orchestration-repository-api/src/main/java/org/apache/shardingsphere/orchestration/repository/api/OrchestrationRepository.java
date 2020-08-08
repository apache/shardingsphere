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

package org.apache.shardingsphere.orchestration.repository.api;

import org.apache.shardingsphere.infra.spi.type.TypedSPI;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEventListener;

import java.util.List;

/**
 * Orchestration repository.
 */
public interface OrchestrationRepository extends TypedSPI {
    
    /**
     * Initialize orchestration center.
     *
     * @param name orchestration center name
     * @param config orchestration center configuration
     */
    void init(String name, OrchestrationCenterConfiguration config);
    
    /**
     * Get data from orchestration center.
     *
     * <p>Maybe use cache if existed.</p>
     *
     * @param key key of data
     * @return value of data
     */
    String get(String key);
    
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
     * Delete node.
     *
     * @param key key of data
     */
    void delete(String key);
    
    /**
     * Watch key or path of orchestration server.
     *
     * @param key key of data
     * @param listener data changed event listener
     */
    void watch(String key, DataChangedEventListener listener);
    
    /**
     * Close.
     */
    void close();
}
