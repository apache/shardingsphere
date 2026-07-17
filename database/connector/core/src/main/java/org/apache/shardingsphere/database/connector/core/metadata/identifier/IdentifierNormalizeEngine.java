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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Optional;

/**
 * Identifier normalize engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierNormalizeEngine {
    
    /**
     * Resolve identifier case policy.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param identifierScope identifier scope
     * @return identifier case policy
     */
    public static IdentifierCasePolicy resolvePolicy(final DatabaseType databaseType, final DataSource dataSource, final IdentifierScope identifierScope) {
        return DatabaseTypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType)
                .map(each -> each.provide(new IdentifierCasePolicyProviderContext(databaseType, dataSource)))
                .orElseGet(IdentifierCasePolicyFactory::newInsensitivePolicySet)
                .getPolicy(identifierScope);
    }
    
    /**
     * Normalize identifier.
     *
     * @param policy identifier case policy
     * @param identifier identifier
     * @return normalized identifier
     */
    public static String normalize(final IdentifierCasePolicy policy, final String identifier) {
        QuoteCharacter quoteCharacter = QuoteCharacter.getQuoteCharacter(identifier);
        String unwrappedIdentifier = quoteCharacter.unwrap(identifier);
        if (QuoteCharacter.NONE != quoteCharacter) {
            return unwrappedIdentifier;
        }
        return LookupMode.NORMALIZED == policy.getLookupMode(QuoteCharacter.NONE) ? policy.normalize(unwrappedIdentifier) : unwrappedIdentifier;
    }
    
    /**
     * Find matched stored identifier.
     *
     * @param storedNames stored identifier names
     * @param policy identifier case policy
     * @param identifier identifier
     * @return matched stored identifier
     */
    public static Optional<String> findMatchedIdentifier(final Collection<String> storedNames, final IdentifierCasePolicy policy, final String identifier) {
        QuoteCharacter quoteCharacter = QuoteCharacter.getQuoteCharacter(identifier);
        String unwrappedIdentifier = quoteCharacter.unwrap(identifier);
        return storedNames.stream().filter(each -> policy.matches(each, unwrappedIdentifier, quoteCharacter)).findFirst();
    }
}
