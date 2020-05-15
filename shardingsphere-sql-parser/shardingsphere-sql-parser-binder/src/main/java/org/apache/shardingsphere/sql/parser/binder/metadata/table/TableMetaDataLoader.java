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

import java.sql.ResultSet;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.metadata.MetaDataConnection;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param table table name
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> load(final DataSource dataSource, final String table, final String databaseType) throws SQLException {
        try (MetaDataConnection connection = new MetaDataConnection(dataSource.getConnection())) {
            if (!isTableExist(connection, table)) {
                return Optional.empty();
            }
            return Optional.of(new TableMetaData(ColumnMetaDataLoader.load(connection, table, databaseType), IndexMetaDataLoader.load(connection, table)));
        }
    }
    
    private static boolean isTableExist(final Connection connection, final String table) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), table, null)) {
            return resultSet.next();
        }
    }
}
