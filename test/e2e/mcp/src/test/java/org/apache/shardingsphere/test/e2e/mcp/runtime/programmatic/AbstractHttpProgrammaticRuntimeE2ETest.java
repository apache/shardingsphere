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

package org.apache.shardingsphere.test.e2e.mcp.runtime.programmatic;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractHttpProgrammaticRuntimeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private static final String CLIENT_NAME = "mcp-e2e-programmatic";
    
    private GenericContainer<?> container;
    
    private Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
    
    private ProgrammaticRuntimeFixture sharedRuntimeFixture;
    
    @AfterEach
    void tearDownContainer() {
        if (useSharedDatabaseBackedRuntime()) {
            container = null;
            runtimeDatabases = null;
            return;
        }
        if (null != container) {
            container.stop();
            container = null;
        }
        runtimeDatabases = null;
    }
    
    protected final void launchHttpTransport() throws IOException {
        prepareRuntime();
    }
    
    protected final String initializeSession(final HttpClient httpClient) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendInitializeRequest(httpClient);
        assertThat(actual.statusCode(), is(200));
        String result = actual.headers().firstValue("MCP-Session-Id").orElseThrow();
        assertThat(sendInitializedNotification(httpClient, result).statusCode(), is(202));
        return result;
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, MCPHttpTransportTestSupport.createInitializeRequestParams(CLIENT_NAME));
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return sendInitializeRequest(httpClient, Map.of(), initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializeRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                               final Map<String, Object> initializeRequestParams) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), headers, "init-1", "initialize", initializeRequestParams);
    }
    
    protected final HttpResponse<String> sendInitializedNotification(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId),
                MCPHttpTransportTestSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of()));
    }
    
    protected final HttpResponse<String> sendToolCallRequest(final HttpClient httpClient, final String sessionId,
                                                             final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), createSessionHeaders(sessionId), toolName + "-1", "tools/call",
                Map.of("name", toolName, "arguments", arguments));
    }
    
    protected final HttpResponse<String> sendResourceReadRequest(final HttpClient httpClient, final String sessionId,
                                                                 final String resourceUri) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendJsonRpcRequest(httpClient, getEndpointUri(), createSessionHeaders(sessionId), "resource-1", "resources/read", Map.of("uri", resourceUri));
    }
    
    protected final HttpResponse<String> sendDeleteRequest(final HttpClient httpClient, final Map<String, String> headers) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendDeleteRequest(httpClient, getEndpointUri(), headers);
    }
    
    protected final HttpResponse<String> sendRawPostRequest(final HttpClient httpClient, final Map<String, String> headers,
                                                            final String requestBody) throws IOException, InterruptedException {
        return MCPHttpTransportTestSupport.sendRawPostRequest(httpClient, getEndpointUri(), headers, requestBody);
    }
    
    protected final Map<String, Object> getStructuredContent(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.getStructuredContent(payload);
    }
    
    protected final Map<String, Object> getFirstResourcePayload(final String responseBody) {
        Map<String, Object> payload = MCPInteractionPayloads.parseJsonPayload(responseBody);
        return MCPInteractionPayloads.getFirstResourcePayload(payload);
    }
    
    protected final Map<String, Object> parseJsonBody(final String responseBody) {
        return MCPInteractionPayloads.parseJsonPayload(responseBody);
    }
    
    protected final Map<String, Object> castToMap(final Object value) {
        return MCPInteractionPayloads.castToMap(value);
    }
    
    protected final Map<String, Object> getRecoveryPayload(final Map<String, Object> payload, final String expectedRecoveryCategory) {
        assertThat(String.valueOf(payload.get("response_mode")), is("recovery"));
        Map<String, Object> result = castToMap(payload.get("recovery"));
        assertThat(String.valueOf(result.get("recovery_category")), is(expectedRecoveryCategory));
        return result;
    }
    
    protected final String createMaskRulePlan(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_mask_rule", Map.of(
                "database", "logic_db",
                "schema", "logic_db",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "KEEP_FIRST_N_LAST_M",
                "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("status")), is("planned"));
        return String.valueOf(payload.get("plan_id"));
    }
    
    protected final URI getEndpointUri() throws IOException {
        return getHttpEndpointUri();
    }
    
    protected final String getProtocolVersion() {
        return MCPHttpTransportTestSupport.PROTOCOL_VERSION;
    }
    
    protected final Map<String, String> createSessionHeaders(final String sessionId) {
        return MCPHttpTransportTestSupport.createSessionHeaders(sessionId, getProtocolVersion());
    }
    
    @Override
    protected final RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected final Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeDatabases;
    }
    
    @Override
    protected final void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(),
                () -> MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed MCP programmatic contract E2E test."));
        if (useSharedDatabaseBackedRuntime()) {
            prepareSharedDatabaseBackedRuntime();
            return;
        }
        applyRuntimeFixture(createRuntimeFixture());
    }
    
    private void prepareSharedDatabaseBackedRuntime() throws IOException {
        if (null == sharedRuntimeFixture) {
            sharedRuntimeFixture = createRuntimeFixture();
        }
        applyRuntimeFixture(sharedRuntimeFixture);
    }
    
    private ProgrammaticRuntimeFixture createRuntimeFixture() throws IOException {
        GenericContainer<?> result = MySQLRuntimeTestSupport.createContainer();
        boolean success = false;
        try {
            result.start();
            Map<String, RuntimeDatabaseConfiguration> actualRuntimeDatabases = MySQLRuntimeTestSupport.createPreparedProgrammaticRuntimeDatabases(result);
            success = true;
            return new ProgrammaticRuntimeFixture(result, actualRuntimeDatabases);
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize MCP E2E runtime databases.", ex);
        } finally {
            if (!success) {
                result.stop();
            }
        }
    }
    
    private void applyRuntimeFixture(final ProgrammaticRuntimeFixture fixture) {
        container = fixture.container();
        runtimeDatabases = fixture.runtimeDatabases();
    }
    
    protected boolean useSharedDatabaseBackedRuntime() {
        return false;
    }
    
    protected void closeSharedDatabaseBackedRuntime() {
        if (null != sharedRuntimeFixture) {
            sharedRuntimeFixture.close();
            sharedRuntimeFixture = null;
        }
    }
    
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ProgrammaticRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> container;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        private GenericContainer<?> container() {
            return container;
        }
        
        private Map<String, RuntimeDatabaseConfiguration> runtimeDatabases() {
            return runtimeDatabases;
        }
        
        @Override
        public void close() {
            container.stop();
        }
    }
}
