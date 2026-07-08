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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MCP database capability option.
 */
@SingletonSPI
public interface MCPDatabaseCapabilityOption extends TypedSPI {
    
    /**
     * Get transaction capability.
     *
     * @return transaction capability
     */
    TransactionCapability getTransactionCapability();
    
    /**
     * Judge whether index metadata is supported.
     *
     * @return whether index metadata is supported
     */
    boolean isIndexSupported();
    
    /**
     * Get default schema semantics.
     *
     * @return default schema semantics
     */
    SchemaSemantics getDefaultSchemaSemantics();
    
    /**
     * Get execution-time schema semantics.
     *
     * @return execution-time schema semantics
     */
    SchemaExecutionSemantics getSchemaExecutionSemantics();
    
    /**
     * Judge whether cross-schema query is supported.
     *
     * @return whether cross-schema query is supported
     */
    boolean isCrossSchemaQuerySupported();
    
    /**
     * Judge whether explain analyze is supported for database version.
     *
     * @param databaseVersion database version
     * @return whether explain analyze is supported
     */
    boolean isExplainAnalyzeSupported(String databaseVersion);
    
    /**
     * Judge whether sequence metadata is supported.
     *
     * @return whether sequence metadata is supported
     */
    boolean isSequenceSupported();
    
    /**
     * Get identifier quote character.
     *
     * @return identifier quote character
     */
    default QuoteCharacter getIdentifierQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    /**
     * Get identifier case policy set.
     *
     * @return identifier case policy set
     */
    default IdentifierCasePolicySet getIdentifierCasePolicySet() {
        return IdentifierCasePolicyFactory.newSensitivePolicySet();
    }
    
    /**
     * Judge whether unquoted identifiers are folded by database metadata lookup.
     *
     * @return whether unquoted identifiers are folded
     */
    default boolean isUnquotedIdentifierCaseFolded() {
        return false;
    }
    
    /**
     * Get system schemas.
     *
     * @return system schemas
     */
    default Collection<String> getSystemSchemas() {
        return List.of();
    }
    
    /**
     * Get sequence metadata query.
     *
     * @return sequence metadata query
     */
    default Optional<String> getSequenceQuery() {
        return Optional.empty();
    }
    
    @Override
    String getType();
}
