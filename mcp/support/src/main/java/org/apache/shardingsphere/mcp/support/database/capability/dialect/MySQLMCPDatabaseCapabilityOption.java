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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.mcp.support.database.capability.DatabaseVersionUtil;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.support.database.capability.TransactionCapability;

import java.util.Collection;
import java.util.List;

/**
 * MCP database capability option for MySQL.
 */
public final class MySQLMCPDatabaseCapabilityOption extends AbstractMCPDatabaseCapabilityOption {
    
    public MySQLMCPDatabaseCapabilityOption() {
        super("MySQL", TransactionCapability.LOCAL_WITH_SAVEPOINT, true,
                SchemaSemantics.DATABASE_AS_SCHEMA, SchemaExecutionSemantics.FIXED_TO_DATABASE, false, false);
    }
    
    @Override
    public QuoteCharacter getIdentifierQuoteCharacter() {
        return QuoteCharacter.BACK_QUOTE;
    }
    
    @Override
    public IdentifierCasePolicySet getIdentifierCasePolicySet() {
        return IdentifierCasePolicyFactory.newMySQLInsensitivePolicySet();
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return List.of("information_schema", "mysql", "performance_schema", "shardingsphere", "sys");
    }
    
    @Override
    public boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return DatabaseVersionUtil.isVersionAtLeast(databaseVersion, 8, 0, 18);
    }
}
