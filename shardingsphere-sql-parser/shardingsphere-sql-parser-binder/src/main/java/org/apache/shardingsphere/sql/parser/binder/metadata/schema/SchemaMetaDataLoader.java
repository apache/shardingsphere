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

package org.apache.shardingsphere.sql.parser.binder.metadata.schema;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.binder.metadata.MetaDataConnection;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.util.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Schema meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-metadata")
public final class SchemaMetaDataLoader {
    
    private static final String TABLE_TYPE = "TABLE";

    private static final String VIEW_TYPE = "VIEW";

    private static final String TABLE_NAME = "TABLE_NAME";
    
    /**
     * Load schema meta data.
     *
     * @param dataSource data source
     * @param maxConnectionCount count of max connections permitted to use for this query
     * @param databaseType database type
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public static SchemaMetaData load(final DataSource dataSource, final int maxConnectionCount, final String databaseType) throws SQLException {
        return load(dataSource, maxConnectionCount, databaseType, Collections.emptyList());
    }
    
    /**
     * Load schema meta data.
     *
     * @param dataSource data source
     * @param maxConnectionCount count of max connections permitted to use for this query
     * @param databaseType database type
     * @param excludedTableNames excluded table names
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public static SchemaMetaData load(final DataSource dataSource, final int maxConnectionCount, final String databaseType, final Collection<String> excludedTableNames) throws SQLException {
        List<String> tableNames;
        try (MetaDataConnection connection = new MetaDataConnection(dataSource.getConnection())) {
            tableNames = loadAllTableNames(connection, databaseType);
            tableNames.removeAll(excludedTableNames);
        }
        log.info("Loading {} tables' meta data.", tableNames.size());
        if (tableNames.isEmpty()) {
            return new SchemaMetaData(Collections.emptyMap());
        }
        List<List<String>> tableGroups = Lists.partition(tableNames, Math.max(tableNames.size() / maxConnectionCount, 1));
        Map<String, TableMetaData> tableMetaDataMap = 1 == tableGroups.size()
                ? load(dataSource.getConnection(), tableGroups.get(0), databaseType) : asyncLoad(dataSource, maxConnectionCount, tableNames, tableGroups, databaseType);
        return new SchemaMetaData(tableMetaDataMap);
    }
    
    private static Map<String, TableMetaData> load(final Connection con, final Collection<String> tables, final String databaseType) throws SQLException {
        try (MetaDataConnection connection = new MetaDataConnection(con)) {
            Map<String, TableMetaData> result = new LinkedHashMap<>(tables.size(), 1);
            for (String each : tables) {
                result.put(each, new TableMetaData(ColumnMetaDataLoader.load(connection, each, databaseType), IndexMetaDataLoader.load(connection, each, databaseType)));
            }
            return result;
        }
    }
    
    private static List<String> loadAllTableNames(final Connection connection, final String databaseType) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), JdbcUtil.getSchema(connection, databaseType), null, new String[]{TABLE_TYPE, VIEW_TYPE})) {
            while (resultSet.next()) {
                String table = resultSet.getString(TABLE_NAME);
                if (!isSystemTable(table)) {
                    result.add(table);
                }
            }
        }
        return result;
    }
    
    private static boolean isSystemTable(final String table) {
        return table.contains("$") || table.contains("/");
    }
    
    private static Map<String, TableMetaData> asyncLoad(final DataSource dataSource, final int maxConnectionCount, final List<String> tableNames,
                                                        final List<List<String>> tableGroups, final String databaseType) throws SQLException {
        Map<String, TableMetaData> result = new ConcurrentHashMap<>(tableNames.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(tableGroups.size(), maxConnectionCount));
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (List<String> each : tableGroups) {
            futures.add(executorService.submit(() -> load(dataSource.getConnection(), each, databaseType)));
        }
        for (Future<Map<String, TableMetaData>> each : futures) {
            try {
                result.putAll(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                Thread.currentThread().interrupt();
            }
        }
        return result;
    }
}
