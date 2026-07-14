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

import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConnectionException;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.resource.MCPUriPathSegmentUtils;
import org.apache.shardingsphere.mcp.support.security.MCPRuntimeProtectionPolicy;

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
    public String getResourceUriTemplate() {
        return URI_PATTERN;
    }
    
    @Override
    public MCPResponse handle(final MCPDatabaseHandlerContext handlerContext, final MCPUriVariables uriVariables) {
        List<RuntimeDatabaseProfile> databases = handlerContext.getMetadataQueryFacade().queryDatabases();
        boolean hasConfiguredDatabase = !databases.isEmpty();
        Map<String, Object> result = new LinkedHashMap<>(15, 1F);
        result.put("response_mode", MCPResponseMode.RUNTIME);
        result.put(MCPPayloadFieldNames.SUMMARY, createSummary(hasConfiguredDatabase, databases.size()));
        result.put("server_status", hasConfiguredDatabase ? "ready" : "configuration_required");
        result.put("status", hasConfiguredDatabase ? "available" : "configuration_required");
        result.put("transport", handlerContext.getActiveTransport());
        result.put("active_transport", handlerContext.getActiveTransport());
        result.put("transport_security_summary", createTransportSecuritySummary(handlerContext.getActiveTransport()));
        result.put("configured_database_count", databases.size());
        result.put("databases", databases.stream().map(each -> createDatabaseStatus(handlerContext, each)).toList());
        result.put("readiness", createReadiness(hasConfiguredDatabase));
        result.put("runtime_protection", MCPRuntimeProtectionPolicy.createRuntimeProtectionPayload());
        result.put("redaction_summary", Map.of("categories", List.of(), "redacted_count", 0, "marker", "******"));
        result.put("diagnostics", createDiagnostics(hasConfiguredDatabase));
        result.put(MCPPayloadFieldNames.RESOURCES_TO_READ, createResourcesToRead(hasConfiguredDatabase));
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createNextActions(hasConfiguredDatabase));
        return new MCPMapResponse(result);
    }
    
    private String createSummary(final boolean hasConfiguredDatabase, final int configuredDatabaseCount) {
        return hasConfiguredDatabase
                ? String.format("Runtime is ready with %d configured logical database(s).", configuredDatabaseCount)
                : "Runtime requires at least one configured logical database before metadata discovery or SQL execution.";
    }
    
    private Map<String, Object> createTransportSecuritySummary(final String activeTransport) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("transport", activeTransport);
        result.put("authentication", "http".equalsIgnoreCase(activeTransport) ? "not_enabled_by_mcp_transport" : "local_client_process");
        result.put("recommended_exposure", "http".equalsIgnoreCase(activeTransport) ? "loopback_or_trusted_gateway" : "local_stdio_session");
        result.put("model_action", "Do not request or echo JDBC URLs, credentials, raw environment variables, or stack traces.");
        return result;
    }
    
    private Map<String, Object> createReadiness(final boolean hasConfiguredDatabase) {
        Map<String, Object> result = new LinkedHashMap<>(hasConfiguredDatabase ? 3 : 4, 1F);
        result.put("ready", hasConfiguredDatabase);
        result.put("token_safe", true);
        result.put("resource_uri", URI_PATTERN);
        if (hasConfiguredDatabase) {
            return result;
        }
        result.put(MCPPayloadFieldNames.REASON, "No runtime databases are configured.");
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
                RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED,
                RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT,
                RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION,
                RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE,
                RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED,
                RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE);
    }
    
    private List<Map<String, Object>> createDiagnosticOperatorActions() {
        return List.of(
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_MISSING_JDBC_DRIVER, "Install the configured runtime database JDBC driver."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_AUTHENTICATION_FAILED, "Check runtime database credentials outside MCP."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_AUTHORIZATION_FAILED, "Check metadata and SQL privileges for the configured runtime database account."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_TIMEOUT, "Check database reachability and timeout settings."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_INVALID_CONFIGURATION, "Fix runtimeDatabases JDBC URL, driver, or binding configuration."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_UNAVAILABLE, "Check database service availability and network access."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_CONNECTION_FAILED, "Inspect runtime database connection settings outside MCP."),
                createDiagnosticOperatorAction(RuntimeDatabaseConnectionException.CATEGORY_DATABASE_NOT_VISIBLE, "Check the configured logical database name and account visibility outside MCP."));
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
                "Read full MCP capabilities before choosing tools.", MCPPayloadFieldNames.RESOURCES_TO_READ);
        if (!hasConfiguredDatabase) {
            return List.of(capabilitiesResource);
        }
        return List.of(capabilitiesResource, MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "read_first",
                "Read logical databases before choosing a database scope.", MCPPayloadFieldNames.RESOURCES_TO_READ));
    }
    
    private List<Map<String, Object>> createNextActions(final boolean hasConfiguredDatabase) {
        if (hasConfiguredDatabase) {
            return List.of(MCPNextActionUtils.readResource("shardingsphere://databases", "Read logical databases before choosing a database scope."));
        }
        Map<String, Object> capabilityAction = MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read the full capability catalog before choosing tools.");
        return MCPNextActionUtils.ordered(capabilityAction, MCPNextActionUtils.dependsOn(MCPNextActionUtils.askUser(
                "Ask the operator to configure at least one runtimeDatabases entry before metadata discovery or SQL execution.", List.of("runtimeDatabases")), 1));
    }
    
    private Map<String, Object> createDatabaseStatus(final MCPDatabaseHandlerContext handlerContext, final RuntimeDatabaseProfile database) {
        Optional<MCPDatabaseCapability> capability = handlerContext.getCapabilityFacade().provide(database.getDatabase());
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("database", database.getDatabase());
        result.put("database_type", database.getDatabaseType());
        result.put("driver_category", database.getDatabaseType().toLowerCase());
        result.put("schema_count", 0);
        result.put("metadata_visibility", "ready");
        result.put("capabilities", capability.map(this::createCapabilityStatus).orElseGet(this::createUnavailableCapabilityStatus));
        result.put("capability_visibility", capability.isPresent() ? "ready" : "unavailable");
        result.put("feature_visibility", "ready");
        result.put(MCPPayloadFieldNames.RESOURCE, MCPResourceHintUtils.create(String.format("shardingsphere://databases/%s", MCPUriPathSegmentUtils.encodePathSegment(database.getDatabase())),
                "logical-database", "inspect_detail", "Read this logical database resource for metadata details.", "databases"));
        return result;
    }
    
    private Map<String, Object> createCapabilityStatus(final MCPDatabaseCapability capability) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("available", true);
        result.put("supports_explain", capability.supportsExplain());
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
