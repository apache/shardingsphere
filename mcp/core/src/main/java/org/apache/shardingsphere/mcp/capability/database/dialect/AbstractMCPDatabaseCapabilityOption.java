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

import org.apache.shardingsphere.mcp.capability.database.MCPDatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.capability.database.SchemaExecutionSemantics;
import org.apache.shardingsphere.mcp.capability.database.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.database.TransactionCapability;

import java.util.Collection;
import java.util.Collections;

/**
 * Common base implementation for MCP database capability options.
 */
abstract class AbstractMCPDatabaseCapabilityOption implements MCPDatabaseCapabilityOption {
    
    private final String type;
    
    private final Collection<Object> typeAliases;
    
    private final TransactionCapability transactionCapability;
    
    private final boolean indexSupported;
    
    private final SchemaSemantics defaultSchemaSemantics;
    
    private final SchemaExecutionSemantics schemaExecutionSemantics;
    
    private final boolean crossSchemaQuerySupported;
    
    private final boolean sequenceSupported;
    
    protected AbstractMCPDatabaseCapabilityOption(final String type, final TransactionCapability transactionCapability, final boolean indexSupported,
                                                  final SchemaSemantics defaultSchemaSemantics, final SchemaExecutionSemantics schemaExecutionSemantics,
                                                  final boolean crossSchemaQuerySupported, final boolean sequenceSupported) {
        this(type, Collections.emptyList(), transactionCapability, indexSupported, defaultSchemaSemantics, schemaExecutionSemantics,
                crossSchemaQuerySupported, sequenceSupported);
    }
    
    protected AbstractMCPDatabaseCapabilityOption(final String type, final Collection<String> typeAliases, final TransactionCapability transactionCapability,
                                                  final boolean indexSupported, final SchemaSemantics defaultSchemaSemantics,
                                                  final SchemaExecutionSemantics schemaExecutionSemantics, final boolean crossSchemaQuerySupported,
                                                  final boolean sequenceSupported) {
        this.type = type;
        this.typeAliases = typeAliases.stream().map(each -> (Object) each).toList();
        this.transactionCapability = transactionCapability;
        this.indexSupported = indexSupported;
        this.defaultSchemaSemantics = defaultSchemaSemantics;
        this.schemaExecutionSemantics = schemaExecutionSemantics;
        this.crossSchemaQuerySupported = crossSchemaQuerySupported;
        this.sequenceSupported = sequenceSupported;
    }
    
    @Override
    public final TransactionCapability getTransactionCapability() {
        return transactionCapability;
    }
    
    @Override
    public final boolean isIndexSupported() {
        return indexSupported;
    }
    
    @Override
    public final SchemaSemantics getDefaultSchemaSemantics() {
        return defaultSchemaSemantics;
    }
    
    @Override
    public final SchemaExecutionSemantics getSchemaExecutionSemantics() {
        return schemaExecutionSemantics;
    }
    
    @Override
    public final boolean isCrossSchemaQuerySupported() {
        return crossSchemaQuerySupported;
    }
    
    @Override
    public final boolean isSequenceSupported() {
        return sequenceSupported;
    }
    
    @Override
    public boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return false;
    }
    
    @Override
    public final String getType() {
        return type;
    }
    
    @Override
    public final Collection<Object> getTypeAliases() {
        return typeAliases;
    }
}
