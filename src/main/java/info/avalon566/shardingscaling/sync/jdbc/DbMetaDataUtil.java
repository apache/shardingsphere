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
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Database meta util.
 *
 * @author avalon566
 */
@Slf4j
public final class DbMetaDataUtil {

    private static final String COLUMN_NAME = "COLUMN_NAME";

    private static final String TYPE_NAME = "TYPE_NAME";

    private static final String DATA_TYPE = "DATA_TYPE";

    private static final String TABLE_NAME = "TABLE_NAME";

    private final RdbmsConfiguration rdbmsConfiguration;

    private LoadingCache<String, List<String>> pksCache;

    private LoadingCache<String, List<ColumnMetaData>> cmdCache;

    public DbMetaDataUtil(final RdbmsConfiguration rdbmsConfiguration) {
        this.rdbmsConfiguration = rdbmsConfiguration;

        pksCache = CacheBuilder.newBuilder()
                .maximumSize(64)
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(final String key) {
                        return getPrimaryKeysInternal(key);
                    }
                });

        cmdCache = CacheBuilder.newBuilder()
                .maximumSize(64)
                .build(new CacheLoader<String, List<ColumnMetaData>>() {
                    @Override
                    public List<ColumnMetaData> load(final String key) {
                        return getColumNamesInternal(key);
                    }
                });
    }

    /**
     * Get primary key column name by table name.
     *
     * @param tableName table name
     * @return list of table name
     */
    public List<String> getPrimaryKeys(final String tableName) {
        try {
            return pksCache.get(tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getPrimaryKeysInternal(final String tableName) {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var rs = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, tableName);
                var primaryKeys = new ArrayList<String>(rs.getRow());
                while (rs.next()) {
                    primaryKeys.add(rs.getString(COLUMN_NAME));
                }
                return primaryKeys;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    /**
     * Get all table names in current database.
     *
     * @return list of table name
     */
    public List<String> getTableNames() {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"});
                var tableNames = new LinkedList<String>();
                while (rs.next()) {
                    tableNames.add(rs.getString(TABLE_NAME));
                }
                return tableNames;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    /**
     * Get all column meta data by table name.
     *
     * @param tableName table name
     * @return list of column meta data
     */
    public List<ColumnMetaData> getColumNames(final String tableName) {
        try {
            return cmdCache.get(tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<ColumnMetaData> getColumNamesInternal(final String tableName) {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var result = new ArrayList<ColumnMetaData>();
                ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), tableName, "%");
                while (resultSet.next()) {
                    var columnMetaData = new ColumnMetaData();
                    columnMetaData.setColumnName(resultSet.getString(COLUMN_NAME));
                    columnMetaData.setColumnType(resultSet.getInt(DATA_TYPE));
                    columnMetaData.setColumnTypeName(resultSet.getString(TYPE_NAME));
                    result.add(columnMetaData);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }

    /**
     * Find column index by column name.
     *
     * @param metaData   meta data list
     * @param columnName table name
     * @return index
     */
    public static int findColumnIndex(final List<ColumnMetaData> metaData, final String columnName) {
        for (int i = 0; i < metaData.size(); i++) {
            if (metaData.get(i).getColumnName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
