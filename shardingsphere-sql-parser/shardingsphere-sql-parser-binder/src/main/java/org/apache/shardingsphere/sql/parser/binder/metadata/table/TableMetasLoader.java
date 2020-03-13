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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

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
     * @param connection connection
     * @param catalog catalog name
     * @param schema schema name
     * @return table metas
     * @throws SQLException SQL exception
     */
    public static TableMetas load(final Connection connection, final String catalog, final String schema) throws SQLException {
        Collection<String> tableNames = loadAllTableNames(connection, catalog, schema);
        Map<String, TableMetaData> result = new HashMap<>(tableNames.size(), 1);
        // TODO concurrency load via maxConnectionsSizePerQuery
        for (String each : tableNames) {
            result.put(each, new TableMetaData(ColumnMetaDataLoader.load(connection, catalog, each), IndexMetaDataLoader.load(connection, catalog, schema, each)));
        }
        return new TableMetas(result);
    }
    
    private static Collection<String> loadAllTableNames(final Connection connection, final String catalog, final String schema) throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
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
