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
import lombok.var;

import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author avalon566
 */
public final class DbMetaDataUtil {

    private final RdbmsConfiguration rdbmsConfiguration;

    private LoadingCache<String, List<String>> pksCache;

    private LoadingCache<String, List<ColumnMetaData>> cmdCache;

    public DbMetaDataUtil(RdbmsConfiguration rdbmsConfiguration) {
        this.rdbmsConfiguration = rdbmsConfiguration;

        pksCache = CacheBuilder.newBuilder()
                .maximumSize(64)
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String key) {
                        return getPrimaryKeysInternal(key);
                    }
                });

        cmdCache = CacheBuilder.newBuilder()
                .maximumSize(64)
                .build(new CacheLoader<String, List<ColumnMetaData>>() {
                    @Override
                    public List<ColumnMetaData> load(String key) {
                        return getColumNamesInternal(key);
                    }
                });
    }

    public List<String> getPrimaryKeys(String tableName) {
        try {
            return pksCache.get(tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getPrimaryKeysInternal(String tableName) {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var rs = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, tableName);
                var primaryKeys = new ArrayList<String>(rs.getRow());
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
                return primaryKeys;
            }
        } catch (Exception e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    public List<String> getTableNames() {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"});
                var tableNames = new LinkedList<String>();
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
                return tableNames;
            }
        } catch (Exception e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    public List<ColumnMetaData> getColumNames(String tableName) {
        try {
            return cmdCache.get(tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ColumnMetaData> getColumNamesInternal(String tableName) {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var ps = connection.prepareStatement(String.format("select * from %s limit 1", tableName));
                var metaData = ps.executeQuery().getMetaData();
                var result = new ArrayList<ColumnMetaData>(metaData.getColumnCount());
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    var columnMetaData = new ColumnMetaData();
                    columnMetaData.setColumnName(metaData.getColumnName(i));
                    columnMetaData.setColumnType(metaData.getColumnType(i));
                    columnMetaData.setColumnTypeName(metaData.getColumnTypeName(i));
                    result.add(columnMetaData);
                }
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    public static int findColumnIndex(List<ColumnMetaData> metaData, String columnName) {
        try {
            for (int i = 0; i < metaData.size(); i++) {
                if (metaData.get(i).getColumnName().equals(columnName)) {
                    return i;
                }
            }
            return -1;
        } catch (Exception e) {
            throw new RuntimeException("findColumnIndex error", e);
        }
    }
}
