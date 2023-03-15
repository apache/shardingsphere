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
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record.MetaData;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.TableColumn;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
    public String buildInsertSQL(final Record record, final List<String> uniqueKeyNames) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + record.getMetaData().getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildInsertSQLInternal(record));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildInsertSQLInternal(final Record record) {
        StringBuilder columnsLiteral = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (TableColumn each : record.getAfterList()) {
            columnsLiteral.append(String.format("%s,", quote(each.getName())));
            holder.append("?,");
        }
        columnsLiteral.setLength(columnsLiteral.length() - 1);
        holder.setLength(holder.length() - 1);
        MetaData metaData = record.getMetaData();
        return String.format("INSERT INTO %s(%s) VALUES(%s)", getQualifiedTableName(metaData.getSchema(), metaData.getTableName()), columnsLiteral, holder);
    }
    
    @Override
    public String buildUpdateSQL(final Record record, final List<String> uniqueKeyNames) {
        MetaData metaData = record.getMetaData();
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + metaData.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            List<String> columnNames = record.getBeforeList().stream().map(TableColumn::getName).collect(Collectors.toList());
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(metaData.getSchema(), metaData.getTableName(), columnNames, uniqueKeyNames));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (TableColumn each : record.getAfterList()) {
            updatedColumnString.append(String.format("%s = ?,", quote(each.getName())));
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
    public String buildDeleteSQL(final Record record, final List<String> uniqueKeyNames) {
        MetaData metaData = record.getMetaData();
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + metaData.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            List<String> columnNames = record.getBeforeList().stream().map(TableColumn::getName).collect(Collectors.toList());
            sqlCacheMap.put(sqlCacheKey, buildDeleteSQLInternal(metaData.getSchema(), metaData.getTableName(), columnNames, uniqueKeyNames));
        }
        return sqlCacheMap.get(sqlCacheKey);
    }
    
    private String buildDeleteSQLInternal(final String schemaName, final String tableName, final List<String> columnNames, final List<String> uniqueKeyNames) {
        return String.format("DELETE FROM %s WHERE %s", getQualifiedTableName(schemaName, tableName), buildWhereSQL(columnNames, uniqueKeyNames));
    }
}
