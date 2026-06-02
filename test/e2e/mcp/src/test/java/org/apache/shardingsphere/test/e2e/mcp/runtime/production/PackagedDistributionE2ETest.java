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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.DockerImageHttpRuntime;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionHttpRuntime;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionPluginFixtureSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.DockerImageStdioInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.PackagedDistributionStdioInteractionClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs({OS.LINUX, OS.MAC, OS.WINDOWS})
@EnabledIf("isEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PackagedDistributionE2ETest {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String IMAGE_PROPERTY = "mcp.e2e.container.image";
    
    private static final List<String> EXPECTED_RUNTIME_ARTIFACT_IDS = List.of(
            "shardingsphere-mcp-bootstrap", "shardingsphere-mcp-feature-encrypt", "shardingsphere-mcp-feature-mask");
    
    private static final List<String> CORE_TOOL_NAMES = List.of("database_gateway_search_metadata", "database_gateway_validate_proxy_connectivity",
            "database_gateway_execute_query", "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow");
    
    private static final List<String> REMOVED_FEATURE_TOOL_NAMES = OfficialMCPToolNames.getAll().stream().filter(each -> !CORE_TOOL_NAMES.contains(each)).toList();
    
    private static final List<String> REMOVED_FEATURE_RESOURCE_URIS = List.of("shardingsphere://features/encrypt/algorithms", "shardingsphere://features/mask/algorithms");
    
    private static final String FIXTURE_RESOURCE_URI = "shardingsphere://features/test-fixture/status";
    
    @TempDir
    private Path tempDir;
    
    private GenericContainer<?> mysqlContainer;
    
    @AfterAll
    void tearDownContainer() {
        if (null != mysqlContainer) {
            mysqlContainer.stop();
            mysqlContainer = null;
        }
    }
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDistributionEnabled();
    }
    
    @Test
    void assertLaunchPackagedDistributionOverHttp() throws IOException, InterruptedException, SQLException {
        PreparedPackagedDistribution distribution = preparePackagedDistribution("http", RuntimeTransport.HTTP);
        try (
                PackagedDistributionHttpRuntime runtime = new PackagedDistributionHttpRuntime(distribution);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertOfficialRuntime(distribution.home(), RuntimeTransport.HTTP, interactionClient);
            assertMySQLMetadata(interactionClient);
            assertExecuteQuery(interactionClient);
        }
    }
    
    @Test
    void assertLaunchPackagedDistributionOverStdio() throws IOException, InterruptedException, SQLException {
        PreparedPackagedDistribution distribution = preparePackagedDistribution("stdio", RuntimeTransport.STDIO);
        try (MCPInteractionClient interactionClient = new PackagedDistributionStdioInteractionClient(distribution.home(), distribution.configFile())) {
            interactionClient.open();
            assertOfficialRuntime(distribution.home(), RuntimeTransport.STDIO, interactionClient);
            assertMySQLMetadata(interactionClient);
            assertExecuteQuery(interactionClient);
        }
    }
    
    @Test
    void assertDiscoverFixturePluginFromPluginsDirectory() throws IOException, InterruptedException, SQLException {
        PreparedPackagedDistribution distribution = preparePackagedDistribution("plugin-discovery", RuntimeTransport.STDIO);
        List<String> actualRemovedJarNames = PackagedDistributionPluginFixtureSupport.removeOfficialFeatureJars(distribution.home().resolve("lib"));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-encrypt")));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-mask")));
        Path actualFixturePluginJar = PackagedDistributionPluginFixtureSupport.createFixturePluginJar(distribution.home().resolve("plugins"));
        assertTrue(Files.isRegularFile(actualFixturePluginJar));
        try (MCPInteractionClient interactionClient = new PackagedDistributionStdioInteractionClient(distribution.home(), distribution.configFile())) {
            interactionClient.open();
            assertDiscoveredTools(interactionClient.listTools());
            assertDiscoveredResources(interactionClient.listResources());
            assertMySQLMetadata(interactionClient);
            assertFixtureTool(interactionClient.call("fixture_ping", Map.of("message", "hello")));
            assertFixtureResource(interactionClient.readResource(FIXTURE_RESOURCE_URI));
            assertCapabilities(interactionClient.readResource("shardingsphere://capabilities"));
        }
    }
    
    @Test
    void assertLaunchContainerOverHttp() throws IOException, InterruptedException, SQLException {
        assumeContainerImageConfigured();
        Path configFile = createDockerConfigurationFile(RuntimeTransport.HTTP);
        try (
                DockerImageHttpRuntime runtime = new DockerImageHttpRuntime(System.getProperty(IMAGE_PROPERTY), configFile);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertContainerRuntime(RuntimeTransport.HTTP, interactionClient);
        }
    }
    
    @Test
    void assertLaunchContainerOverStdio() throws IOException, InterruptedException, SQLException {
        assumeContainerImageConfigured();
        Path configFile = createDockerConfigurationFile(RuntimeTransport.STDIO);
        try (MCPInteractionClient interactionClient = new DockerImageStdioInteractionClient(System.getProperty(IMAGE_PROPERTY), configFile)) {
            interactionClient.open();
            assertContainerRuntime(RuntimeTransport.STDIO, interactionClient);
        }
    }
    
    private PreparedPackagedDistribution preparePackagedDistribution(final String caseName, final RuntimeTransport transport) throws IOException, SQLException {
        prepareMySQLContainer();
        return PackagedDistributionTestSupport.prepare(tempDir.resolve(caseName), transport,
                MySQLRuntimeTestSupport.createRuntimeDatabases(mysqlContainer, LOGICAL_DATABASE_NAME));
    }
    
    private Path createDockerConfigurationFile(final RuntimeTransport transport) throws IOException, SQLException {
        prepareMySQLContainer();
        return PackagedDistributionTestSupport.createDockerConfigurationFile(tempDir.resolve("container-" + transport.name().toLowerCase() + ".yaml"), transport,
                MySQLRuntimeTestSupport.createDockerHostRuntimeDatabases(mysqlContainer, LOGICAL_DATABASE_NAME));
    }
    
    private void prepareMySQLContainer() throws SQLException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(),
                () -> MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed MCP distribution E2E test."));
        if (null != mysqlContainer) {
            return;
        }
        mysqlContainer = MySQLRuntimeTestSupport.createContainer();
        mysqlContainer.start();
        MySQLRuntimeTestSupport.initializeDatabase(mysqlContainer);
    }
    
    private void assumeContainerImageConfigured() {
        Assumptions.assumeFalse(System.getProperty(IMAGE_PROPERTY, "").isBlank(), "Set -D" + IMAGE_PROPERTY + " to run MCP container distribution E2E.");
    }
    
    private void assertOfficialRuntime(final Path distributionHome, final RuntimeTransport transport,
                                       final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertBootstrapDirectoriesCreated(distributionHome);
        assertRemovedExtensionDirectoryAbsent(distributionHome);
        assertOfficialFeatureJarsPackaged(distributionHome);
        assertRuntimeDiagnostics(interactionClient.readResource("shardingsphere://runtime"), transport);
        assertDatabaseNames(interactionClient.readResource("shardingsphere://databases"));
        assertSupportedTools(interactionClient.readResource("shardingsphere://capabilities").get("supportedTools"));
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
    }
    
    private void assertContainerRuntime(final RuntimeTransport transport, final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertRuntimeDiagnostics(interactionClient.readResource("shardingsphere://runtime"), transport);
        assertDatabaseNames(interactionClient.readResource("shardingsphere://databases"));
        assertSupportedTools(interactionClient.readResource("shardingsphere://capabilities").get("supportedTools"));
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
        assertMySQLMetadata(interactionClient);
        assertExecuteQuery(interactionClient);
    }
    
    private void assertBootstrapDirectoriesCreated(final Path distributionHome) {
        assertTrue(Files.isDirectory(distributionHome.resolve("data")));
        assertTrue(Files.isDirectory(distributionHome.resolve("logs")));
        assertTrue(Files.isDirectory(distributionHome.resolve("plugins")));
    }
    
    private void assertRemovedExtensionDirectoryAbsent(final Path distributionHome) {
        assertFalse(Files.exists(distributionHome.resolve("ext-lib")));
    }
    
    private void assertRuntimeDiagnostics(final Map<String, Object> runtimeStatus, final RuntimeTransport transport) {
        assertThat(runtimeStatus.get("status"), is("available"));
        assertThat(runtimeStatus.get("active_transport"), is(getTransportName(transport)));
        Map<String, Object> actualRedactionSummary = MCPInteractionPayloads.castToMap(runtimeStatus.get("redaction_summary"));
        assertThat(actualRedactionSummary.get("marker"), is("******"));
        Map<String, Object> actualDiagnostics = MCPInteractionPayloads.castToMap(runtimeStatus.get("diagnostics"));
        assertThat(actualDiagnostics.get("current_category"), is("ready"));
        assertTrue(((List<?>) actualDiagnostics.get("safe_categories")).contains("invalid_configuration"));
        assertTrue(MCPInteractionPayloads.castToList(actualDiagnostics.get("operator_next_actions")).stream().anyMatch(each -> "invalid_configuration".equals(each.get("category"))));
        assertRuntimeStatusSecretSafe(runtimeStatus);
    }
    
    private String getTransportName(final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? "http" : "stdio";
    }
    
    private void assertRuntimeStatusSecretSafe(final Map<String, Object> runtimeStatus) {
        String actualPayload = String.valueOf(runtimeStatus);
        assertFalse(actualPayload.contains("Authorization: Bearer"));
        assertFalse(actualPayload.contains("runtime-secret"));
        assertFalse(actualPayload.contains("jdbc:"));
        assertFalse(actualPayload.contains("at org.apache."));
    }
    
    private void assertOfficialFeatureJarsPackaged(final Path distributionHome) throws IOException {
        try (Stream<Path> paths = Files.list(distributionHome.resolve("lib"))) {
            List<String> actualJarNames = paths.map(each -> each.getFileName().toString()).toList();
            for (String each : EXPECTED_RUNTIME_ARTIFACT_IDS) {
                assertTrue(actualJarNames.stream().anyMatch(actual -> actual.contains(each)));
            }
        }
    }
    
    private void assertDatabaseNames(final Map<String, Object> payload) {
        List<String> actualDatabaseNames = getPayloadItems(payload).stream().map(each -> String.valueOf(each.get("database"))).toList();
        assertThat(actualDatabaseNames, containsInAnyOrder(LOGICAL_DATABASE_NAME));
    }
    
    private void assertSupportedTools(final Object supportedTools) {
        assertOfficialToolNames(((List<?>) supportedTools).stream().map(String::valueOf).toList());
    }
    
    private void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    private void assertMySQLMetadata(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        List<String> actualSearchItems = getItemNames(interactionClient.call("database_gateway_search_metadata",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order", "object_types", List.of("table", "view"))));
        assertThat(actualSearchItems, hasItems("orders", "order_items", "active_orders"));
    }
    
    private void assertExecuteQuery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actualResult = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
        assertThat(String.valueOf(actualResult.get("result_kind")), is("result_set"));
    }
    
    private void assertDiscoveredTools(final List<Map<String, Object>> tools) {
        List<String> actualToolNames = tools.stream().map(each -> String.valueOf(each.get("name"))).toList();
        assertThat(actualToolNames, hasItems("database_gateway_search_metadata", "database_gateway_validate_proxy_connectivity", "database_gateway_execute_query",
                "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow", "fixture_ping"));
        for (String each : REMOVED_FEATURE_TOOL_NAMES) {
            assertFalse(actualToolNames.contains(each));
        }
    }
    
    private void assertDiscoveredResources(final Map<String, Object> payload) {
        List<Map<String, Object>> actualResources = MCPInteractionPayloads.castToList(payload.get("resources"));
        assertTrue(actualResources.stream().anyMatch(each -> FIXTURE_RESOURCE_URI.equals(each.get("uri"))));
    }
    
    private void assertFixtureTool(final Map<String, Object> payload) {
        assertThat(payload.get("status"), is("ready"));
        assertThat(payload.get("echo"), is("hello"));
    }
    
    private void assertFixtureResource(final Map<String, Object> payload) {
        List<Map<String, Object>> actualItems = MCPInteractionPayloads.castToList(payload.get("items"));
        assertThat(actualItems.size(), is(1));
        assertThat(actualItems.get(0).get("feature"), is("test-fixture"));
        assertThat(actualItems.get(0).get("status"), is("ready"));
    }
    
    private void assertCapabilities(final Map<String, Object> payload) {
        List<String> actualSupportedTools = ((List<?>) payload.get("supportedTools")).stream().map(String::valueOf).toList();
        assertThat(actualSupportedTools, hasItems("database_gateway_search_metadata", "database_gateway_validate_proxy_connectivity", "database_gateway_execute_query",
                "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow", "fixture_ping"));
        for (String each : REMOVED_FEATURE_TOOL_NAMES) {
            assertFalse(actualSupportedTools.contains(each));
        }
        List<String> actualSupportedResources = ((List<?>) payload.get("supportedResources")).stream().map(String::valueOf).toList();
        assertTrue(actualSupportedResources.contains(FIXTURE_RESOURCE_URI));
        for (String each : REMOVED_FEATURE_RESOURCE_URIS) {
            assertFalse(actualSupportedResources.contains(each));
        }
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return getPayloadItems(payload).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
    
    private List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items"));
    }
}
