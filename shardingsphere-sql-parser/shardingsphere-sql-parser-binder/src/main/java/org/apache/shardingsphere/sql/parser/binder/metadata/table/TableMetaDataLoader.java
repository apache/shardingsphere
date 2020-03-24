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

package org.apache.shardingsphere.sql.parser.binder.metadata.table;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    
    private static final int FUTURE_GET_TIME_OUT_SEC = 5;
    
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShadingSphere-Meta-table").build();
    
    /**
     * Load table meta data.
     * @param dataSource data source
     * @param table table name
     * @param databaseType database type
     *
     * @return table meta data
     *
     * @throws SQLException SQL exception
     */
    public static TableMetaData load(final DataSource dataSource, final String table, final String databaseType) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return new TableMetaData(ColumnMetaDataLoader.load(connection, table, databaseType), IndexMetaDataLoader.load(connection, table));
        }
    }
    
    /**
     * Load table meta data.
     * @param dataSource the data source
     * @param tables the tables
     * @param databaseType the database type
     *
     * @return the table meta data
     *
     * @throws SQLException the sql exception
     */
    public static Map<String, TableMetaData> load(final DataSource dataSource, final Collection<String> tables, final String databaseType) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Map<String, TableMetaData> result = new LinkedHashMap<>();
            for (String each : tables) {
                result.put(each, new TableMetaData(ColumnMetaDataLoader.load(con, each, databaseType), IndexMetaDataLoader.load(con, each)));
            }
            return result;
        }
    }
    
    /**
     * Async load map.
     * @param dataSourceNodes the data source nodes
     * @param databaseType the database type
     *
     * @return the map
     */
    public static Map<String, Map<String, TableMetaData>> asyncLoad(final Map<DataSource, Map<String, List<String>>> dataSourceNodes, final String databaseType) {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CORES * 2, dataSourceNodes.size()), THREAD_FACTORY);
        Map<String, Future<Map<String, TableMetaData>>> tableFutureMap = new HashMap<>(dataSourceNodes.size(), 1);
        Map<String, Map<String, TableMetaData>> result = new LinkedHashMap<>();
        for (Map.Entry<DataSource, Map<String, List<String>>> node : dataSourceNodes.entrySet()) {
            for (Map.Entry<String, List<String>> entry : node.getValue().entrySet()) {
                Future<Map<String, TableMetaData>> futures = executorService.submit(() -> load(node.getKey(), entry.getValue(), databaseType));
                tableFutureMap.put(entry.getKey(), futures);
            }
        }
        tableFutureMap.forEach((key, value) -> {
            try {
                Map<String, TableMetaData> tableMetaData = value.get(FUTURE_GET_TIME_OUT_SEC, TimeUnit.SECONDS);
                result.put(key, tableMetaData);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException(String.format("Error while fetching tableMetaData with key= %s and Value=%s", key, value), e);
            }
        });
        executorService.shutdownNow();
        return result;
    }
    
    /**
     * Async load map.
     * @param dataSource the data source
     * @param maxConnectionCount the max connection count
     * @param tableNames the table names
     * @param tableGroups the table groups
     * @param databaseType the database type
     *
     * @return the map
     *
     */
    public static Map<String, TableMetaData> asyncLoad(final DataSource dataSource, final int maxConnectionCount, final List<String> tableNames,
                                                        final List<List<String>> tableGroups, final String databaseType) {
        Map<String, TableMetaData> result = new ConcurrentHashMap<>(tableNames.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(tableGroups.size(), maxConnectionCount), THREAD_FACTORY);
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (List<String> each : tableGroups) {
            futures.add(executorService.submit(() -> load(dataSource, each, databaseType)));
        }
        for (Future<Map<String, TableMetaData>> each : futures) {
            try {
                result.putAll(each.get(FUTURE_GET_TIME_OUT_SEC, TimeUnit.SECONDS));
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException("Error while fetching tableMetaData", e);
            }
        }
        executorService.shutdownNow();
        return result;
    }
    
}
