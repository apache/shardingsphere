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

package org.apache.shardingsphere.infra.metadata.schema.loader.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.loader.adapter.MetaDataLoaderConnectionAdapter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    /**
     * Load all table names.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @return loaded all table names
     * @throws SQLException SQL exception
     */
    public static Collection<String> loadAllTableNames(final DatabaseType databaseType, final DataSource dataSource) throws SQLException {
        try (MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            return loadAllTableNames(connectionAdapter);
        }
    }
    
    private static Collection<String> loadAllTableNames(final Connection connection) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{TABLE_TYPE, VIEW_TYPE})) {
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
