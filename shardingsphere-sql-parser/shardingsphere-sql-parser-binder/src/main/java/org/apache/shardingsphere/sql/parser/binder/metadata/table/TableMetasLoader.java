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

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
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
 * Table metas loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetasLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    /**
     * Load table metas.
     *
     * @param dataSource data source
     * @param catalog catalog name
     * @param schema schema name
     * @param maxConnectionCount count of max connections permitted to use for this query
     * @return table metas
     * @throws SQLException SQL exception
     */
    public static TableMetas load(final DataSource dataSource, final String catalog, final String schema, final int maxConnectionCount) throws SQLException {
        List<String> tableNames;
        try (Connection connection = dataSource.getConnection()) {
            tableNames = loadAllTableNames(connection, catalog, schema);
        }
        List<List<String>> tableGroups = Lists.partition(tableNames, Math.max(tableNames.size() / maxConnectionCount, 1));
        if (1 == tableGroups.size()) {
            return new TableMetas(load(dataSource.getConnection(), tableGroups.get(0), catalog, schema));
        }
        Map<String, TableMetaData> result = new ConcurrentHashMap<>(tableNames.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(maxConnectionCount);
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (List<String> each : tableGroups) {
            futures.add(executorService.submit(() -> load(dataSource.getConnection(), each, catalog, schema)));
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
        return new TableMetas(result);
    }
    
    private static Map<String, TableMetaData> load(final Connection connection, final Collection<String> tables, final String catalog, final String schema) throws SQLException {
        try (Connection con = connection) {
            Map<String, TableMetaData> result = new LinkedHashMap<>();
            for (String each : tables) {
                result.put(each, new TableMetaData(ColumnMetaDataLoader.load(con, catalog, each), IndexMetaDataLoader.load(con, catalog, schema, each)));
            }
            return result;
        }
        
    }
    
    private static List<String> loadAllTableNames(final Connection connection, final String catalog, final String schema) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, schema, null, new String[]{TABLE_TYPE})) {
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
}
