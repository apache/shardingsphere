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

package org.apache.shardingsphere.mode.persist.service;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.Collection;

/**
 * Table meta data persist service.
 */
public interface TableMetaDataPersistService {
    
    /**
     * Load tables.
     *
     * @param databaseName to be loaded database name
     * @param schemaName to be loaded schema name
     * @return loaded tables
     */
    Collection<ShardingSphereTable> load(String databaseName, String schemaName);
    
    /**
     * Load table.
     *
     * @param databaseName to be loaded database name
     * @param schemaName to be loaded schema name
     * @param tableName to be loaded table name
     * @return loaded table
     */
    ShardingSphereTable load(String databaseName, String schemaName, String tableName);
    
    /**
     * Persist tables.
     *
     * @param databaseName to be persisted database name
     * @param schemaName to be persisted schema name
     * @param tables to be persisted tables
     */
    void persist(String databaseName, String schemaName, Collection<ShardingSphereTable> tables);
    
    /**
     * Drop table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName to be dropped table name
     */
    void drop(String databaseName, String schemaName, String tableName);
    
    /**
     * Drop tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tables to be dropped tables
     */
    void drop(String databaseName, String schemaName, Collection<ShardingSphereTable> tables);
}
