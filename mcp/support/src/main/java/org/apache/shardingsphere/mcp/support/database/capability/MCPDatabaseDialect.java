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

package org.apache.shardingsphere.mcp.support.database.capability;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Optional;

/**
 * MCP database dialect capabilities.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPDatabaseDialect {
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData;
    
    private final SystemDatabase systemDatabase;
    
    private final Optional<MCPDatabaseCapabilityOption> option;
    
    /**
     * Create MCP database dialect capabilities.
     *
     * @param databaseType database type
     * @return MCP database dialect capabilities
     */
    public static MCPDatabaseDialect of(final String databaseType) {
        String actualDatabaseType = trimToEmpty(databaseType);
        DatabaseType databaseTypeFromSPI = TypedSPILoader.findService(DatabaseType.class, actualDatabaseType)
                .orElseThrow(() -> new ServiceProviderNotFoundException(DatabaseType.class, actualDatabaseType));
        DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)
                .orElseThrow(() -> new ServiceProviderNotFoundException(DialectDatabaseMetaData.class, actualDatabaseType));
        return new MCPDatabaseDialect(dialectDatabaseMetaData, new SystemDatabase(databaseTypeFromSPI),
                TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, actualDatabaseType));
    }
    
    /**
     * Get identifier quote character.
     *
     * @return identifier quote character
     */
    public QuoteCharacter getIdentifierQuoteCharacter() {
        return dialectDatabaseMetaData.getQuoteCharacter();
    }
    
    /**
     * Get identifier case policy.
     *
     * @param identifierScope identifier scope
     * @return identifier case policy
     */
    public IdentifierCasePolicy getIdentifierCasePolicy(final IdentifierScope identifierScope) {
        return getIdentifierCasePolicySet().getPolicy(identifierScope);
    }
    
    private IdentifierCasePolicySet getIdentifierCasePolicySet() {
        return createDialectIdentifierCasePolicySet(dialectDatabaseMetaData)
                .orElseGet(() -> option.map(MCPDatabaseCapabilityOption::getIdentifierCasePolicySet).orElseGet(IdentifierCasePolicyFactory::newSensitivePolicySet));
    }
    
    private static Optional<IdentifierCasePolicySet> createDialectIdentifierCasePolicySet(final DialectDatabaseMetaData dialectDatabaseMetaData) {
        IdentifierPatternType identifierPatternType = dialectDatabaseMetaData.getIdentifierPatternType();
        return IdentifierPatternType.KEEP_ORIGIN != identifierPatternType
                ? Optional.of(IdentifierCasePolicyFactory.newDialectDefaultPolicySet(identifierPatternType, dialectDatabaseMetaData.isCaseSensitive()))
                : Optional.empty();
    }
    
    /**
     * Get default schema semantics.
     *
     * @return default schema semantics
     */
    public DialectSchemaSemantics getDefaultSchemaSemantics() {
        return dialectDatabaseMetaData.getSchemaOption().getSchemaSemantics();
    }
    
    /**
     * Get transaction capability.
     *
     * @param supportsTransaction whether transaction is supported
     * @param supportsSavepoint whether savepoint is supported
     * @return transaction capability
     */
    public TransactionCapability getTransactionCapability(final boolean supportsTransaction, final boolean supportsSavepoint) {
        if (!supportsTransaction) {
            return TransactionCapability.NONE;
        }
        return supportsSavepoint ? TransactionCapability.LOCAL_WITH_SAVEPOINT : TransactionCapability.LOCAL;
    }
    
    /**
     * Judge whether unquoted identifiers are folded by database metadata lookup.
     *
     * @return whether unquoted identifiers are folded
     */
    public boolean isUnquotedIdentifierCaseFolded() {
        return IdentifierPatternType.KEEP_ORIGIN != dialectDatabaseMetaData.getIdentifierPatternType();
    }
    
    /**
     * Get sequence metadata query.
     *
     * @return sequence metadata query
     */
    public boolean isSequenceSupported() {
        return dialectDatabaseMetaData.getSequenceOption().isPresent();
    }
    
    /**
     * Judge whether schema is a system schema.
     *
     * @param schemaName schema name
     * @return whether schema is a system schema
     */
    public boolean isSystemSchema(final String schemaName) {
        String actualSchemaName = trimToEmpty(schemaName);
        if (actualSchemaName.isEmpty()) {
            return false;
        }
        return containsSystemSchema(systemDatabase.getSystemSchemas(), actualSchemaName);
    }
    
    /**
     * Judge whether schema or catalog is a system schema.
     *
     * @param schemaName schema name
     * @param catalogName catalog name
     * @param defaultSchemaSemantics default schema semantics
     * @return whether schema or catalog is a system schema
     */
    public boolean isSystemSchema(final String schemaName, final String catalogName, final DialectSchemaSemantics defaultSchemaSemantics) {
        return isSystemSchema(schemaName) || DialectSchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics && isSystemSchema(catalogName);
    }
    
    private static boolean containsSystemSchema(final Collection<String> systemSchemas, final String schemaName) {
        for (String each : systemSchemas) {
            if (schemaName.equalsIgnoreCase(each)) {
                return true;
            }
        }
        return false;
    }
    
    private static String trimToEmpty(final String value) {
        return null == value ? "" : value.trim();
    }
}
