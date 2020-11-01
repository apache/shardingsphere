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

package org.apache.shardingsphere.scaling.core.execute.executor.importer;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.RecordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract SQL builder.
 */
@RequiredArgsConstructor
public abstract class AbstractSQLBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final Map<String, Set<String>> shardingColumnsMap;
    
    private final ConcurrentMap<String, PreparedSQL> sqlCacheMap = new ConcurrentHashMap<>();
    
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
    protected StringBuilder quote(final String item) {
        return new StringBuilder().append(getLeftIdentifierQuoteString()).append(item).append(getRightIdentifierQuoteString());
    }
    
    /**
     * Build insert SQL.
     *
     * @param dataRecord data record
     * @return insert prepared SQL
     */
    public PreparedSQL buildInsertSQL(final DataRecord dataRecord) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(dataRecord));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    protected PreparedSQL buildInsertSQLInternal(final DataRecord dataRecord) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        List<Integer> valuesIndex = new ArrayList<>();
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            columnsLiteral.append(String.format("%s,", quote(dataRecord.getColumn(i).getName())));
            holder.append("?,");
            valuesIndex.add(i);
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        return new PreparedSQL(
                String.format("INSERT INTO %s(%s) VALUES(%s)", quote(dataRecord.getTableName()), columnsLiteral, holder),
                valuesIndex);
    }
    
    /**
     * Build update SQL.
     *
     * @param dataRecord data record
     * @return update prepared SQL
     */
    public PreparedSQL buildUpdateSQL(final DataRecord dataRecord) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(dataRecord));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        List<Integer> valuesIndex = new ArrayList<>();
        for (Integer each : RecordUtil.extractUpdatedColumns(dataRecord)) {
            updatedColumnString.append(String.format("%s = ?,", quote(dataRecord.getColumn(each).getName())));
            valuesIndex.add(each);
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        PreparedSQL preparedSQL = sqlCacheMap.get(sqlCacheKey);
        valuesIndex.addAll(preparedSQL.getValuesIndex());
        return new PreparedSQL(
                String.format(preparedSQL.getSql(), updatedColumnString),
                valuesIndex);
    }
    
    private PreparedSQL buildUpdateSQLInternal(final DataRecord dataRecord) {
        List<Integer> valuesIndex = new ArrayList<>();
        return new PreparedSQL(
                String.format("UPDATE %s SET %%s WHERE %s", quote(dataRecord.getTableName()), buildWhereSQL(dataRecord, valuesIndex)),
                valuesIndex);
    }
    
    /**
     * Build delete SQL.
     *
     * @param dataRecord data record
     * @return delete prepared SQL
     */
    public PreparedSQL buildDeleteSQL(final DataRecord dataRecord) {
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(dataRecord));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private PreparedSQL buildDeleteSQLInternal(final DataRecord dataRecord) {
        List<Integer> columnsIndex = new ArrayList<>();
        return new PreparedSQL(
                String.format("DELETE FROM %s WHERE %s", quote(dataRecord.getTableName()), buildWhereSQL(dataRecord, columnsIndex)),
                columnsIndex);
    }
    
    private String buildWhereSQL(final DataRecord dataRecord, final List<Integer> valuesIndex) {
        StringBuilder where = new StringBuilder();
        for (Integer each : RecordUtil.extractConditionColumns(dataRecord, shardingColumnsMap.get(dataRecord.getTableName()))) {
            where.append(String.format("%s = ? and ", quote(dataRecord.getColumn(each).getName())));
            valuesIndex.add(each);
        }
        where.setLength(where.length() - 5);
        return where.toString();
    }
    
    /**
     * Build count SQL.
     *
     * @param tableName table name
     * @return count SQL
     */
    public String buildCountSQL(final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", quote(tableName));
    }
}
