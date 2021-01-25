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

package org.apache.shardingsphere.governance.repository.api;

import org.apache.shardingsphere.infra.spi.typed.TypedSPI;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;

import java.util.List;

/**
 * Governance repository.
 */
public interface GovernanceRepository extends TypedSPI {
    
    /**
     * Path separator.
     */
    String PATH_SEPARATOR = "/";
    
    /**
     * Dot separator.
     */
    String DOT_SEPARATOR = ".";
    
    /**
     * Initialize governance center.
     *
     * @param name governance center name
     * @param config governance center configuration
     */
    void init(String name, GovernanceCenterConfiguration config);
    
    /**
     * Get data from governance center.
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
     * Watch key or path of governance server.
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
