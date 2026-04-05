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

package org.apache.shardingsphere.mcp.capability.dialect;

import org.apache.shardingsphere.mcp.capability.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityBuilder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityBuilderSupport;
import org.apache.shardingsphere.mcp.capability.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.TransactionCapability;

/**
 * Abstract database capability builder.
 */
public abstract class AbstractDatabaseCapabilityBuilder implements DatabaseCapabilityBuilder {
    
    private final String databaseType;
    
    private final TransactionCapability transactionCapability;
    
    private final boolean indexSupported;
    
    private final SchemaSemantics defaultSchemaSemantics;
    
    private final boolean crossSchemaQuerySupported;
    
    private final boolean explainAnalyzeSupported;
    
    protected AbstractDatabaseCapabilityBuilder(final String databaseType, final TransactionCapability transactionCapability, final boolean indexSupported,
                                                final SchemaSemantics defaultSchemaSemantics, final boolean crossSchemaQuerySupported,
                                                final boolean explainAnalyzeSupported) {
        this.databaseType = databaseType;
        this.transactionCapability = transactionCapability;
        this.indexSupported = indexSupported;
        this.defaultSchemaSemantics = defaultSchemaSemantics;
        this.crossSchemaQuerySupported = crossSchemaQuerySupported;
        this.explainAnalyzeSupported = explainAnalyzeSupported;
    }
    
    @Override
    public final DatabaseCapability build(final String databaseName, final String databaseVersion) {
        return DatabaseCapabilityBuilderSupport.createDefaultCapability(databaseName, databaseType, transactionCapability,
                indexSupported, defaultSchemaSemantics, crossSchemaQuerySupported, isExplainAnalyzeSupported(databaseVersion));
    }
    
    protected boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return explainAnalyzeSupported;
    }
    
    @Override
    public final String getType() {
        return databaseType;
    }
}
