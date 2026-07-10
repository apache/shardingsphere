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

package org.apache.shardingsphere.mcp.support.database.capability.dialect;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.TransactionCapability;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MCP database capability option for PostgreSQL.
 */
public final class PostgreSQLMCPDatabaseCapabilityOption extends AbstractMCPDatabaseCapabilityOption {
    
    private static final String SEQUENCE_QUERY =
            "SELECT sequence_schema AS SEQUENCE_SCHEMA, sequence_name AS SEQUENCE_NAME FROM information_schema.sequences";
    
    public PostgreSQLMCPDatabaseCapabilityOption() {
        super("PostgreSQL", TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                SchemaSemantics.NATIVE_SCHEMA, SchemaExecutionSemantics.BEST_EFFORT, true, true);
    }
    
    @Override
    public IdentifierCasePolicySet getIdentifierCasePolicySet() {
        return IdentifierCasePolicyFactory.newLowerCasePolicySet();
    }
    
    @Override
    public boolean isUnquotedIdentifierCaseFolded() {
        return true;
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return List.of("information_schema", "pg_catalog", "shardingsphere");
    }
    
    @Override
    public Optional<String> getSequenceQuery() {
        return Optional.of(SEQUENCE_QUERY);
    }
    
    @Override
    public boolean isInformationSchemaColumnSchemaFilterRequired() {
        return true;
    }
    
    @Override
    public boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return true;
    }
}
