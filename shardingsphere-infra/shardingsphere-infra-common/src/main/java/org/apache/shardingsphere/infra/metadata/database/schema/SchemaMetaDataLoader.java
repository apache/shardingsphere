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

package org.apache.shardingsphere.infra.metadata.database.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.database.MetaDataConnectionAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
     * Load unconfigured table name.
     *
     * @param dataSource data source
     * @param databaseType database type
     * @param excludedTableNames excluded table names
     * @return unconfigured table names
     * @throws SQLException SQL exception
     */
    public static Collection<String> loadUnconfiguredTableNames(final DataSource dataSource, final String databaseType, final Collection<String> excludedTableNames) throws SQLException {
        List<String> result;
        try (MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, dataSource.getConnection())) {
            result = loadAllTableNames(connectionAdapter);
            result.removeAll(excludedTableNames);
        }
        log.info("Loading {} tables' meta data.", result.size());
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        return result;
    }
    
    private static List<String> loadAllTableNames(final Connection connection) throws SQLException {
        List<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{TABLE_TYPE})) {
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
