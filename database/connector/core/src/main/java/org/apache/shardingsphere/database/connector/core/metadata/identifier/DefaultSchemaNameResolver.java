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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * Default schema name resolver.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultSchemaNameResolver {
    
    /**
     * Resolve protocol default schema name.
     *
     * @param protocolType protocol type
     * @param databaseName database name, may be null
     * @return default schema name, may be null
     */
    public static String resolveProtocol(final DatabaseType protocolType, final String databaseName) {
        return resolve(protocolType, databaseName, () -> IdentifierNormalizeEngine.resolvePolicy(protocolType, null, IdentifierScope.SCHEMA));
    }
    
    /**
     * Resolve storage default schema name.
     *
     * @param storageType storage type
     * @param dataSource data source
     * @param databaseName database name, may be null
     * @return default schema name, may be null
     */
    public static String resolveStorage(final DatabaseType storageType, final DataSource dataSource, final String databaseName) {
        return resolve(storageType, databaseName, () -> IdentifierNormalizeEngine.resolvePolicy(storageType, dataSource, IdentifierScope.SCHEMA));
    }
    
    private static String resolve(final DatabaseType databaseType, final String databaseName, final Supplier<IdentifierCasePolicy> identifierCasePolicySupplier) {
        return new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema()
                .orElseGet(() -> null == databaseName ? null : IdentifierNormalizeEngine.normalize(identifierCasePolicySupplier.get(), databaseName));
    }
}
