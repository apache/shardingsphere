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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.ResourceRequest;
import org.apache.shardingsphere.mcp.tool.ToolDispatchResult;
import org.apache.shardingsphere.mcp.tool.ToolRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shared sync MCP server factory for HTTP and STDIO transports.
 */
@RequiredArgsConstructor
public final class MCPSyncServerFactory {
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String RESOURCE_SCHEME_PREFIX = "shardingsphere://";
    
    private static final String RESOURCE_CAPABILITIES = "shardingsphere://capabilities";
    
    private static final String RESOURCE_DATABASES = "shardingsphere://databases";
    
    private final MCPRuntimeServices runtimeServices;
    
    private final McpJsonMapper jsonMapper;
    
    private final MetadataCatalog metadataCatalog;
    
    private final DatabaseRuntime databaseRuntime;
    
    /**
     * Create one JSON mapper for MCP transports.
     *
     * @return MCP JSON mapper
     */
    public static McpJsonMapper createJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return new JacksonMcpJsonMapper(objectMapper);
    }
    
    /**
     * Create one sync server for single-session transports.
     *
     * @param transportProvider MCP server transport provider
     * @return sync server
     */
    public McpSyncServer create(final McpServerTransportProvider transportProvider) {
        return create(McpServer.sync(transportProvider));
    }
    
    /**
     * Create one sync server for streamable transports.
     *
     * @param transportProvider MCP streamable transport provider
     * @return sync server
     */
    public McpSyncServer create(final McpStreamableServerTransportProvider transportProvider) {
        return create(McpServer.sync(transportProvider));
    }
    
    private McpSyncServer create(final McpServer.SyncSpecification<?> specification) {
        return specification.jsonMapper(jsonMapper)
                .serverInfo(MCPTransportConstants.SERVER_NAME, resolveServerVersion())
                .instructions(MCPTransportConstants.SERVER_INSTRUCTIONS)
                .capabilities(createServerCapabilities())
                .tools(createToolSpecifications())
                .resources(createResourceSpecifications())
                .resourceTemplates(createResourceTemplateSpecifications())
                .build();
    }
    
    private ServerCapabilities createServerCapabilities() {
        return McpSchema.ServerCapabilities.builder()
                .resources(Boolean.FALSE, Boolean.FALSE)
                .tools(Boolean.FALSE)
                .build();
    }
    
    private List<SyncToolSpecification> createToolSpecifications() {
        List<SyncToolSpecification> result = new ArrayList<>();
        for (String each : runtimeServices.getCapabilityAssembler().assembleServiceCapability().getSupportedTools()) {
            result.add(new SyncToolSpecification.Builder()
                    .tool(createToolDefinition(each))
                    .callHandler(this::handleToolCall)
                    .build());
        }
        return result;
    }
    
    private McpSchema.Tool createToolDefinition(final String toolName) {
        return McpSchema.Tool.builder()
                .name(toolName)
                .title(toTitle(toolName))
                .description("ShardingSphere MCP tool: " + toolName)
                .inputSchema(createToolInputSchema(toolName))
                .build();
    }
    
    private McpSchema.JsonSchema createToolInputSchema(final String toolName) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        switch (toolName) {
            case "list_schemas":
                properties.put("database", createStringSchema("Logical database name."));
                required.add("database");
                break;
            case "list_tables":
            case "list_views":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Schema name."));
                properties.put("search", createStringSchema("Optional fuzzy filter."));
                properties.put("page_size", createIntegerSchema("Requested page size."));
                properties.put("page_token", createStringSchema("Opaque pagination token."));
                required.add("database");
                required.add("schema");
                break;
            case "list_columns":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Schema name."));
                properties.put("object_type", createStringSchema("Parent object type: table or view."));
                properties.put("object_name", createStringSchema("Parent object name."));
                properties.put("search", createStringSchema("Optional fuzzy filter."));
                properties.put("page_size", createIntegerSchema("Requested page size."));
                properties.put("page_token", createStringSchema("Opaque pagination token."));
                required.add("database");
                required.add("schema");
                required.add("object_type");
                required.add("object_name");
                break;
            case "list_indexes":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Schema name."));
                properties.put("table", createStringSchema("Table name."));
                properties.put("search", createStringSchema("Optional fuzzy filter."));
                properties.put("page_size", createIntegerSchema("Requested page size."));
                properties.put("page_token", createStringSchema("Opaque pagination token."));
                required.add("database");
                required.add("schema");
                required.add("table");
                break;
            case "search_metadata":
                properties.put("database", createStringSchema("Optional logical database name."));
                properties.put("schema", createStringSchema("Optional schema name."));
                properties.put("query", createStringSchema("Search query."));
                properties.put("object_types", createArraySchema("Optional object-type filter."));
                properties.put("page_size", createIntegerSchema("Requested page size."));
                properties.put("page_token", createStringSchema("Opaque pagination token."));
                required.add("query");
                break;
            case "describe_table":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Schema name."));
                properties.put("table", createStringSchema("Table name."));
                required.add("database");
                required.add("schema");
                required.add("table");
                break;
            case "describe_view":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Schema name."));
                properties.put("view", createStringSchema("View name."));
                required.add("database");
                required.add("schema");
                required.add("view");
                break;
            case "get_capabilities":
                properties.put("database", createStringSchema("Optional logical database name."));
                break;
            case "execute_query":
                properties.put("database", createStringSchema("Logical database name."));
                properties.put("schema", createStringSchema("Optional schema name."));
                properties.put("sql", createStringSchema("Single SQL statement."));
                properties.put("max_rows", createIntegerSchema("Optional maximum row count."));
                properties.put("timeout_ms", createIntegerSchema("Optional timeout in milliseconds."));
                required.add("database");
                required.add("sql");
                break;
            default:
                break;
        }
        return new McpSchema.JsonSchema("object", properties, required, Boolean.TRUE, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private Map<String, Object> createStringSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "string");
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createIntegerSchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "integer");
        result.put("description", description);
        return result;
    }
    
    private Map<String, Object> createArraySchema(final String description) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "array");
        result.put("description", description);
        result.put("items", createStringSchema("Array element value."));
        return result;
    }
    
    private McpSchema.CallToolResult handleToolCall(final McpSyncServerExchange exchange, final McpSchema.CallToolRequest request) {
        Map<String, Object> arguments = Optional.ofNullable(request.arguments()).orElse(Collections.emptyMap());
        switch (request.name()) {
            case "list_databases":
            case "list_schemas":
            case "list_tables":
            case "list_views":
            case "list_columns":
            case "list_indexes":
            case "search_metadata":
            case "describe_table":
            case "describe_view":
                return handleMetadataToolCall(request.name(), arguments);
            case "get_capabilities":
                return handleGetCapabilities(arguments);
            case "execute_query":
                return handleExecuteQuery(exchange.sessionId(), arguments);
            default:
                return errorToolResult("invalid_request", "Unsupported tool.");
        }
    }
    
    private McpSchema.CallToolResult handleMetadataToolCall(final String toolName, final Map<String, Object> arguments) {
        ToolDispatchResult result = runtimeServices.getMetadataToolDispatcher().dispatch(metadataCatalog, createMetadataToolRequest(toolName, arguments));
        if (!result.isSuccessful()) {
            return errorToolResult(toDomainErrorCode(result.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), result.getMessage());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", result.getMetadataObjects());
        if (!result.getNextPageToken().isEmpty()) {
            payload.put("next_page_token", result.getNextPageToken());
        }
        return successToolResult(payload);
    }
    
    private ToolRequest createMetadataToolRequest(final String toolName, final Map<String, Object> arguments) {
        String database = getStringArgument(arguments, "database");
        String schema = getStringArgument(arguments, "schema");
        String query = getStringArgument(arguments, "query");
        if (query.isEmpty()) {
            query = getStringArgument(arguments, "search");
        }
        String objectName;
        String parentObjectType;
        switch (toolName) {
            case "list_columns":
                objectName = getStringArgument(arguments, "object_name");
                parentObjectType = getStringArgument(arguments, "object_type").toUpperCase(Locale.ENGLISH);
                break;
            case "list_indexes":
                objectName = getStringArgument(arguments, "table");
                parentObjectType = "TABLE";
                break;
            case "describe_table":
                objectName = getStringArgument(arguments, "table");
                parentObjectType = "";
                break;
            case "describe_view":
                objectName = getStringArgument(arguments, "view");
                parentObjectType = "";
                break;
            default:
                objectName = getStringArgument(arguments, "object_name");
                parentObjectType = getStringArgument(arguments, "parent_object_type");
                break;
        }
        return new ToolRequest(toolName, database, schema, objectName, parentObjectType, query, getObjectTypes(arguments),
                getIntegerArgument(arguments, "page_size", 100), getStringArgument(arguments, "page_token"));
    }
    
    private McpSchema.CallToolResult handleGetCapabilities(final Map<String, Object> arguments) {
        String database = getStringArgument(arguments, "database");
        if (database.isEmpty()) {
            return successToolResult(runtimeServices.getCapabilityAssembler().assembleServiceCapability());
        }
        String databaseType = metadataCatalog.getDatabaseTypes().get(database);
        if (null == databaseType) {
            return errorToolResult("not_found", "Database capability does not exist.");
        }
        Optional<DatabaseCapabilityView> capability = runtimeServices.getCapabilityAssembler().assembleDatabaseCapability(database, databaseType);
        return capability.isPresent() ? successToolResult(toDatabaseCapabilityPayload(capability.get())) : errorToolResult("not_found", "Database capability does not exist.");
    }
    
    private McpSchema.CallToolResult handleExecuteQuery(final String sessionId, final Map<String, Object> arguments) {
        String database = getStringArgument(arguments, "database");
        String sql = getStringArgument(arguments, "sql");
        if (database.isEmpty() || sql.isEmpty()) {
            return errorToolResult("invalid_request", "Database and sql are required.");
        }
        String databaseType = metadataCatalog.getDatabaseTypes().get(database);
        if (null == databaseType) {
            return errorToolResult("not_found", "Database capability does not exist.");
        }
        ExecutionRequest executionRequest = new ExecutionRequest(sessionId, database, databaseType, getStringArgument(arguments, "schema"),
                sql, getIntegerArgument(arguments, "max_rows", 0), getIntegerArgument(arguments, "timeout_ms", 0), databaseRuntime);
        ExecuteQueryResponse response = runtimeServices.getExecuteQueryFacade().execute(executionRequest);
        return response.isSuccessful() ? successToolResult(toExecuteQueryPayload(response))
                : errorToolResult(toDomainErrorCode(response.getError().get().getErrorCode()), response.getError().get().getMessage(), toExecuteQueryPayload(response));
    }
    
    private Map<String, Object> toExecuteQueryPayload(final ExecuteQueryResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("result_kind", response.getResultKind().name().toLowerCase(Locale.ENGLISH));
        payload.put("statement_type", response.getStatementType());
        payload.put("status", response.getStatus());
        if (!response.getColumns().isEmpty()) {
            payload.put("columns", response.getColumns());
        }
        if (!response.getRows().isEmpty()) {
            payload.put("rows", response.getRows());
        }
        if (0 != response.getAffectedRows()) {
            payload.put("affected_rows", response.getAffectedRows());
        }
        if (!response.getMessage().isEmpty()) {
            payload.put("message", response.getMessage());
        }
        payload.put("truncated", response.isTruncated());
        if (response.getError().isPresent()) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error_code", toDomainErrorCode(response.getError().get().getErrorCode()));
            error.put("message", response.getError().get().getMessage());
            payload.put("error", error);
        }
        return payload;
    }
    
    private Map<String, Object> toDatabaseCapabilityPayload(final DatabaseCapabilityView capability) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("database", capability.getDatabase());
        result.put("databaseType", capability.getDatabaseType());
        result.put("minSupportedVersion", capability.getMinSupportedVersion());
        result.put("supportedObjectTypes", capability.getSupportedMetadataObjectTypes());
        result.put("supportedStatementClasses", capability.getSupportedStatementClasses());
        result.put("supportsTransactionControl", capability.isSupportsTransactionControl());
        result.put("supportsSavepoint", capability.isSupportsSavepoint());
        result.put("supportedTransactionStatements", capability.getSupportedTransactionStatements());
        result.put("defaultAutocommit", capability.isDefaultAutocommit());
        result.put("maxRowsDefault", capability.getMaxRowsDefault());
        result.put("maxTimeoutMsDefault", capability.getMaxTimeoutMsDefault());
        result.put("defaultSchemaSemantics", capability.getDefaultSchemaSemantics());
        result.put("supportsCrossSchemaSql", capability.isSupportsCrossSchemaSql());
        result.put("supportsExplainAnalyze", capability.isSupportsExplainAnalyze());
        result.put("ddlTransactionBehavior", capability.getDdlTransactionBehavior());
        result.put("dclTransactionBehavior", capability.getDclTransactionBehavior());
        result.put("explainAnalyzeResultBehavior", capability.getExplainAnalyzeResultBehavior());
        result.put("explainAnalyzeTransactionBehavior", capability.getExplainAnalyzeTransactionBehavior());
        return result;
    }
    
    private McpSchema.CallToolResult successToolResult(final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(payload))
                .isError(Boolean.FALSE)
                .build();
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message) {
        return errorToolResult(errorCode, message, Map.of("error_code", errorCode, "message", message));
    }
    
    private McpSchema.CallToolResult errorToolResult(final String errorCode, final String message, final Object payload) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(payload)
                .addTextContent(JsonUtils.toJsonString(Map.of("error_code", errorCode, "message", message)))
                .isError(Boolean.TRUE)
                .build();
    }
    
    private List<SyncResourceSpecification> createResourceSpecifications() {
        List<SyncResourceSpecification> result = new ArrayList<>();
        for (String each : runtimeServices.getCapabilityAssembler().assembleServiceCapability().getSupportedResources()) {
            if (!isTemplatedResource(each)) {
                result.add(new SyncResourceSpecification(createResource(each), this::handleReadResource));
            }
        }
        return result;
    }
    
    private List<SyncResourceTemplateSpecification> createResourceTemplateSpecifications() {
        List<SyncResourceTemplateSpecification> result = new ArrayList<>();
        for (String each : runtimeServices.getCapabilityAssembler().assembleServiceCapability().getSupportedResources()) {
            if (isTemplatedResource(each)) {
                result.add(new SyncResourceTemplateSpecification(createResourceTemplate(each), this::handleReadResource));
            }
        }
        return result;
    }
    
    private McpSchema.Resource createResource(final String uri) {
        return McpSchema.Resource.builder()
                .uri(uri)
                .name(uri.substring(uri.lastIndexOf('/') + 1))
                .description("ShardingSphere MCP resource: " + uri)
                .mimeType(JSON_CONTENT_TYPE)
                .build();
    }
    
    private McpSchema.ResourceTemplate createResourceTemplate(final String uriTemplate) {
        return McpSchema.ResourceTemplate.builder()
                .uriTemplate(uriTemplate)
                .name(uriTemplate.substring(uriTemplate.lastIndexOf('/') + 1))
                .description("ShardingSphere MCP resource template: " + uriTemplate)
                .mimeType(JSON_CONTENT_TYPE)
                .build();
    }
    
    private McpSchema.ReadResourceResult handleReadResource(final McpSyncServerExchange exchange, final McpSchema.ReadResourceRequest request) {
        Object payload = resolveResourcePayload(request.uri());
        return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(request.uri(), JSON_CONTENT_TYPE, JsonUtils.toJsonString(payload))));
    }
    
    private Object resolveResourcePayload(final String resourceUri) {
        if (RESOURCE_CAPABILITIES.equals(resourceUri)) {
            return runtimeServices.getCapabilityAssembler().assembleServiceCapability();
        }
        if (RESOURCE_DATABASES.equals(resourceUri)) {
            return toResourcePayload(runtimeServices.getMetadataResourceLoader().load(metadataCatalog, new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", "")));
        }
        List<String> segments = splitResourceUri(resourceUri);
        if (segments.size() >= 3 && "databases".equals(segments.get(0)) && "capabilities".equals(segments.get(2))) {
            String database = segments.get(1);
            String databaseType = metadataCatalog.getDatabaseTypes().get(database);
            if (null == databaseType) {
                return createErrorPayload("not_found", "Database capability does not exist.");
            }
            Optional<DatabaseCapabilityView> capability = runtimeServices.getCapabilityAssembler().assembleDatabaseCapability(database, databaseType);
            return capability.isPresent() ? toDatabaseCapabilityPayload(capability.get()) : createErrorPayload("not_found", "Database capability does not exist.");
        }
        Optional<ResourceRequest> resourceRequest = createMetadataResourceRequest(segments);
        return resourceRequest.isPresent() ? toResourcePayload(runtimeServices.getMetadataResourceLoader().load(metadataCatalog, resourceRequest.get()))
                : createErrorPayload("invalid_request", "Unsupported resource URI.");
    }
    
    private Object toResourcePayload(final ResourceLoadResult loadResult) {
        if (!loadResult.isSuccessful()) {
            return createErrorPayload(toDomainErrorCode(loadResult.getErrorCode().orElse(ErrorCode.INVALID_REQUEST)), loadResult.getMessage());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", loadResult.getMetadataObjects());
        return payload;
    }
    
    private Optional<ResourceRequest> createMetadataResourceRequest(final List<String> segments) {
        if (segments.isEmpty() || !"databases".equals(segments.get(0))) {
            return Optional.empty();
        }
        if (1 == segments.size()) {
            return Optional.of(new ResourceRequest("", "", MetadataObjectType.DATABASE, "", "", ""));
        }
        String database = segments.get(1);
        if (2 == segments.size()) {
            return Optional.of(new ResourceRequest("", "", MetadataObjectType.DATABASE, database, "", ""));
        }
        if (!"schemas".equals(segments.get(2))) {
            return Optional.empty();
        }
        return createSchemaResourceRequest(database, segments);
    }
    
    private Optional<ResourceRequest> createSchemaResourceRequest(final String database, final List<String> segments) {
        if (3 == segments.size()) {
            return Optional.of(new ResourceRequest(database, "", MetadataObjectType.SCHEMA, "", "", ""));
        }
        String schema = segments.get(3);
        if (4 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.SCHEMA, schema, "", ""));
        }
        if ("tables".equals(segments.get(4))) {
            return createTableResourceRequest(database, schema, segments);
        }
        if ("views".equals(segments.get(4))) {
            return createViewResourceRequest(database, schema, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createTableResourceRequest(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.TABLE, "", "", ""));
        }
        String table = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.TABLE, table, "", ""));
        }
        if ("columns".equals(segments.get(6))) {
            return createColumnResourceRequest(database, schema, table, "TABLE", segments);
        }
        if ("indexes".equals(segments.get(6))) {
            return createIndexResourceRequest(database, schema, table, segments);
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createViewResourceRequest(final String database, final String schema, final List<String> segments) {
        if (5 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.VIEW, "", "", ""));
        }
        String view = segments.get(5);
        if (6 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.VIEW, view, "", ""));
        }
        if (!"columns".equals(segments.get(6))) {
            return Optional.empty();
        }
        return createColumnResourceRequest(database, schema, view, "VIEW", segments);
    }
    
    private Optional<ResourceRequest> createColumnResourceRequest(final String database, final String schema, final String parentObjectName,
                                                                  final String parentObjectType, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, "", parentObjectType, parentObjectName));
        }
        if (8 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.COLUMN, segments.get(7), parentObjectType, parentObjectName));
        }
        return Optional.empty();
    }
    
    private Optional<ResourceRequest> createIndexResourceRequest(final String database, final String schema, final String table, final List<String> segments) {
        if (7 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.INDEX, "", "TABLE", table));
        }
        if (8 == segments.size()) {
            return Optional.of(new ResourceRequest(database, schema, MetadataObjectType.INDEX, segments.get(7), "TABLE", table));
        }
        return Optional.empty();
    }
    
    private List<String> splitResourceUri(final String resourceUri) {
        if (!resourceUri.startsWith(RESOURCE_SCHEME_PREFIX)) {
            return Collections.emptyList();
        }
        String actualUri = resourceUri.substring(RESOURCE_SCHEME_PREFIX.length());
        if (actualUri.isEmpty()) {
            return Collections.emptyList();
        }
        return List.of(actualUri.split("/"));
    }
    
    private Map<String, Object> createErrorPayload(final String errorCode, final String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error_code", errorCode);
        payload.put("message", message);
        return payload;
    }
    
    private Set<MetadataObjectType> getObjectTypes(final Map<String, Object> arguments) {
        Object rawValue = arguments.get("object_types");
        if (!(rawValue instanceof Collection)) {
            return Collections.emptySet();
        }
        Set<MetadataObjectType> result = new LinkedHashSet<>();
        for (Object each : (Collection<?>) rawValue) {
            if (null == each) {
                continue;
            }
            try {
                result.add(MetadataObjectType.valueOf(each.toString().trim().toUpperCase(Locale.ENGLISH)));
            } catch (final IllegalArgumentException ignored) {
            }
        }
        return result;
    }
    
    private String getStringArgument(final Map<String, Object> arguments, final String name) {
        Object result = arguments.get(name);
        return null == result ? "" : result.toString().trim();
    }
    
    private int getIntegerArgument(final Map<String, Object> arguments, final String name, final int defaultValue) {
        Object result = arguments.get(name);
        if (null == result) {
            return defaultValue;
        }
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        String actualValue = result.toString().trim();
        if (actualValue.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(actualValue);
        } catch (final NumberFormatException ignored) {
            return defaultValue;
        }
    }
    
    private boolean isTemplatedResource(final String resourceUri) {
        return resourceUri.contains("{");
    }
    
    private String toTitle(final String rawValue) {
        String[] segments = rawValue.split("_");
        List<String> words = new ArrayList<>(segments.length);
        for (String each : segments) {
            if (!each.isEmpty()) {
                words.add(Character.toUpperCase(each.charAt(0)) + each.substring(1));
            }
        }
        return String.join(" ", words);
    }
    
    private String toDomainErrorCode(final ErrorCode errorCode) {
        return errorCode.name().toLowerCase(Locale.ENGLISH);
    }
    
    private String resolveServerVersion() {
        return Optional.ofNullable(MCPSyncServerFactory.class.getPackage().getImplementationVersion()).orElse("development");
    }
}
