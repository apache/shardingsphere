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

package org.apache.shardingsphere.mode.metadata.persist.service.schema;

/**
 * Schema meta data persist service.
 *
 * @param <T> type of schema
 */
public interface SchemaMetaDataPersistService<T> {
    
    /**
     * Compare and persist meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    void compareAndPersist(String databaseName, String schemaName, T schema);
    
    /**
     * Persist meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    void persist(String databaseName, String schemaName, T schema);
    
    /**
     * Load schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema meta data
     */
    T load(String databaseName, String schemaName);
    
    /**
     * Delete table or view.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param name table or view name
     */
    void delete(String databaseName, String schemaName, String name);
}
