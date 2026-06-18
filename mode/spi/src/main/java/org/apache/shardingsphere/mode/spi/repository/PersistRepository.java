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

package org.apache.shardingsphere.mode.spi.repository;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.List;

/**
 * Persist repository.
 */
public interface PersistRepository extends TypedSPI, AutoCloseable {
    
    /**
     * Path separator.
     */
    String PATH_SEPARATOR = "/";
    
    /**
     * Query data.
     *
     * @param key key to be queried
     * @return queried data
     */
    String query(String key);
    
    /**
     * Get names of sub-node.
     *
     * @param key key of data
     * @return sub-node names
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * Judge node is existed or not.
     *
     * @param key key
     * @return node is existed or not
     */
    boolean isExisted(String key);
    
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
     * @param key key
     * @param value value
     */
    void update(String key, String value);
    
    /**
     * Delete node.
     *
     * @param key key of data
     */
    void delete(String key);
    
    @Override
    void close();
}
