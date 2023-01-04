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

package org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.TableMetaData;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract SQL builder.
 */
public abstract class AbstractSQLBuilder implements SQLBuilder {
    
    protected static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    protected static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    protected static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    @Getter
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
    
    protected final String getQualifiedTableName(final String schemaName, final String tableName) {
        StringBuilder result = new StringBuilder();
        if (!Strings.isNullOrEmpty(schemaName)) {
            result.append(quote(schemaName)).append(".");
        }
        result.append(quote(tableName));
        return result.toString();
    }
    
    @Override
    public String buildInsertSQL(final Record record) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + record.getTableMetaData().getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(record));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildInsertSQLInternal(final Record record) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (String each : record.getAfterMap().keySet()) {
            columnsLiteral.append(String.format("%s,", quote(each)));
            holder.append("?,");
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        TableMetaData tableMetaData = record.getTableMetaData();
        return String.format("INSERT INTO %s(%s) VALUES(%s)", getQualifiedTableName(tableMetaData.getSchema(), tableMetaData.getTableName()), columnsLiteral, holder);
    }
    
    @Override
    public String buildUpdateSQL(final Record record) {
        TableMetaData tableMetaData = record.getTableMetaData();
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + tableMetaData.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(tableMetaData.getSchema(), tableMetaData.getTableName(), record.getBeforeMap().keySet(), tableMetaData.getUniqueKeyNamesList()));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (String each : record.getAfterMap().keySet()) {
            updatedColumnString.append(String.format("%s = ?,", quote(each)));
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        return String.format(sqlCacheMap.get(sqlCacheKey), updatedColumnString);
    }
    
    private String buildUpdateSQLInternal(final String schemaName, final String tableName, final Collection<String> columnNames, final Collection<String> uniqueKeyNames) {
        return String.format("UPDATE %s SET %%s WHERE %s", getQualifiedTableName(schemaName, tableName), buildWhereSQL(columnNames, uniqueKeyNames));
    }
    
    private String buildWhereSQL(final Collection<String> columnNames, final Collection<String> uniqueKeyNames) {
        StringBuilder where = new StringBuilder();
        for (String each : columnNames.containsAll(uniqueKeyNames) ? uniqueKeyNames : columnNames) {
            where.append(String.format("%s = ? and ", quote(each)));
        }
        where.setLength(where.length() - 5);
        return where.toString();
    }
    
    /**
     * Build delete SQL.
     *
     * @param record record
     * @return delete SQL
     */
    @Override
    public String buildDeleteSQL(final Record record) {
        TableMetaData tableMetaData = record.getTableMetaData();
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + tableMetaData.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(tableMetaData.getSchema(), tableMetaData.getTableName(), record.getBeforeMap().keySet(), tableMetaData.getUniqueKeyNamesList()));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildDeleteSQLInternal(final String schemaName, final String tableName, final Collection<String> columnNames, final Collection<String> uniqueKeyNames) {
        return String.format("DELETE FROM %s WHERE %s", getQualifiedTableName(schemaName, tableName), buildWhereSQL(columnNames, uniqueKeyNames));
    }
}
