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

package org.apache.shardingsphere.test.e2e.sql.framework.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.sql.framework.metadata.dialect.PostgreSQLDatabaseAssertionMetaDataSQLProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Dialect database assertion meta data factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DialectDatabaseAssertionMetaDataFactory {
    
    /**
     * Get primary key column name.
     *
     * @param databaseType database type
     * @param dataSource dataSource data source
     * @param tableName table name
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static Optional<String> getPrimaryKeyColumnName(final DatabaseType databaseType, final DataSource dataSource, final String tableName) throws SQLException {
        Optional<DialectDatabaseAssertionMetaDataSQLProvider> sqlProvider = findDialectDatabaseAssertionMetaDataSQLProvider(databaseType);
        return sqlProvider.isPresent() ? Optional.of(getPrimaryKeyColumnName(dataSource, tableName, sqlProvider.get().getFetchPrimaryKeyColumnNameSQL(tableName))) : Optional.empty();
    }
    
    private static String getPrimaryKeyColumnName(final DataSource dataSource, final String tableName, final String sql) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            throw new SQLException(String.format("Can not get primary key of `%s`", tableName));
        }
    }
    
    private static Optional<DialectDatabaseAssertionMetaDataSQLProvider> findDialectDatabaseAssertionMetaDataSQLProvider(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "PostgreSQL":
            case "openGauss":
                return Optional.of(new PostgreSQLDatabaseAssertionMetaDataSQLProvider());
            default:
                return Optional.empty();
        }
    }
}
