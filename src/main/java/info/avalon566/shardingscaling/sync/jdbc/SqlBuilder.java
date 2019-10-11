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

package info.avalon566.shardingscaling.sync.jdbc;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Sql builder.
 *
 * @author avalon566
 */
public final class SqlBuilder {
    
    private static final String LEFT_ESCAPE_QUOTE = "`";

    private static final String RIGHT_ESCAPE_QUOTE = "`";

    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";

    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";

    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";

    private final RdbmsConfiguration rdbmsConfiguration;

    private final LoadingCache<String, String> sqlCache;

    private final DbMetaDataUtil dbMetaDataUtil;

    public SqlBuilder(final RdbmsConfiguration rdbmsConfiguration) {
        this.rdbmsConfiguration = rdbmsConfiguration;
        this.dbMetaDataUtil = new DbMetaDataUtil(rdbmsConfiguration);
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
     * @return sql
     */
    public String buildUpdateSql(final String tableName) {
        try {
            return sqlCache.get(UPDATE_SQL_CACHE_KEY_PREFIX + tableName);
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
            columns.append(String.format("%s%s%s,", LEFT_ESCAPE_QUOTE, each.getColumnName(), RIGHT_ESCAPE_QUOTE));
            holder.append("?,");
        }
        columns.setLength(columns.length() - 1);
        holder.setLength(holder.length() - 1);
        return String.format("INSERT INTO %s%s%s(%s) VALUES(%s)", LEFT_ESCAPE_QUOTE, tableName, RIGHT_ESCAPE_QUOTE, columns.toString(), holder.toString());
    }

    private String buildDeleteSqlInternal(final String tableName) {
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(tableName);
        StringBuilder where = new StringBuilder();
        for (String each : primaryKeys) {
            where.append(String.format("%s%s%s = ?,", LEFT_ESCAPE_QUOTE, each, RIGHT_ESCAPE_QUOTE));
        }
        where.setLength(where.length() - 1);
        return String.format("DELETE FROM %s%s%s WHERE %s", LEFT_ESCAPE_QUOTE, tableName, RIGHT_ESCAPE_QUOTE, where.toString());
    }

    private String buildUpdateSqlInternal(final String tableName) {
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(tableName);
        StringBuilder where = new StringBuilder();
        for (String each : primaryKeys) {
            where.append(String.format("%s%s%s = ?,", LEFT_ESCAPE_QUOTE, each, RIGHT_ESCAPE_QUOTE));
        }
        where.setLength(where.length() - 1);
        return String.format("UPDATE %s%s%s SET %%s WHERE %s", LEFT_ESCAPE_QUOTE, tableName, RIGHT_ESCAPE_QUOTE, where.toString());
    }
}
