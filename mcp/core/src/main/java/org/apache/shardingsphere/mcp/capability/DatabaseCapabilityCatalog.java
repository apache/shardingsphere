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

package org.apache.shardingsphere.mcp.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * MCP database capability catalog.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCapabilityCatalog {
    
    /**
     * Find one capability definition by database type and version.
     *
     * @param databaseName logical database name
     * @param databaseType database type
     * @param databaseVersion database version
     * @return capability definition when present
     */
    public static Optional<DatabaseCapability> find(final String databaseName, final String databaseType, final String databaseVersion) {
        return TypedSPILoader.findService(DatabaseCapabilityBuilder.class, normalizeDatabaseType(databaseType)).map(each -> each.build(databaseName, databaseVersion));
    }
    
    static String normalizeDatabaseType(final String databaseType) {
        return databaseType.trim().toUpperCase(Locale.ENGLISH);
    }
    
    static Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
        return DatabaseCapabilityBuilderSupport.createSupportedTransactionStatements(transactionCapability);
    }
}
