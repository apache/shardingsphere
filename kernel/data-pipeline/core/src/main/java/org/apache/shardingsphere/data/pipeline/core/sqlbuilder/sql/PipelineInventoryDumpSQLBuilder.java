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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Pipeline inventory dump SQL builder.
 */
public final class PipelineInventoryDumpSQLBuilder {
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineInventoryDumpSQLBuilder(final DatabaseType databaseType) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build divisible inventory dump SQL.
     *
     * @param param parameter
     * @return built SQL
     */
    public String buildDivisibleSQL(final BuildDivisibleSQLParameter param) {
        String queryColumns = buildQueryColumns(param.getColumnNames());
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(param.getSchemaName(), param.getTableName());
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(param.getUniqueKey());
        String operator = param.isLowerInclusive() ? ">=" : ">";
        String sql = param.isLimited()
                ? String.format("SELECT %s FROM %s WHERE %s%s? AND %s<=? ORDER BY %s ASC", queryColumns, qualifiedTableName, escapedUniqueKey, operator, escapedUniqueKey, escapedUniqueKey)
                : String.format("SELECT %s FROM %s WHERE %s%s? ORDER BY %s ASC", queryColumns, qualifiedTableName, escapedUniqueKey, operator, escapedUniqueKey);
        return dialectSQLBuilder.wrapWithPageQuery(sql);
    }
    
    /**
     * Build indivisible inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildIndivisibleSQL(final String schemaName, final String tableName, final Collection<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey);
    }
    
    private String buildQueryColumns(final Collection<String> columnNames) {
        return columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
    }
    
    /**
     * Build point query SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildPointQuerySQL(final String schemaName, final String tableName, final Collection<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s WHERE %s=?", queryColumns, qualifiedTableName, escapedUniqueKey);
    }
    
    /**
     * Build fetch all inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName tableName
     * @param columnNames column names
     * @return built SQL
     */
    public String buildFetchAllSQL(final String schemaName, final String tableName, final Collection<String> columnNames) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        return String.format("SELECT %s FROM %s", queryColumns, qualifiedTableName);
    }
    
    /**
     * Build fetch all inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName tableName
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return built SQL
     */
    public String buildFetchAllSQL(final String schemaName, final String tableName, final Collection<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        String quotedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT %s FROM %s ORDER BY %s ASC", queryColumns, qualifiedTableName, quotedUniqueKey);
    }
}
