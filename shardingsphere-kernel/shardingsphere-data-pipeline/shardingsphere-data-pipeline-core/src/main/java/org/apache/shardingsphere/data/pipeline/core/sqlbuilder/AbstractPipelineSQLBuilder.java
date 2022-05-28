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

import com.google.common.base.Strings;
import lombok.NonNull;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.util.ArrayList;
import java.util.Collection;
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
    public String quote(final String item) {
        return getLeftIdentifierQuoteString() + item + getRightIdentifierQuoteString();
    }
    
    @Override
    public String buildInventoryDumpSQL(final String schemaName, final String tableName, final String uniqueKey, final int uniqueKeyDataType, final boolean firstQuery) {
        String decoratedTableName = decorate(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        if (PipelineJdbcUtils.isIntegerColumn(uniqueKeyDataType)) {
            return "SELECT * FROM " + decoratedTableName + " WHERE " + quotedUniqueKey + " " + (firstQuery ? ">=" : ">") + " ?"
                    + " AND " + quotedUniqueKey + " <= ? ORDER BY " + quotedUniqueKey + " ASC LIMIT ?";
        } else if (PipelineJdbcUtils.isStringColumn(uniqueKeyDataType)) {
            return "SELECT * FROM " + decoratedTableName + " WHERE " + quotedUniqueKey + " " + (firstQuery ? ">=" : ">") + " ?"
                    + " ORDER BY " + quotedUniqueKey + " ASC LIMIT ?";
        } else {
            throw new IllegalArgumentException("Unknown uniqueKeyDataType: " + uniqueKeyDataType);
        }
    }
    
    protected String decorate(final String schemaName, final String tableName) {
        StringBuilder result = new StringBuilder();
        if (isSchemaAvailable() && !Strings.isNullOrEmpty(schemaName)) {
            result.append(quote(schemaName)).append(".");
        }
        result.append(quote(tableName));
        return result.toString();
    }
    
    private boolean isSchemaAvailable() {
        return DatabaseTypeFactory.getInstance(getType()).isSchemaAvailable();
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
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
        return String.format("INSERT INTO %s(%s) VALUES(%s)", decorate(schemaName, tableName), columnsLiteral, holder);
    }
    
    // TODO seems sharding column could be updated for insert statement on conflict by kernel now
    protected final boolean isShardingColumn(final Map<LogicTableName, Set<String>> shardingColumnsMap, final String tableName, final String columnName) {
        Set<String> shardingColumns = shardingColumnsMap.get(new LogicTableName(tableName));
        return null != shardingColumns && shardingColumns.contains(columnName);
    }
    
    @Override
    public String buildUpdateSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (!sqlCacheMap.containsKey(sqlCacheKey)) {
            sqlCacheMap.put(sqlCacheKey, buildUpdateSQLInternal(schemaName, dataRecord.getTableName(), conditionColumns));
        }
        StringBuilder updatedColumnString = new StringBuilder();
        for (Column each : extractUpdatedColumns(dataRecord, shardingColumnsMap)) {
            updatedColumnString.append(String.format("%s = ?,", quote(each.getName())));
        }
        updatedColumnString.setLength(updatedColumnString.length() - 1);
        return String.format(sqlCacheMap.get(sqlCacheKey), updatedColumnString);
    }
    
    private String buildUpdateSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("UPDATE %s SET %%s WHERE %s", decorate(schemaName, tableName), buildWhereSQL(conditionColumns));
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final DataRecord record, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        return new ArrayList<>(RecordUtil.extractUpdatedColumns(record));
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
    public String buildTruncateSQL(final String schemaName, final String tableName) {
        return String.format("TRUNCATE TABLE %s", decorate(schemaName, tableName));
    }
    
    private String buildDeleteSQLInternal(final String schemaName, final String tableName, final Collection<Column> conditionColumns) {
        return String.format("DELETE FROM %s WHERE %s", decorate(schemaName, tableName), buildWhereSQL(conditionColumns));
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
    public String buildCountSQL(final String schemaName, final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", decorate(schemaName, tableName));
    }
    
    @Override
    public String buildChunkedQuerySQL(final @NonNull String schemaName, final @NonNull String tableName, final @NonNull String uniqueKey, final boolean firstQuery) {
        if (firstQuery) {
            return "SELECT * FROM " + decorate(schemaName, tableName) + " ORDER BY " + quote(uniqueKey) + " ASC LIMIT ?";
        } else {
            return "SELECT * FROM " + decorate(schemaName, tableName) + " WHERE " + quote(uniqueKey) + " > ? ORDER BY " + quote(uniqueKey) + " ASC LIMIT ?";
        }
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM %s LIMIT 1", decorate(schemaName, tableName));
    }
    
    @Override
    public String buildSplitByPrimaryKeyRangeSQL(final String schemaName, final String tableName, final String primaryKey) {
        String quotedKey = quote(primaryKey);
        return String.format("SELECT MAX(%s) FROM (SELECT %s FROM %s WHERE %s>=? ORDER BY %s LIMIT ?) t", quotedKey, quotedKey, decorate(schemaName, tableName), quotedKey, quotedKey);
    }
}
