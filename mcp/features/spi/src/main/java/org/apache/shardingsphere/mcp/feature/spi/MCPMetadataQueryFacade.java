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

package org.apache.shardingsphere.mcp.feature.spi;

import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;

import java.util.List;
import java.util.Optional;

/**
 * MCP metadata query facade.
 */
public interface MCPMetadataQueryFacade {
    
    /**
     * Query all logical databases.
     *
     * @return database metadata list
     */
    List<MCPDatabaseMetadata> queryDatabases();
    
    /**
     * Query one logical database.
     *
     * @param databaseName database name
     * @return database metadata
     */
    Optional<MCPDatabaseMetadata> queryDatabase(String databaseName);
    
    /**
     * Query schemas in one logical database.
     *
     * @param databaseName database name
     * @return schema metadata list
     */
    List<MCPSchemaMetadata> querySchemas(String databaseName);
    
    /**
     * Query one schema in a logical database.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return schema metadata
     */
    Optional<MCPSchemaMetadata> querySchema(String databaseName, String schemaName);
    
    /**
     * Query tables in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return table metadata list
     */
    List<MCPTableMetadata> queryTables(String databaseName, String schemaName);
    
    /**
     * Query one table in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return table metadata
     */
    Optional<MCPTableMetadata> queryTable(String databaseName, String schemaName, String tableName);
    
    /**
     * Query views in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view metadata list
     */
    List<MCPViewMetadata> queryViews(String databaseName, String schemaName);
    
    /**
     * Query one view in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view metadata
     */
    Optional<MCPViewMetadata> queryView(String databaseName, String schemaName, String viewName);
    
    /**
     * Query table columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return column metadata list
     */
    List<MCPColumnMetadata> queryTableColumns(String databaseName, String schemaName, String tableName);
    
    /**
     * Query one table column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return column metadata
     */
    Optional<MCPColumnMetadata> queryTableColumn(String databaseName, String schemaName, String tableName, String columnName);
    
    /**
     * Query view columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return column metadata list
     */
    List<MCPColumnMetadata> queryViewColumns(String databaseName, String schemaName, String viewName);
    
    /**
     * Query one view column.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param columnName column name
     * @return column metadata
     */
    Optional<MCPColumnMetadata> queryViewColumn(String databaseName, String schemaName, String viewName, String columnName);
    
    /**
     * Query table indexes.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @return index metadata list
     */
    List<MCPIndexMetadata> queryIndexes(String databaseName, String schemaName, String tableName);
    
    /**
     * Query one table index.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param indexName index name
     * @return index metadata
     */
    Optional<MCPIndexMetadata> queryIndex(String databaseName, String schemaName, String tableName, String indexName);
    
    /**
     * Query sequences in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return sequence metadata list
     */
    List<MCPSequenceMetadata> querySequences(String databaseName, String schemaName);
    
    /**
     * Query one sequence in a schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param sequenceName sequence name
     * @return sequence metadata
     */
    Optional<MCPSequenceMetadata> querySequence(String databaseName, String schemaName, String sequenceName);
    
    /**
     * Check whether the database supports the specified metadata object type.
     *
     * @param databaseName database name
     * @param objectType metadata object type
     * @return whether the metadata object type is supported
     */
    boolean isSupportedMetadataObjectType(String databaseName, SupportedMCPMetadataObjectType objectType);
}
