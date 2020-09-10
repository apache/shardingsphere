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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.binder.metadata.MetaDataConnection;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.util.JdbcUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Schema meta data loader.
 * Note: this is only load table name, skip index and column info
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-metadata")
public final class SchemaMetaDataLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    /**
     * Load schema meta data.
     *
     * @param dataSource data source
     * @param databaseType database type
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public static SchemaMetaData load(final DataSource dataSource, final String databaseType) throws SQLException {
        return load(dataSource, databaseType, Collections.emptyList());
    }
    
    /**
     * Load schema meta data.
     *
     * @param dataSource data source
     * @param databaseType database type
     * @param excludedTableNames excluded table names
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public static SchemaMetaData load(final DataSource dataSource, final String databaseType, final Collection<String> excludedTableNames) throws SQLException {
        List<String> tableNames;
        try (MetaDataConnection connection = new MetaDataConnection(dataSource.getConnection())) {
            tableNames = loadAllTableNames(connection, databaseType);
            tableNames.removeAll(excludedTableNames);
        }
        log.info("Loading {} tables' meta data.", tableNames.size());
        if (tableNames.isEmpty()) {
            return new SchemaMetaData(Collections.emptyMap());
        }
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(tableNames.size(), 1);
        tableNames.forEach(tableName -> tableMetaDataMap.put(tableName, new TableMetaData(Collections.emptyList(), Collections.emptyList())));
        return new SchemaMetaData(tableMetaDataMap);
    }
    
    private static List<String> loadAllTableNames(final Connection connection, final String databaseType) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), JdbcUtil.getSchema(connection, databaseType), null, new String[]{TABLE_TYPE})) {
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
