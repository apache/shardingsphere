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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MCP feature direct query facade.
 */
public interface MCPFeatureQueryFacade {
    
    /**
     * Query rows from a target database.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param sql SQL text
     * @return query rows
     */
    List<Map<String, Object>> query(String databaseName, String schemaName, String sql);
    
    /**
     * Query rows using any configured database.
     *
     * @param sql SQL text
     * @return query rows
     */
    List<Map<String, Object>> queryWithAnyDatabase(String sql);
    
    /**
     * Query column definition.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return formatted column definition
     */
    String queryColumnDefinition(String databaseName, String schemaName, String tableName, String columnName);
    
    /**
     * Query information schema columns.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @return actual column names
     */
    Set<String> queryInformationSchemaColumnNames(String databaseName, String schemaName, String tableName, Collection<String> columnNames);
}
