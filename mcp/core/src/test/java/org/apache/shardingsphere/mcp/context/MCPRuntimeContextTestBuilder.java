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

package org.apache.shardingsphere.mcp.context;

import org.apache.shardingsphere.mcp.audit.AuditRecorder;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityAssembler;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade;
import org.apache.shardingsphere.mcp.execute.StatementClassifier;
import org.apache.shardingsphere.mcp.protocol.MCPPayloadBuilder;
import org.apache.shardingsphere.mcp.resource.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader;
import org.apache.shardingsphere.mcp.resource.ResourceUriResolver;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.session.TransactionCommandExecutor;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;
import org.apache.shardingsphere.mcp.tool.MetadataToolDispatcher;

/**
 * MCP runtime context builder for tests.
 */
public final class MCPRuntimeContextTestBuilder {
    
    /**
     * Build MCP runtime context for tests.
     *
     * @param databaseMetadataSnapshots database metadata snapshots
     * @param databaseRuntime database runtime
     * @return runtime context
     */
    public MCPRuntimeContext build(final DatabaseMetadataSnapshots databaseMetadataSnapshots, final DatabaseRuntime databaseRuntime) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        DatabaseCapabilityAssembler capabilityAssembler = new DatabaseCapabilityAssembler(databaseMetadataSnapshots);
        MetadataResourceLoader metadataResourceLoader = new MetadataResourceLoader();
        ResourceUriResolver resourceUriResolver = new ResourceUriResolver();
        MetadataToolDispatcher metadataToolDispatcher = new MetadataToolDispatcher(metadataResourceLoader);
        MCPToolCatalog toolCatalog = new MCPToolCatalog();
        TransactionCommandExecutor transactionCommandExecutor = new TransactionCommandExecutor(capabilityAssembler, sessionManager, databaseRuntime);
        AuditRecorder auditRecorder = new AuditRecorder();
        ExecuteQueryFacade executeQueryFacade = new ExecuteQueryFacade(new StatementClassifier(), capabilityAssembler, transactionCommandExecutor, auditRecorder);
        MCPPayloadBuilder payloadBuilder = new MCPPayloadBuilder();
        return new MCPRuntimeContext(sessionManager, databaseMetadataSnapshots, databaseRuntime, capabilityAssembler,
                metadataResourceLoader, resourceUriResolver, metadataToolDispatcher, toolCatalog, transactionCommandExecutor, auditRecorder, executeQueryFacade, payloadBuilder);
    }
}
