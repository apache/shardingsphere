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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract pipeline SQL builder.
 */
public abstract class AbstractPipelineSQLBuilder implements PipelineSQLBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final ConcurrentMap<String, String> sqlCacheMap = new ConcurrentHashMap<>();
    
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, Set<String>> shardingColumnsMap;
    
    public AbstractPipelineSQLBuilder() {
        shardingColumnsMap = Collections.emptyMap();
    }
    
    public AbstractPipelineSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
        this.shardingColumnsMap = shardingColumnsMap;
    }
    
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
    
    /**
     * Add left and right identifier quote string.
     *
     * @param item to add quote item
     * @return add quote string
     */
    public StringBuilder quote(final String item) {
        return new StringBuilder().append(getLeftIdentifierQuoteString()).append(item).append(getRightIdentifierQuoteString());
    }
    
    @Override
    public String buildInsertSQL(final DataRecord dataRecord) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(dataRecord.getTableName(), dataRecord.getColumns()));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildInsertSQLInternal(final String tableName, final List<Column> columns) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (Column each : columns) {
            columnsLiteral.append(String.format("%s,", quote(each.getName())));
            holder.append("?,");
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        return String.format("INSERT INTO %s(%s) VALUES(%s)", quote(tableName), columnsLiteral, holder);
    }
    
    @Override
    public String buildUpdateSQL(final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(dataRecord.getTableName(), conditionColumns));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (Column each : extractUpdatedColumns(dataRecord.getColumns(), dataRecord)) {
            updatedColumnString.append(String.format("%s = ?,", quote(each.getName())));
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        return String.format(sqlCacheMap.get(sqlCacheKey), updatedColumnString);
    }
    
    private String buildUpdateSQLInternal(final String tableName, final Collection<Column> conditionColumns) {
        return String.format("UPDATE %s SET %%s WHERE %s", quote(tableName), buildWhereSQL(conditionColumns));
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final Collection<Column> columns, final DataRecord record) {
        return new ArrayList<>(RecordUtil.extractUpdatedColumns(record));
    }
    
    @Override
    public String buildDeleteSQL(final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(dataRecord.getTableName(), conditionColumns));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    @Override
    public String buildTruncateSQL(final String tableName) {
        return String.format("TRUNCATE TABLE %s", quote(tableName));
    }
    
    private String buildDeleteSQLInternal(final String tableName, final Collection<Column> conditionColumns) {
        return String.format("DELETE FROM %s WHERE %s", quote(tableName), buildWhereSQL(conditionColumns));
    }
    
    private String buildWhereSQL(final Collection<Column> conditionColumns) {
        StringBuilder where = new StringBuilder();
        for (Column each : conditionColumns) {
            where.append(String.format("%s = ? and ", quote(each.getName())));
        }
        where.setLength(where.length() - 5);
        return where.toString();
    }
    
    @Override
    public String buildCountSQL(final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", quote(tableName));
    }
    
    @Override
    public String buildChunkedQuerySQL(final String tableName, final String uniqueKey, final Number startUniqueValue) {
        Preconditions.checkNotNull(uniqueKey, "uniqueKey is null");
        Preconditions.checkNotNull(startUniqueValue, "startUniqueValue is null");
        return "SELECT * FROM " + quote(tableName) + " WHERE " + quote(uniqueKey) + " > ? ORDER BY " + quote(uniqueKey) + " ASC LIMIT ?";
    }
    
    @Override
    public String buildCheckEmptySQL(final String tableName) {
        return String.format("SELECT * FROM %s LIMIT 1", quote(tableName));
    }
    
    @Override
    public String buildSplitByPrimaryKeyRangeSQL(final String tableName, final String primaryKey) {
        String quotedKey = quote(primaryKey).toString();
        return String.format("SELECT MAX(%s) FROM (SELECT %s FROM %s WHERE %s>=? ORDER BY %s LIMIT ?) t", quotedKey, quotedKey, quote(tableName), quotedKey, quotedKey);
    }
}
