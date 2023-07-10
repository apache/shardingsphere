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

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.record.RecordUtils;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Abstract pipeline SQL builder.
 */
public abstract class AbstractPipelineSQLBuilder implements PipelineSQLBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final ConcurrentMap<String, String> sqlCacheMap = new ConcurrentHashMap<>();
    
    /**
     * Add left and right identifier quote string.
     *
     * @param item to add quote item
     * @return add quote string
     */
    public String quote(final String item) {
        return isKeyword(item) ? getLeftIdentifierQuoteString() + item + getRightIdentifierQuoteString() : item;
    }
    
    protected abstract boolean isKeyword(String item);
    
    /**
     * Get left identifier quote string.
     *
     * @return string
     */
    protected abstract String getLeftIdentifierQuoteString();
    
    /**
     * Get right identifier quote string.
     *
     * @return string
     */
    protected abstract String getRightIdentifierQuoteString();
    
    @Override
    public String buildDivisibleInventoryDumpSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT %s FROM %s WHERE %s>=? AND %s<=? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey, quotedUniqueKey, quotedUniqueKey);
    }
    
    private String buildQueryColumns(final List<String> columnNames) {
        if (columnNames.isEmpty()) {
            return "*";
        }
        return columnNames.stream().map(this::quote).collect(Collectors.joining(","));
    }
    
    @Override
    public String buildDivisibleInventoryDumpSQLNoEnd(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT %s FROM %s WHERE %s>=? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey, quotedUniqueKey);
    }
    
    @Override
    public String buildIndivisibleInventoryDumpSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT %s FROM %s ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey);
    }
    
    @Override
    public String buildNoUniqueKeyInventoryDumpSQL(final String schemaName, final String tableName) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        return String.format("SELECT * FROM %s", qualifiedTableName);
    }
    
    protected final String getQualifiedTableName(final String schemaName, final String tableName) {
        StringBuilder result = new StringBuilder();
        if (getType().isSchemaAvailable() && !Strings.isNullOrEmpty(schemaName)) {
            result.append(quote(schemaName)).append('.');
        }
        result.append(quote(tableName));
        return result.toString();
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(schemaName, dataRecord.getTableName(), dataRecord.getColumns()));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildInsertSQLInternal(final String schemaName, final String tableName, final List<Column> columns) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (Column each : columns) {
            columnsLiteral.append(String.format("%s,", quote(each.getName())));
            holder.append("?,");
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        return String.format("INSERT INTO %s(%s) VALUES(%s)", getQualifiedTableName(schemaName, tableName), columnsLiteral, holder);
    }
    
    @Override
    public String buildUpdateSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(schemaName, dataRecord.getTableName(), conditionColumns));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (Column each : extractUpdatedColumns(dataRecord)) {
            updatedColumnString.append(String.format("%s = ?,", quote(each.getName())));
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        return String.format(sqlCacheMap.get(sqlCacheKey), updatedColumnString);
    }
    
    private String buildUpdateSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("UPDATE %s SET %%s WHERE %s", getQualifiedTableName(schemaName, tableName), buildWhereSQL(conditionColumns));
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        return new ArrayList<>(RecordUtils.extractUpdatedColumns(dataRecord));
    }
    
    @Override
    public String buildDeleteSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(schemaName, dataRecord.getTableName(), conditionColumns));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    @Override
    public String buildDropSQL(final String schemaName, final String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", getQualifiedTableName(schemaName, tableName));
    }
    
    private String buildDeleteSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("DELETE FROM %s WHERE %s", getQualifiedTableName(schemaName, tableName), buildWhereSQL(conditionColumns));
    }
    
    private String buildWhereSQL(final Collection<Column> conditionColumns) {
        StringBuilder where = new StringBuilder();
        for (Column each : conditionColumns) {
            where.append(String.format("%s = ? AND ", quote(each.getName())));
        }
        where.setLength(where.length() - 5);
        return where.toString();
    }
    
    @Override
    public String buildCountSQL(final String schemaName, final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", getQualifiedTableName(schemaName, tableName));
    }
    
    @Override
    public String buildUniqueKeyMinMaxValuesSQL(final String schemaName, final String tableName, final String uniqueKey) {
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT MIN(%s), MAX(%s) FROM %s", quotedUniqueKey, quotedUniqueKey, getQualifiedTableName(schemaName, tableName));
    }
    
    @Override
    public String buildQueryAllOrderingSQL(final String schemaName, final String tableName, final List<String> columnNames, final String uniqueKey, final boolean firstQuery) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return firstQuery
                ? String.format("SELECT %s FROM %s ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey)
                : String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s ASC", buildQueryColumns(columnNames), qualifiedTableName, quotedUniqueKey, quotedUniqueKey);
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM %s LIMIT 1", getQualifiedTableName(schemaName, tableName));
    }
}
