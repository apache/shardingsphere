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

package org.apache.shardingsphere.mode.metadata.persist.service;

/**
 * Schema based persist service.
 * 
 * @param <T> type of configuration
 */
public interface SchemaBasedPersistService<T> {
    
    /**
     * Persist configurations.
     *
     * @param schemaName schema name
     * @param configs configurations
     * @param isOverwrite is overwrite
     */
    void persist(String schemaName, T configs, boolean isOverwrite);
    
    /**
     * Persist configurations.
     *
     * @param schemaName schema name
     * @param configs configurations
     */
    void persist(String schemaName, T configs);
    
    /**
     * Persist version configurations.
     * 
     * @param schemaName schema name
     * @param version version
     * @param configs configurations
     */
    void persist(String schemaName, String version, T configs);
    
    /**
     * Load configurations.
     *
     * @param schemaName schema name
     * @return configurations
     */
    T load(String schemaName);
    
    /**
     * Load configurations based version.
     * 
     * @param schemaName schema name
     * @param version version
     * @return configurations
     */
    T load(String schemaName, String version);
    
    /**
     * Judge whether schema configuration existed.
     *
     * @param schemaName schema name
     * @return configuration existed or not
     */
    boolean isExisted(String schemaName);
}
