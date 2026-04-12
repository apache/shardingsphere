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

package org.apache.shardingsphere.mcp.capability.database.dialect;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.capability.database.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.capability.database.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.database.TransactionCapability;

/**
 * MCP database capability option for PostgreSQL.
 */
@Getter
public final class PostgreSQLMCPDatabaseCapabilityOption implements MCPDatabaseCapabilityOption {
    
    private final TransactionCapability transactionCapability = TransactionCapability.LOCAL_WITH_SAVEPOINT;
    
    private final boolean indexSupported = true;
    
    private final SchemaSemantics defaultSchemaSemantics = SchemaSemantics.NATIVE_SCHEMA;
    
    private final SchemaExecutionSemantics schemaExecutionSemantics = SchemaExecutionSemantics.BEST_EFFORT;
    
    private final boolean crossSchemaQuerySupported = true;
    
    private final boolean isSequenceSupported = true;
    
    @Override
    public boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return true;
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
