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

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Resolver of identifier case policies.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierCasePolicyResolver {
    
    /**
     * Resolve identifier case policies from database dialect metadata.
     *
     * @param databaseType database type
     * @return identifier case policies
     */
    public static IdentifierCasePolicySet resolveProtocol(final DatabaseType databaseType) {
        return TypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType).map(IdentifierCasePolicyProvider::provide).orElseGet(() -> {
            DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType);
            return IdentifierCasePolicyFactory.newDialectDefaultPolicySet(dialectDatabaseMetaData.getIdentifierPatternType(), dialectDatabaseMetaData.isCaseSensitive());
        });
    }
    
    /**
     * Resolve identifier case policies from database runtime state.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @return identifier case policies
     * @throws SQLException SQL exception
     */
    public static IdentifierCasePolicySet resolveStorage(final DatabaseType databaseType, final DataSource dataSource) throws SQLException {
        Optional<DialectIdentifierCasePolicyLoader> loader = findLoader(databaseType);
        if (!loader.isPresent()) {
            return resolveProtocol(databaseType);
        }
        try (Connection connection = dataSource.getConnection()) {
            return loader.get().load(connection);
        }
    }
    
    /**
     * Resolve identifier case policies from database runtime state without closing the provided connection.
     *
     * @param databaseType database type
     * @param connection database connection
     * @return identifier case policies
     * @throws SQLException SQL exception
     */
    public static IdentifierCasePolicySet resolveStorage(final DatabaseType databaseType, final Connection connection) throws SQLException {
        Optional<DialectIdentifierCasePolicyLoader> loader = findLoader(databaseType);
        return loader.isPresent() ? loader.get().load(connection) : resolveProtocol(databaseType);
    }
    
    private static Optional<DialectIdentifierCasePolicyLoader> findLoader(final DatabaseType databaseType) {
        return TypedSPILoader.findService(DialectIdentifierCasePolicyLoader.class, databaseType);
    }
}
