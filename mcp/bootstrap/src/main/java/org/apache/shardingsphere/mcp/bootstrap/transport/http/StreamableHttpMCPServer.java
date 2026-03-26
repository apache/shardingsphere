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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.ProtocolVersions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityView;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.execute.ExecutionRequest;
import org.apache.shardingsphere.mcp.metadata.MetadataRefreshCoordinator;
import org.apache.shardingsphere.mcp.protocol.ErrorCode;
import org.apache.shardingsphere.mcp.protocol.ExecuteQueryResponse;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.resource.MetadataObjectType;
import org.apache.shardingsphere.mcp.resource.ResourceLoadResult;
import org.apache.shardingsphere.mcp.resource.ResourceRequest;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.tool.ToolDispatchResult;
import org.apache.shardingsphere.mcp.tool.ToolRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SDK-backed HTTP listener for the MCP Streamable HTTP runtime.
 */
public final class StreamableHttpMCPServer {
    
    private static final String SESSION_HEADER = "MCP-Session-Id";
    
    private static final String PROTOCOL_HEADER = "MCP-Protocol-Version";
    
    private static final String JSON_CONTENT_TYPE = "application/json";
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private static final String ORIGIN_HEADER = "Origin";
    
    private static final String MISSING_SESSION_MESSAGE = "Session ID required in mcp-session-id header";
    
    private static final String PROTOCOL_MISMATCH_MESSAGE = "Protocol version mismatch.";
    
    private static final String INVALID_ORIGIN_MESSAGE = "Origin is not allowed for the current binding.";
    
    private static final String RESOURCE_SCHEME_PREFIX = "shardingsphere://";
    
    private static final String FIXED_PROTOCOL_VERSION = ProtocolVersions.MCP_2025_11_25;
    
    private static final String RESOURCE_CAPABILITIES = "shardingsphere://capabilities";
    
    private static final String RESOURCE_DATABASES = "shardingsphere://databases";
    
    private final HttpTransportConfiguration transportConfiguration;
    
    private final MCPServerRegistry serverRegistry;
    
    private final MCPRuntimeServices runtimeServices;
    
    private final McpJsonMapper jsonMapper = createJsonMapper();
    
    private final MetadataCatalog metadataCatalog;
    
    private final DatabaseRuntime databaseRuntime;
    
    private final MetadataRefreshCoordinator metadataRefreshCoordinator;
    
    private Tomcat tomcat;
    
    private Connector connector;
    
    private Path baseDirectory;
    
    private SdkStreamableHttpServlet transportProvider;
    
    private McpSyncServer syncServer;
    
    @Getter
    private boolean running;
    
    /**
     * Construct one HTTP MCP server with caller-provided runtime metadata.
     *
     * @param transportConfiguration HTTP transport configuration
     * @param serverRegistry server registry
     * @param runtimeServices runtime services
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     */
    public StreamableHttpMCPServer(final HttpTransportConfiguration transportConfiguration, final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                                   final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        this.transportConfiguration = Objects.requireNonNull(transportConfiguration, "transportConfiguration cannot be null");
        this.serverRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null");
        this.metadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        this.databaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
        metadataRefreshCoordinator = runtimeServices.getMetadataRefreshCoordinator();
    }
    
    /**
     * Start the HTTP listener.
     *
     * @throws IOException when the listener cannot be created
     */
    public void start() throws IOException {
        if (running) {
            return;
        }
        transportProvider = new SdkStreamableHttpServlet(serverRegistry.getSessionManager(), databaseRuntime, metadataRefreshCoordinator, jsonMapper,
                transportConfiguration.getBindHost(), transportConfiguration.getEndpointPath());
        syncServer = createSyncServer();
        try {
            tomcat = new Tomcat();
            baseDirectory = Files.createTempDirectory("shardingsphere-mcp-tomcat");
            connector = new Connector();
            connector.setPort(transportConfiguration.getPort());
            connector.setProperty("address", transportConfiguration.getBindHost());
            tomcat.setBaseDir(baseDirectory.toString());
            tomcat.setConnector(connector);
            Context context = tomcat.addContext("", baseDirectory.toString());
            Wrapper servletWrapper = Tomcat.addServlet(context, "mcp-streamable-http", transportProvider);
            servletWrapper.setAsyncSupported(true);
            context.addServletMappingDecoded(transportConfiguration.getEndpointPath(), "mcp-streamable-http");
            tomcat.start();
            running = true;
        } catch (final LifecycleException ex) {
            stop();
            throw new IOException("Failed to start embedded Tomcat runtime.", ex);
        }
    }
    
    /**
     * Stop the HTTP listener.
     */
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer.close();
            syncServer = null;
        }
        if (null != transportProvider) {
            transportProvider.shutdown();
            transportProvider = null;
        }
        if (null != tomcat) {
            try {
                tomcat.stop();
            } catch (final LifecycleException ignored) {
            }
            try {
                tomcat.destroy();
            } catch (final LifecycleException ignored) {
            }
            tomcat = null;
        }
        connector = null;
        deleteBaseDirectory();
        running = false;
    }
    
    /**
     * Get the local port that the listener actually bound to.
     *
     * @return bound port
     */
    public int getLocalPort() {
        return null == connector ? transportConfiguration.getPort() : connector.getLocalPort();
    }
    
    private McpSyncServer createSyncServer() {
        return McpServer.sync(transportProvider)
                .jsonMapper(jsonMapper)
                .serverInfo("Apache ShardingSphere MCP", resolveServerVersion())
                .instructions("Apache ShardingSphere MCP bootstrap runtime")
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
    
    private McpJsonMapper createJsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return new JacksonMcpJsonMapper(objectMapper);
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
        return capability.isPresent() ? successToolResult(capability.get()) : errorToolResult("not_found", "Database capability does not exist.");
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
            return capability.isPresent() ? capability.get() : createErrorPayload("not_found", "Database capability does not exist.");
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
        return Optional.ofNullable(StreamableHttpMCPServer.class.getPackage().getImplementationVersion()).orElse("development");
    }
    
    private void deleteBaseDirectory() {
        if (null == baseDirectory) {
            return;
        }
        try {
            Files.walk(baseDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(each -> {
                        try {
                            Files.deleteIfExists(each);
                        } catch (final IOException ignored) {
                        }
                    });
        } catch (final IOException ignored) {
        }
        baseDirectory = null;
    }
    
    private static final class SdkStreamableHttpServlet extends HttpServlet implements McpStreamableServerTransportProvider {
        
        private final MCPSessionManager sessionManager;
        
        private final DatabaseRuntime databaseRuntime;
        
        private final MetadataRefreshCoordinator metadataRefreshCoordinator;
        
        private final HttpServletStreamableServerTransportProvider delegate;
        
        private final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
        
        private final String bindHost;
        
        private SdkStreamableHttpServlet(final MCPSessionManager sessionManager, final DatabaseRuntime databaseRuntime,
                                         final MetadataRefreshCoordinator metadataRefreshCoordinator,
                                         final McpJsonMapper jsonMapper, final String bindHost, final String endpointPath) {
            this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
            this.databaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
            this.metadataRefreshCoordinator = Objects.requireNonNull(metadataRefreshCoordinator, "metadataRefreshCoordinator cannot be null");
            this.bindHost = Objects.requireNonNull(bindHost, "bindHost cannot be null");
            delegate = HttpServletStreamableServerTransportProvider.builder()
                    .jsonMapper(Objects.requireNonNull(jsonMapper, "jsonMapper cannot be null"))
                    .mcpEndpoint(Objects.requireNonNull(endpointPath, "endpointPath cannot be null"))
                    .contextExtractor(this::createTransportContext)
                    .build();
        }
        
        @Override
        public List<String> protocolVersions() {
            return List.of(FIXED_PROTOCOL_VERSION);
        }
        
        @Override
        public void setSessionFactory(final McpStreamableServerSession.Factory sessionFactory) {
            delegate.setSessionFactory(initializeRequest -> {
                McpSchema.InitializeRequest actualInitializeRequest = normalizeInitializeRequest(initializeRequest);
                McpStreamableServerSession.McpStreamableServerSessionInit result = sessionFactory.startSession(actualInitializeRequest);
                String sessionId = result.session().getId();
                sessionManager.createSession(sessionId);
                activeSessionIds.add(sessionId);
                return result;
            });
        }
        
        @Override
        public reactor.core.publisher.Mono<Void> notifyClients(final String method, final Object params) {
            return delegate.notifyClients(method, params);
        }
        
        @Override
        public reactor.core.publisher.Mono<Void> notifyClient(final String sessionId, final String method, final Object params) {
            return delegate.notifyClient(sessionId, method, params);
        }
        
        @Override
        public reactor.core.publisher.Mono<Void> closeGracefully() {
            return delegate.closeGracefully().doFinally(ignored -> closeManagedSessions());
        }
        
        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
            if (validationFailure.isPresent()) {
                writeResponse(response, validationFailure.get());
                return;
            }
            delegate.service(withDefaultAcceptHeader(request), response);
        }
        
        @Override
        protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            String sessionId = getHeader(request, SESSION_HEADER);
            if (!sessionId.isEmpty()) {
                Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
                if (validationFailure.isPresent()) {
                    writeResponse(response, validationFailure.get());
                    return;
                }
                delegate.service(withDefaultAcceptHeader(request), response);
                return;
            }
            Map<String, String> headers = extractHeaders(request);
            Optional<ResponseStatus> initializationFailure = validateInitializeRequest(headers);
            if (initializationFailure.isPresent()) {
                writeResponse(response, initializationFailure.get());
                return;
            }
            delegate.service(withDefaultAcceptHeader(request), new InitializeResponseHeaderWrapper(response, this));
        }
        
        @Override
        protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
            Optional<ResponseStatus> validationFailure = validateFollowUpRequest(request);
            if (validationFailure.isPresent()) {
                writeResponse(response, validationFailure.get());
                return;
            }
            String sessionId = getHeader(request, SESSION_HEADER);
            delegate.service(withDefaultAcceptHeader(request), response);
            if (200 == response.getStatus() && activeSessionIds.remove(sessionId)) {
                metadataRefreshCoordinator.clearSession(sessionId);
                databaseRuntime.closeSession(sessionId);
                sessionManager.closeSession(sessionId);
            }
        }
        
        @Override
        public void destroy() {
            try {
                delegate.destroy();
            } finally {
                closeManagedSessions();
                super.destroy();
            }
        }
        
        private HttpServletRequest withDefaultAcceptHeader(final HttpServletRequest request) {
            if (!getHeader(request, ACCEPT_HEADER).isEmpty()) {
                return request;
            }
            return new AcceptHeaderRequestWrapper(request);
        }
        
        private McpTransportContext createTransportContext(final HttpServletRequest request) {
            return McpTransportContext.create(Collections.emptyMap());
        }
        
        private Optional<ResponseStatus> validateInitializeRequest(final Map<String, String> headers) {
            return validateOrigin(headers);
        }
        
        private Optional<ResponseStatus> validateFollowUpRequest(final HttpServletRequest request) {
            String sessionId = getHeader(request, SESSION_HEADER);
            if (sessionId.isEmpty()) {
                return Optional.of(new ResponseStatus(400, MISSING_SESSION_MESSAGE));
            }
            Map<String, String> headers = extractHeaders(request);
            Optional<ResponseStatus> originFailure = validateOrigin(headers);
            if (originFailure.isPresent()) {
                return originFailure;
            }
            if (!sessionManager.hasSession(sessionId)) {
                return Optional.of(new ResponseStatus(404, "Session does not exist."));
            }
            String actualProtocolVersion = normalizeProtocolVersion(getHeader(request, PROTOCOL_HEADER));
            String negotiatedProtocolVersion = FIXED_PROTOCOL_VERSION;
            if (!negotiatedProtocolVersion.equals(actualProtocolVersion)) {
                return Optional.of(new ResponseStatus(400, PROTOCOL_MISMATCH_MESSAGE));
            }
            return Optional.empty();
        }
        
        private Optional<ResponseStatus> validateOrigin(final Map<String, String> headers) {
            if (!isLocalBinding()) {
                return Optional.empty();
            }
            String origin = getHeader(headers, ORIGIN_HEADER);
            if (origin.isEmpty()) {
                return Optional.empty();
            }
            try {
                String host = Optional.ofNullable(URI.create(origin).getHost()).orElse("");
                return isLoopbackHost(host) ? Optional.empty() : Optional.of(new ResponseStatus(403, INVALID_ORIGIN_MESSAGE));
            } catch (final IllegalArgumentException ignored) {
                return Optional.of(new ResponseStatus(403, INVALID_ORIGIN_MESSAGE));
            }
        }
        
        private boolean isLocalBinding() {
            return isLoopbackHost(bindHost);
        }
        
        private boolean isLoopbackHost(final String host) {
            String actualHost = null == host ? "" : host.trim().toLowerCase(Locale.ENGLISH);
            return "127.0.0.1".equals(actualHost) || "localhost".equals(actualHost) || "::1".equals(actualHost);
        }
        
        private Map<String, String> extractHeaders(final HttpServletRequest request) {
            Map<String, String> result = new LinkedHashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String each = headerNames.nextElement();
                String value = request.getHeader(each);
                if (null != value && !value.trim().isEmpty()) {
                    result.put(each, value.trim());
                }
            }
            return result;
        }
        
        private String getHeader(final HttpServletRequest request, final String headerName) {
            return getHeader(extractHeaders(request), headerName);
        }
        
        private String getHeader(final Map<String, String> headers, final String headerName) {
            String result = headers.get(headerName);
            if (null != result) {
                return result.trim();
            }
            for (Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(headerName)) {
                    return entry.getValue().trim();
                }
            }
            return "";
        }
        
        private McpSchema.InitializeRequest normalizeInitializeRequest(final McpSchema.InitializeRequest initializeRequest) {
            String actualProtocolVersion = normalizeProtocolVersion(initializeRequest.protocolVersion());
            if (actualProtocolVersion.equals(initializeRequest.protocolVersion())) {
                return initializeRequest;
            }
            return new McpSchema.InitializeRequest(actualProtocolVersion, initializeRequest.capabilities(), initializeRequest.clientInfo(), initializeRequest.meta());
        }
        
        private void writeResponse(final HttpServletResponse response, final ResponseStatus responseStatus) throws IOException {
            response.setStatus(responseStatus.getStatusCode());
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(JsonUtils.toJsonString(Map.of("message", responseStatus.getMessage())));
            response.getWriter().flush();
        }
        
        private String normalizeProtocolVersion(final String rawProtocolVersion) {
            String actualProtocolVersion = null == rawProtocolVersion ? "" : rawProtocolVersion.trim();
            return actualProtocolVersion.isEmpty() ? FIXED_PROTOCOL_VERSION : actualProtocolVersion;
        }
        
        private void closeManagedSessions() {
            Set<String> sessionIds = new LinkedHashSet<>(activeSessionIds);
            activeSessionIds.clear();
            for (String each : sessionIds) {
                metadataRefreshCoordinator.clearSession(each);
                databaseRuntime.closeSession(each);
                sessionManager.closeSession(each);
            }
        }
        
        private void shutdown() {
            closeGracefully().block();
        }
    }
    
    private static final class InitializeResponseHeaderWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        
        private final SdkStreamableHttpServlet owner;
        
        private InitializeResponseHeaderWrapper(final HttpServletResponse response, final SdkStreamableHttpServlet owner) {
            super(response);
            this.owner = owner;
        }
        
        @Override
        public void setHeader(final String name, final String value) {
            super.setHeader(name, value);
            addNegotiatedProtocolHeader(name, value);
        }
        
        @Override
        public void addHeader(final String name, final String value) {
            super.addHeader(name, value);
            addNegotiatedProtocolHeader(name, value);
        }
        
        private void addNegotiatedProtocolHeader(final String name, final String value) {
            if (SESSION_HEADER.equalsIgnoreCase(name) && null != value && !value.trim().isEmpty()) {
                super.setHeader(PROTOCOL_HEADER, FIXED_PROTOCOL_VERSION);
            }
        }
    }
    
    private static final class ResponseStatus {
        
        @Getter
        private final int statusCode;
        
        @Getter
        private final String message;
        
        private ResponseStatus(final int statusCode, final String message) {
            this.statusCode = statusCode;
            this.message = Objects.requireNonNull(message, "message cannot be null");
        }
    }
    
    private static final class AcceptHeaderRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        private AcceptHeaderRequestWrapper(final HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getHeader(final String name) {
            if (ACCEPT_HEADER.equalsIgnoreCase(name)) {
                return DEFAULT_ACCEPT;
            }
            return super.getHeader(name);
        }
        
        @Override
        public Enumeration<String> getHeaders(final String name) {
            if (ACCEPT_HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(List.of(DEFAULT_ACCEPT));
            }
            return super.getHeaders(name);
        }
        
        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> result = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
            result.add(ACCEPT_HEADER);
            return Collections.enumeration(result);
        }
    }
}
