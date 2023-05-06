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

package org.apache.shardingsphere.metadata.persist.service.database;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.metadata.persist.service.schema.SchemaMetaDataPersistService;

import java.util.Collection;
import java.util.Map;

/**
 * TODO replace the old implementation after meta data refactor completed
 * Database meta data based registry service.
 */
public interface DatabaseMetaDataBasedPersistService {
    
    /**
     * Add database name.
     *
     * @param databaseName database name
     */
    void addDatabase(String databaseName);
    
    /**
     * Drop database.
     *
     * @param databaseName database name to be deleted
     */
    void dropDatabase(String databaseName);
    
    /**
     * Load all database names.
     *
     * @return all database names
     */
    Collection<String> loadAllDatabaseNames();
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    void addSchema(String databaseName, String schemaName);
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    void dropSchema(String databaseName, String schemaName);
    
    /**
     * Compare and persist schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    void compareAndPersist(String databaseName, String schemaName, ShardingSphereSchema schema);
    
    /**
     * Persist schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    void persist(String databaseName, String schemaName, ShardingSphereSchema schema);
    
    /**
     * Delete schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    void delete(String databaseName, String schemaName, ShardingSphereSchema schema);
    
    /**
     * Load schema meta data.
     *
     * @param databaseName database name
     * @return schema meta data
     */
    Map<String, ShardingSphereSchema> loadSchemas(String databaseName);
    
    /**
     * Get table meta data persist service.
     *
     * @return persist service
     */
    SchemaMetaDataPersistService<Map<String, ShardingSphereTable>> getTableMetaDataPersistService();
    
    /**
     * Get view meta data persist service.
     *
     * @return persist service
     */
    SchemaMetaDataPersistService<Map<String, ShardingSphereView>> getViewMetaDataPersistService();
}
