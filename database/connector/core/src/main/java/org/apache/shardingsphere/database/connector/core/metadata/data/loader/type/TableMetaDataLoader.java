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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

/**
 * Table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> load(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        return load(dataSource, tableNamePattern, databaseType, IdentifierNormalizeEngine.resolvePolicy(databaseType, dataSource, IdentifierScope.TABLE));
    }
    
    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @param tableIdentifierPolicy table identifier case policy
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> load(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType,
                                               final IdentifierCasePolicy tableIdentifierPolicy) throws SQLException {
        return loadTableMetaData(dataSource, tableNamePattern, IdentifierNormalizeEngine.normalize(tableIdentifierPolicy, tableNamePattern), databaseType);
    }
    
    /**
     * Load table meta data with normalized table name pattern.
     *
     * @param dataSource data source
     * @param tableNamePattern normalized table name pattern
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> loadNormalized(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        return loadTableMetaData(dataSource, tableNamePattern, tableNamePattern, databaseType);
    }
    
    private static Optional<TableMetaData> loadTableMetaData(final DataSource dataSource, final String tableName,
                                                             final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        try (MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, dataSource.getConnection())) {
            return isTableExist(connection, tableNamePattern)
                    ? Optional.of(new TableMetaData(tableName, ColumnMetaDataLoader.load(
                            connection, tableNamePattern, databaseType), IndexMetaDataLoader.load(connection, tableNamePattern), Collections.emptyList()))
                    : Optional.empty();
        }
    }
    
    private static boolean isTableExist(final Connection connection, final String tableNamePattern) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, null)) {
            return resultSet.next();
        }
    }
}
