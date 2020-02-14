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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.writer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.shardingscaling.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.shardingscaling.core.metadata.DbMetaDataUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Abstract sql builder.
 *
 * @author avalon566
 */
public abstract class AbstractSqlBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final LoadingCache<String, String> sqlCache;
    
    @Getter(value = AccessLevel.PROTECTED)
    private final DbMetaDataUtil dbMetaDataUtil;
    
    public AbstractSqlBuilder(final DbMetaDataUtil dbMetaDataUtil) {
        this.dbMetaDataUtil = dbMetaDataUtil;
        sqlCache = CacheBuilder.newBuilder()
                .maximumSize(64)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(final String key) {
                        if (key.startsWith(INSERT_SQL_CACHE_KEY_PREFIX)) {
                            return buildInsertSqlInternal(key.replaceFirst(INSERT_SQL_CACHE_KEY_PREFIX, ""));
                        } else if (key.startsWith(UPDATE_SQL_CACHE_KEY_PREFIX)) {
                            return buildUpdateSqlInternal(key.replaceFirst(UPDATE_SQL_CACHE_KEY_PREFIX, ""));
                        } else {
                            return buildDeleteSqlInternal(key.replaceFirst(DELETE_SQL_CACHE_KEY_PREFIX, ""));
                        }
                    }
                });
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
     * Build insert sql.
     *
     * @param tableName table name
     * @return sql
     */
    public String buildInsertSql(final String tableName) {
        try {
            return sqlCache.get(INSERT_SQL_CACHE_KEY_PREFIX + tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Build update sql.
     *
     * @param tableName table name
     * @param updatedColumns of updated
     * @return sql
     */
    public String buildUpdateSql(final String tableName, final List<ColumnMetaData> updatedColumns) {
        try {
            StringBuilder updatedColumnString = new StringBuilder();
            for (ColumnMetaData columnMetaData : updatedColumns) {
                updatedColumnString.append(String.format("%s%s%s = ?,", getLeftIdentifierQuoteString(), columnMetaData.getColumnName(), getRightIdentifierQuoteString()));
            }
            return String.format(sqlCache.get(UPDATE_SQL_CACHE_KEY_PREFIX + tableName), updatedColumnString.substring(0, updatedColumnString.length() - 1));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Build delete sql.
     *
     * @param tableName table name
     * @return sql
     */
    public String buildDeleteSql(final String tableName) {
        try {
            return sqlCache.get(DELETE_SQL_CACHE_KEY_PREFIX + tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String buildInsertSqlInternal(final String tableName) {
        List<ColumnMetaData> metaData = dbMetaDataUtil.getColumnNames(tableName);
        StringBuilder columns = new StringBuilder();
        StringBuilder holder = new StringBuilder();
        for (ColumnMetaData each : metaData) {
            columns.append(String.format("%s%s%s,", getLeftIdentifierQuoteString(), each.getColumnName(), getRightIdentifierQuoteString()));
            holder.append("?,");
        }
        columns.setLength(columns.length() - 1);
        holder.setLength(holder.length() - 1);
        return String.format("INSERT INTO %s%s%s(%s) VALUES(%s)", getLeftIdentifierQuoteString(), tableName, getRightIdentifierQuoteString(), columns.toString(), holder.toString());
    }
    
    private String buildDeleteSqlInternal(final String tableName) {
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(tableName);
        StringBuilder where = new StringBuilder();
        for (String each : primaryKeys) {
            where.append(String.format("%s%s%s = ?,", getLeftIdentifierQuoteString(), each, getRightIdentifierQuoteString()));
        }
        where.setLength(where.length() - 1);
        return String.format("DELETE FROM %s%s%s WHERE %s", getLeftIdentifierQuoteString(), tableName, getRightIdentifierQuoteString(), where.toString());
    }
    
    private String buildUpdateSqlInternal(final String tableName) {
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(tableName);
        StringBuilder where = new StringBuilder();
        for (String each : primaryKeys) {
            where.append(String.format("%s%s%s = ?,", getLeftIdentifierQuoteString(), each, getRightIdentifierQuoteString()));
        }
        where.setLength(where.length() - 1);
        return String.format("UPDATE %s%s%s SET %%s WHERE %s", getLeftIdentifierQuoteString(), tableName, getRightIdentifierQuoteString(), where.toString());
    }
}
