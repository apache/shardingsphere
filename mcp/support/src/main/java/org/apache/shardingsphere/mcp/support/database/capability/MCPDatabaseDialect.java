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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MCP database dialect capabilities.
 */
public final class MCPDatabaseDialect {
    
    private static final Collection<String> LEGACY_SYSTEM_SCHEMAS = List.of(
            "information_schema", "mysql", "performance_schema", "pg_catalog", "shardingsphere", "sys", "system_lobs");
    
    private final String databaseType;
    
    private final Optional<MCPDatabaseCapabilityOption> option;
    
    private MCPDatabaseDialect(final String databaseType, final Optional<MCPDatabaseCapabilityOption> option) {
        this.databaseType = databaseType;
        this.option = option;
    }
    
    /**
     * Create MCP database dialect capabilities.
     *
     * @param databaseType database type
     * @return MCP database dialect capabilities
     */
    public static MCPDatabaseDialect of(final String databaseType) {
        String actualDatabaseType = trimToEmpty(databaseType);
        Optional<MCPDatabaseCapabilityOption> option = actualDatabaseType.isEmpty()
                ? Optional.empty()
                : TypedSPILoader.findService(MCPDatabaseCapabilityOption.class, actualDatabaseType);
        return new MCPDatabaseDialect(actualDatabaseType, option);
    }
    
    /**
     * Get identifier quote character.
     *
     * @return identifier quote character
     */
    public QuoteCharacter getIdentifierQuoteCharacter() {
        return option.map(MCPDatabaseCapabilityOption::getIdentifierQuoteCharacter).orElseGet(this::getFallbackIdentifierQuoteCharacter);
    }
    
    /**
     * Get identifier case policy.
     *
     * @param identifierScope identifier scope
     * @return identifier case policy
     */
    public IdentifierCasePolicy getIdentifierCasePolicy(final IdentifierScope identifierScope) {
        return option.map(MCPDatabaseCapabilityOption::getIdentifierCasePolicySet)
                .orElseGet(IdentifierCasePolicyFactory::newSensitivePolicySet).getPolicy(identifierScope);
    }
    
    /**
     * Get default schema semantics.
     *
     * @return default schema semantics
     */
    public SchemaSemantics getDefaultSchemaSemantics() {
        return option.map(MCPDatabaseCapabilityOption::getDefaultSchemaSemantics).orElse(SchemaSemantics.NATIVE_SCHEMA);
    }
    
    /**
     * Judge whether unquoted identifiers are folded by database metadata lookup.
     *
     * @return whether unquoted identifiers are folded
     */
    public boolean isUnquotedIdentifierCaseFolded() {
        return option.map(MCPDatabaseCapabilityOption::isUnquotedIdentifierCaseFolded).orElse(false);
    }
    
    /**
     * Get sequence metadata query.
     *
     * @return sequence metadata query
     */
    public Optional<String> getSequenceQuery() {
        return option.flatMap(MCPDatabaseCapabilityOption::getSequenceQuery);
    }
    
    /**
     * Judge whether information_schema column lookup should filter table_schema.
     *
     * @return whether information_schema column lookup should filter table_schema
     */
    public boolean isInformationSchemaColumnSchemaFilterRequired() {
        return option.map(MCPDatabaseCapabilityOption::isInformationSchemaColumnSchemaFilterRequired).orElse(false);
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
        Collection<String> systemSchemas = option.map(MCPDatabaseCapabilityOption::getSystemSchemas).orElseGet(List::of);
        return containsSystemSchema(systemSchemas, actualSchemaName) || containsSystemSchema(LEGACY_SYSTEM_SCHEMAS, actualSchemaName);
    }
    
    /**
     * Judge whether schema or catalog is a system schema.
     *
     * @param schemaName schema name
     * @param catalogName catalog name
     * @param defaultSchemaSemantics default schema semantics
     * @return whether schema or catalog is a system schema
     */
    public boolean isSystemSchema(final String schemaName, final String catalogName, final SchemaSemantics defaultSchemaSemantics) {
        return isSystemSchema(schemaName) || SchemaSemantics.DATABASE_AS_SCHEMA == defaultSchemaSemantics && isSystemSchema(catalogName);
    }
    
    private QuoteCharacter getFallbackIdentifierQuoteCharacter() {
        return databaseType.isEmpty() ? QuoteCharacter.BACK_QUOTE : QuoteCharacter.QUOTE;
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
