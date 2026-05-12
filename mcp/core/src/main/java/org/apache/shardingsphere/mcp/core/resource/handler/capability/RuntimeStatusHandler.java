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
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorRegistry;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.resource.MCPUriTemplateUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        boolean hasConfiguredDatabase = !databases.isEmpty();
        Map<String, Object> result = new LinkedHashMap<>(13, 1F);
        result.put("response_mode", MCPResponseMode.RUNTIME);
        result.put("server_status", hasConfiguredDatabase ? "ready" : "configuration_required");
        result.put("status", hasConfiguredDatabase ? "available" : "configuration_required");
        result.put("transport", handlerContext.getActiveTransport());
        result.put("active_transport", handlerContext.getActiveTransport());
        result.put("configured_database_count", databases.size());
        result.put("databases", databases.stream().map(each -> createDatabaseStatus(handlerContext, each)).toList());
        result.put("readiness", createReadiness(hasConfiguredDatabase));
        result.put("redaction_summary", Map.of("categories", List.of(), "redacted_count", 0, "marker", "******"));
        result.put("diagnostics", createDiagnostics(hasConfiguredDatabase));
        result.put("capability_fingerprint", MCPDescriptorRegistry.getDescriptorCatalogFingerprint());
        result.put("resources_to_read", createResourcesToRead(hasConfiguredDatabase));
        result.put("next_actions", createNextActions(hasConfiguredDatabase));
        return new MCPMapResponse(result);
    }
    
    private Map<String, Object> createReadiness(final boolean hasConfiguredDatabase) {
        Map<String, Object> result = new LinkedHashMap<>(hasConfiguredDatabase ? 3 : 4, 1F);
        result.put("ready", hasConfiguredDatabase);
        result.put("token_safe", true);
        result.put("resource_uri", URI_PATTERN);
        if (hasConfiguredDatabase) {
            return result;
        }
        result.put("reason", "No runtime databases are configured.");
        return result;
    }
    
    private Map<String, Object> createDiagnostics(final boolean hasConfiguredDatabase) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("current_category", hasConfiguredDatabase ? "ready" : RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION);
        result.put("safe_categories", createSafeRuntimeCategories());
        result.put("operator_next_actions", createDiagnosticOperatorActions());
        result.put("secret_policy", "Expose only categories and operator actions; never expose JDBC URLs, credentials, raw environment variables, or stack traces.");
        return result;
    }
    
    private List<String> createSafeRuntimeCategories() {
        return List.of(
                RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER,
                RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED,
                RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT,
                RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION,
                RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE,
                RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED);
    }
    
    private List<Map<String, Object>> createDiagnosticOperatorActions() {
        return List.of(
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER, "Install the configured runtime database JDBC driver."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED, "Check runtime database credentials outside MCP."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT, "Check database reachability and timeout settings."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION, "Fix runtimeDatabases databaseType, driver, or binding configuration."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE, "Check database service availability and network access."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED, "Inspect runtime database connection settings outside MCP."));
    }
    
    private Map<String, Object> createDiagnosticOperatorAction(final String category, final String operatorAction) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("category", category);
        result.put("operator_action", operatorAction);
        result.put("secret_safe", true);
        return result;
    }
    
    private List<Map<String, Object>> createResourcesToRead(final boolean hasConfiguredDatabase) {
        Map<String, Object> capabilitiesResource = MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first",
                "Read full MCP capabilities before choosing tools.", "resources_to_read");
        if (!hasConfiguredDatabase) {
            return List.of(capabilitiesResource);
        }
        return List.of(capabilitiesResource, MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "read_first",
                "Read logical databases before choosing a database scope.", "resources_to_read"));
    }
    
    private List<Map<String, Object>> createNextActions(final boolean hasConfiguredDatabase) {
        Map<String, Object> capabilityAction = MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read the full capability catalog before choosing tools.");
        if (hasConfiguredDatabase) {
            return List.of();
        }
        return MCPNextActionUtils.ordered(capabilityAction, MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser(
                "Ask the operator to configure at least one runtimeDatabases entry before metadata discovery or SQL execution.", List.of("runtimeDatabases"), false), 1));
    }
    
    private Map<String, Object> createDatabaseStatus(final MCPDatabaseHandlerContext handlerContext, final MCPDatabaseMetadata database) {
        Optional<MCPDatabaseCapability> capability = handlerContext.getCapabilityFacade().provide(database.getDatabase());
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("database", database.getDatabase());
        result.put("database_type", database.getDatabaseType());
        result.put("driver_category", database.getDatabaseType().toLowerCase());
        result.put("schema_count", database.getSchemas().size());
        result.put("metadata_visibility", "ready");
        result.put("capabilities", capability.map(this::createCapabilityStatus).orElseGet(this::createUnavailableCapabilityStatus));
        result.put("capability_visibility", capability.isPresent() ? "ready" : "unavailable");
        result.put("feature_visibility", "ready");
        result.put("resource", MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s", MCPUriTemplateUtils.encodePathSegment(database.getDatabase())),
                "logical-database", "inspect_detail", "Read this logical database resource for metadata details.", "databases"));
        return result;
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
