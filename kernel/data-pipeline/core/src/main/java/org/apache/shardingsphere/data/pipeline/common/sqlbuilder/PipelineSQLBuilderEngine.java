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

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.DatabaseTypedSPILoader;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Pipeline SQL builder engine.
 */
public final class PipelineSQLBuilderEngine {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    @Getter
    private final PipelineInventoryDumpSQLBuilder inventoryDumpSQLBuilder;
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    private final ConcurrentMap<String, String> sqlCacheMap;
    
    public PipelineSQLBuilderEngine(final DatabaseType databaseType) {
        inventoryDumpSQLBuilder = new PipelineInventoryDumpSQLBuilder(databaseType);
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
        sqlCacheMap = new ConcurrentHashMap<>();
    }
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return dialectSQLBuilder.buildCreateSchemaSQL(schemaName);
    }
    
    /**
     * Build insert SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @return insert SQL
     */
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(schemaName, dataRecord.getTableName(), dataRecord.getColumns()));
        }
        String insertSQL = sqlCacheMap.get(sqlCacheKey);
        return dialectSQLBuilder.buildInsertSQLOnDuplicateClause(schemaName, dataRecord).map(optional -> insertSQL + " " + optional).orElse(insertSQL);
    }
    
    private String buildInsertSQLInternal(final String schemaName, final String tableName, final List<Column> columns) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (Column each : columns) {
            columnsLiteral.append(String.format("%s,", sqlSegmentBuilder.getEscapedIdentifier(each.getName())));
            holder.append("?,");
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        return String.format("INSERT INTO %s(%s) VALUES(%s)", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName), columnsLiteral, holder);
    }
    
    /**
     * Build update SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return update SQL
     */
    public String buildUpdateSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(schemaName, dataRecord.getTableName(), conditionColumns));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (Column each : extractUpdatedColumns(dataRecord)) {
            updatedColumnString.append(String.format("%s = ?,", sqlSegmentBuilder.getEscapedIdentifier(each.getName())));
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        return String.format(sqlCacheMap.get(sqlCacheKey), updatedColumnString);
    }
    
    private String buildUpdateSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("UPDATE %s SET %%s WHERE %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName), buildWhereSQL(conditionColumns));
    }
    
    /**
     * Extract updated columns.
     *
     * @param dataRecord data record
     * @return filtered columns
     */
    public List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        return dialectSQLBuilder.extractUpdatedColumns(dataRecord);
    }
    
    /**
     * Build delete SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return delete SQL
     */
    public String buildDeleteSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(schemaName, dataRecord.getTableName(), conditionColumns));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    /**
     * Build drop SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return drop SQL
     */
    public String buildDropSQL(final String schemaName, final String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    private String buildDeleteSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("DELETE FROM %s WHERE %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName), buildWhereSQL(conditionColumns));
    }
    
    private String buildWhereSQL(final Collection<Column> conditionColumns) {
        StringBuilder where = new StringBuilder();
        for (Column each : conditionColumns) {
            where.append(String.format("%s = ? AND ", sqlSegmentBuilder.getEscapedIdentifier(each.getName())));
        }
        where.setLength(where.length() - 5);
        return where.toString();
    }
    
    /**
     * Build count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return count SQL
     */
    public String buildCountSQL(final String schemaName, final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    /**
     * Build estimated count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return estimated count SQL
     */
    public Optional<String> buildEstimatedCountSQL(final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildEstimatedCountSQL(schemaName, tableName);
    }
    
    /**
     * Build unique key minimum maximum values SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @return min max unique key SQL
     */
    public String buildUniqueKeyMinMaxValuesSQL(final String schemaName, final String tableName, final String uniqueKey) {
        String quotedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT MIN(%s), MAX(%s) FROM %s", quotedUniqueKey, quotedUniqueKey, sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    /**
     * Build query all ordering SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key, it may be primary key, not null
     * @param firstQuery first query
     * @return query SQL
     */
    public String buildQueryAllOrderingSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey, final boolean firstQuery) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return firstQuery
                ? String.format("SELECT %s FROM %s ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey)
                : String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey, quotedUniqueKey);
    }
    
    private String buildQueryColumns(final List<String> columnNames) {
        return columnNames.isEmpty() ? "*" : columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
    }
    
    /**
     * Build check empty SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return check SQL
     */
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildCheckEmptySQL(schemaName, tableName);
    }
    
    /**
     * Build CRC32 SQL.
     *
     * @param schemaName schema name
     * @param tableName table Name
     * @param column column
     * @return CRC32 SQL
     */
    public Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return dialectSQLBuilder.buildCRC32SQL(schemaName, tableName, column);
    }
}
