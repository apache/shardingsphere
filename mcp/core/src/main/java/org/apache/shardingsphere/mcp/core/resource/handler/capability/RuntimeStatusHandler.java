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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.MCPUriTemplateUtils;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for runtime status resource URI.
 */
public final class RuntimeStatusHandler implements MCPResourceHandler<MCPDatabaseHandlerContext> {

    private static final String URI_PATTERN = "shardingsphere://runtime";

    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }

    @Override
    public MCPResourceDescriptor getResourceDescriptor() {
        return MCPDescriptorRegistry.getRequiredResourceDescriptor(URI_PATTERN);
    }

    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext handlerContext, final MCPUriVariables uriVariables) {
        List<MCPDatabaseMetadata> databases = handlerContext.getMetadataQueryFacade().queryDatabases();
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", "available");
        result.put("active_transport", handlerContext.getActiveTransport());
        result.put("configured_database_count", databases.size());
        result.put("databases", databases.stream().map(each -> createDatabaseStatus(handlerContext, each)).toList());
        result.put("capability_fingerprint", MCPDescriptorRegistry.getDescriptorCatalogFingerprint());
        result.put("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read full MCP capabilities before choosing tools.", "resources_to_read"),
                MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "read_first", "Read logical databases before choosing a database scope.", "resources_to_read")));
        result.put("next_actions", List.of(MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read the full capability catalog before choosing tools.")));
        return new MCPMapResponse(result);
    }

    private Map<String, Object> createDatabaseStatus(final MCPDatabaseHandlerContext handlerContext, final MCPDatabaseMetadata database) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("database", database.getDatabase());
        result.put("database_type", database.getDatabaseType());
        result.put("schema_count", database.getSchemas().size());
        result.put("capabilities", createCapabilityStatus(handlerContext, database.getDatabase()));
        result.put("resource", MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s", MCPUriTemplateUtils.encodePathSegment(database.getDatabase())),
                "logical-database", "inspect_detail", "Read this logical database resource for metadata details.", "databases"));
        return result;
    }

    private Map<String, Object> createCapabilityStatus(final MCPDatabaseHandlerContext handlerContext, final String databaseName) {
        return handlerContext.getCapabilityFacade().provide(databaseName).map(this::createCapabilityStatus).orElseGet(this::createUnavailableCapabilityStatus);
    }

    private Map<String, Object> createCapabilityStatus(final MCPDatabaseCapability capability) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("available", true);
        result.put("supports_explain_analyze", capability.isSupportsExplainAnalyze());
        result.put("supported_statement_classes", capability.getSupportedStatementClasses().stream().map(Enum::name).toList());
        result.put("supported_metadata_object_types", capability.getSupportedMetadataObjectTypes().stream().map(Enum::name).toList());
        return result;
    }

    private Map<String, Object> createUnavailableCapabilityStatus() {
        Map<String, Object> result = new LinkedHashMap<>(1, 1F);
        result.put("available", false);
        return result;
    }
}
