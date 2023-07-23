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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline inventory dump SQL builder.
 */
public final class PipelineInventoryDumpSQLBuilder {
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineInventoryDumpSQLBuilder(final DatabaseType databaseType) {
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build divisible inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildDivisibleSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s WHERE %s>=? AND %s<=? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, escapedUniqueKey, escapedUniqueKey, escapedUniqueKey);
    }
    
    /**
     * Build divisible inventory dump SQL with unlimited value.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildUnlimitedDivisibleSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s WHERE %s>=? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, escapedUniqueKey, escapedUniqueKey);
    }
    
    /**
     * Build indivisible inventory dump first SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildIndivisibleSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey);
    }
    
    private String buildQueryColumns(final List<String> columnNames) {
        return columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
    }
    
    /**
     * Build fetch all inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName tableName
     * @return built SQL
     */
    public String buildFetchAllSQL(final String schemaName, final String tableName) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        return String.format("SELECT * FROM %s", qualifiedTableName);
    }
}
