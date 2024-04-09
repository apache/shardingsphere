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

package org.apache.shardingsphere.metadata.persist.service.schema;

import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;

import java.util.Collection;

/**
 * TODO replace the old implementation after meta data refactor completed
 * Schema meta data persist service.
 */
public interface TableRowDataBasedPersistService {
    
    /**
     * Persist table row data.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    void persist(String databaseName, String schemaName, String tableName, Collection<YamlShardingSphereRowData> rows);
    
    /**
     * Delete table row data.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    void delete(String databaseName, String schemaName, String tableName, Collection<YamlShardingSphereRowData> rows);
    
    /**
     * Load table data.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param table table
     * @return ShardingSphere table data
     */
    ShardingSphereTableData load(String databaseName, String schemaName, String tableName, ShardingSphereTable table);
}
