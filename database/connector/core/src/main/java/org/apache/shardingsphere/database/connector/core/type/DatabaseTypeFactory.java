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

package org.apache.shardingsphere.database.connector.core.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.exception.AmbiguousStorageTypeException;
import org.apache.shardingsphere.database.connector.core.exception.UnsupportedStorageTypeException;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.branch.DialectBranchOption;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeFactory {
    
    /**
     * Get database type.
     *
     * @param url database URL
     * @return database type
     */
    public static DatabaseType get(final String url) {
        Collection<DatabaseType> databaseTypes = ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class).stream().filter(each -> matchURLs(url, each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkNotEmpty(databaseTypes, () -> new UnsupportedStorageTypeException(url));
        for (DatabaseType each : databaseTypes) {
            if (!each.getTrunkDatabaseType().isPresent()) {
                return each;
            }
        }
        return databaseTypes.iterator().next();
    }
    
    /**
     * Get database type.
     *
     * @param metaData database meta data
     * @return database type
     * @throws SQLException SQL exception
     */
    public static DatabaseType get(final DatabaseMetaData metaData) throws SQLException {
        return Objects.toString(metaData.getDatabaseProductName(), "").contains("Hive") ? TypedSPILoader.getService(DatabaseType.class, "Hive") : get(metaData.getURL());
    }
    
    /**
     * Get database type.
     *
     * @param connection database connection
     * @return database type
     * @throws SQLException SQL exception
     */
    public static DatabaseType get(final Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        DatabaseType result = get(metaData);
        Collection<DatabaseType> actualBranchDatabaseTypes = findActualBranchDatabaseTypes(connection, result);
        checkUniqueActualBranchDatabaseType(metaData, actualBranchDatabaseTypes);
        return actualBranchDatabaseTypes.isEmpty() ? result : actualBranchDatabaseTypes.iterator().next();
    }
    
    private static void checkUniqueActualBranchDatabaseType(final DatabaseMetaData metaData, final Collection<DatabaseType> actualBranchDatabaseTypes) throws SQLException {
        if (actualBranchDatabaseTypes.size() <= 1) {
            return;
        }
        String url = metaData.getURL();
        ShardingSpherePreconditions.checkState(false,
                () -> new AmbiguousStorageTypeException(url, actualBranchDatabaseTypes.stream().map(DatabaseType::getType).collect(Collectors.toList())));
    }
    
    private static Collection<DatabaseType> findActualBranchDatabaseTypes(final Connection connection, final DatabaseType trunkDatabaseType) throws SQLException {
        Collection<DatabaseType> result = new LinkedList<>();
        for (DatabaseType each : ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)) {
            if (!isBranchDatabaseType(each, trunkDatabaseType)) {
                continue;
            }
            if (isActualBranchDatabaseType(each, connection)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static boolean isBranchDatabaseType(final DatabaseType databaseType, final DatabaseType trunkDatabaseType) {
        return databaseType.getTrunkDatabaseType().map(optional -> Objects.equals(optional.getType(), trunkDatabaseType.getType())).orElse(false);
    }
    
    private static boolean isActualBranchDatabaseType(final DatabaseType databaseType, final Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return containsBranch(databaseType, metaData.getDatabaseProductName())
                || containsBranch(databaseType, metaData.getDatabaseProductVersion()) || containsBranch(databaseType, queryBranchTypeDetectionValue(databaseType, connection));
    }
    
    private static boolean containsBranch(final DatabaseType databaseType, final String value) {
        return Objects.toString(value, "").toUpperCase(Locale.ENGLISH).contains(databaseType.getType().toUpperCase(Locale.ENGLISH));
    }
    
    private static String queryBranchTypeDetectionValue(final DatabaseType databaseType, final Connection connection) {
        Optional<DialectBranchOption> branchOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getBranchOption();
        if (!branchOption.isPresent()) {
            return "";
        }
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(branchOption.get().getBranchTypeDetectionSQL())) {
            return resultSet.next() ? resultSet.getString(1) : "";
        } catch (final SQLException ignored) {
            return "";
        }
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
}
