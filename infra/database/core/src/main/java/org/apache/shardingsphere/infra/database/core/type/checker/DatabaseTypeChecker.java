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

package org.apache.shardingsphere.infra.database.core.type.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Database type checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeChecker {
    
    private static final Collection<String> MOCKED_URL_PREFIXES = new HashSet<>(Arrays.asList("jdbc:fixture", "jdbc:mock", "mock:jdbc"));
    
    private static final Collection<String> UNSUPPORTED_URL_PREFIXES = Collections.singletonList("jdbc:mysql:aws");
    
    private static final Collection<DatabaseType> SUPPORTED_STORAGE_TYPES = new HashSet<>(8, 1F);
    
    private static volatile boolean isChecked;
    
    static {
        Arrays.asList("MySQL", "PostgreSQL", "openGauss", "Oracle", "SQLServer", "H2", "MariaDB")
                .forEach(each -> TypedSPILoader.findService(DatabaseType.class, each).ifPresent(SUPPORTED_STORAGE_TYPES::add));
    }
    
    /**
     * Check supported storage types.
     *
     * @param dataSources  data sources
     * @param databaseName database name
     * @param storageTypes storage types
     * @throws SQLException SQL exception
     */
    public static void checkSupportedStorageTypes(final Map<String, DataSource> dataSources, final String databaseName, final Map<String, DatabaseType> storageTypes) throws SQLException {
        if (isChecked || dataSources.isEmpty()) {
            return;
        }
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            try (Connection connection = entry.getValue().getConnection()) {
                String url = connection.getMetaData().getURL();
                if (MOCKED_URL_PREFIXES.stream().anyMatch(url::startsWith)) {
                    return;
                }
                ShardingSpherePreconditions.checkState(UNSUPPORTED_URL_PREFIXES.stream()
                        .noneMatch(url::startsWith), () -> new UnsupportedStorageTypeException(databaseName, entry.getKey()));
            }
        }
        storageTypes.forEach((key, value) -> ShardingSpherePreconditions.checkState(SUPPORTED_STORAGE_TYPES.stream()
                .anyMatch(each -> each.getClass().equals(value.getClass())), () -> new UnsupportedStorageTypeException(databaseName, key)));
        isChecked = true;
    }
    
    /**
     * Check supported storage type.
     *
     * @param url URL
     * @param dataSourceName data source name
     */
    public static void checkSupportedStorageType(final String url, final String dataSourceName) {
        if (MOCKED_URL_PREFIXES.stream().anyMatch(url::startsWith)) {
            return;
        }
        ShardingSpherePreconditions.checkState(UNSUPPORTED_URL_PREFIXES.stream()
                .noneMatch(url::startsWith), () -> new UnsupportedStorageTypeException(dataSourceName));
        ShardingSpherePreconditions.checkState(SUPPORTED_STORAGE_TYPES.stream()
                .flatMap(storageType -> storageType.getJdbcUrlPrefixes().stream())
                .anyMatch(url::startsWith), () -> new UnsupportedStorageTypeException(dataSourceName));
    }
}
