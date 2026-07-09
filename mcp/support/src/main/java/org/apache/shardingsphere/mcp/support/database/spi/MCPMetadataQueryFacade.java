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

package org.apache.shardingsphere.mcp.support.database.spi;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSequence;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;

import java.util.List;
import java.util.Optional;

/**
 * MCP metadata query facade.
 */
public interface MCPMetadataQueryFacade {
    
    /**
     * Query all logical databases.
     *
     * @return database profiles
     */
    List<RuntimeDatabaseProfile> queryDatabases();
    
    /**
     * Query one logical database.
     *
     * @param databaseName database name
     * @return database profile
     */
    Optional<RuntimeDatabaseProfile> queryDatabase(String databaseName);
    
    /**
     * Query schemas in one logical database.
     *
     * @param databaseName database name
     * @return schema metadata list
     */
    List<ShardingSphereSchema> querySchemas(String databaseName);
    
    /**
     * Query one schema in a logical database.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema metadata
     */
    Optional<ShardingSphereSchema> querySchema(String databaseName, String schemaName);
    
    /**
     * Query tables in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table metadata list
     */
    List<ShardingSphereTable> queryTables(String databaseName, String schemaName);
    
    /**
     * Query one table in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table metadata
     */
    Optional<ShardingSphereTable> queryTable(String databaseName, String schemaName, String tableName);
    
    /**
     * Query views in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view metadata list
     */
    List<ShardingSphereTable> queryViews(String databaseName, String schemaName);
    
    /**
     * Query one view in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view metadata
     */
    Optional<ShardingSphereTable> queryView(String databaseName, String schemaName, String viewName);
    
    /**
     * Query table columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return column metadata list
     */
    List<ShardingSphereColumn> queryTableColumns(String databaseName, String schemaName, String tableName);
    
    /**
     * Query one table column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return column metadata
     */
    Optional<ShardingSphereColumn> queryTableColumn(String databaseName, String schemaName, String tableName, String columnName);
    
    /**
     * Query view columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return column metadata list
     */
    List<ShardingSphereColumn> queryViewColumns(String databaseName, String schemaName, String viewName);
    
    /**
     * Query one view column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param columnName column name
     * @return column metadata
     */
    Optional<ShardingSphereColumn> queryViewColumn(String databaseName, String schemaName, String viewName, String columnName);
    
    /**
     * Query table indexes.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata list
     */
    List<ShardingSphereIndex> queryIndexes(String databaseName, String schemaName, String tableName);
    
    /**
     * Query one table index.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param indexName index name
     * @return index metadata
     */
    Optional<ShardingSphereIndex> queryIndex(String databaseName, String schemaName, String tableName, String indexName);
    
    /**
     * Query sequences in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return sequence metadata list
     */
    List<ShardingSphereSequence> querySequences(String databaseName, String schemaName);
    
    /**
     * Query one sequence in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param sequenceName sequence name
     * @return sequence metadata
     */
    Optional<ShardingSphereSequence> querySequence(String databaseName, String schemaName, String sequenceName);
    
    /**
     * Check whether the database supports the specified metadata object type.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return whether the metadata object type is supported
     */
    boolean isSupportedMetadataObjectType(String databaseName, SupportedMCPMetadataObjectType objectType);
}
