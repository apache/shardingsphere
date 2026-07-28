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

import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.DockerImageHttpRuntime;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionHttpRuntime;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionPluginFixtureSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.PostgreSQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.DockerImageStdioInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.PackagedDistributionStdioInteractionClient;
import org.junit.jupiter.api.AfterAll;
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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs({OS.LINUX, OS.MAC, OS.WINDOWS})
@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PackagedDistributionE2ETest {
    
    private static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    private static final String IMAGE_PROPERTY = "mcp.e2e.container.image";
    
    private static final List<String> EXPECTED_RUNTIME_ARTIFACT_IDS = List.of(
            "shardingsphere-mcp-bootstrap", "shardingsphere-mcp-feature-encrypt", "shardingsphere-mcp-feature-mask",
            "shardingsphere-mcp-feature-broadcast", "shardingsphere-mcp-feature-readwrite-splitting", "shardingsphere-mcp-feature-shadow",
            "shardingsphere-mcp-feature-sharding");
    
    private static final List<String> CORE_TOOL_NAMES = List.of("database_gateway_search_metadata", "database_gateway_validate_runtime_database",
            "database_gateway_execute_query", "database_gateway_execute_explain_query", "database_gateway_execute_update",
            "database_gateway_apply_workflow", "database_gateway_validate_workflow");
    
    private static final List<String> REMOVED_FEATURE_TOOL_NAMES = OfficialMCPToolNames.getAll().stream().filter(each -> !CORE_TOOL_NAMES.contains(each)).toList();
    
    private static final String FIXTURE_RESOURCE_URI = "shardingsphere://features/test-fixture/status";
    
    @TempDir
    private Path tempDir;
    
    private GenericContainer<?> mysqlContainer;
    
    private GenericContainer<?> postgresqlContainer;
    
    @AfterAll
    void tearDownContainer() {
        if (null != mysqlContainer) {
            mysqlContainer.stop();
            mysqlContainer = null;
        }
        if (null != postgresqlContainer) {
            postgresqlContainer.stop();
            postgresqlContainer = null;
        }
    }
    
    @Test
    void assertLaunchPackagedDistributionOverHttp() throws IOException, InterruptedException, SQLException {
        PreparedPackagedDistribution distribution = prepareReusableOfficialPackagedDistribution(RuntimeTransport.HTTP);
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
        PreparedPackagedDistribution distribution = prepareReusableOfficialPackagedDistribution(RuntimeTransport.STDIO);
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
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-broadcast")));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-readwrite-splitting")));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-shadow")));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-sharding")));
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
        Path configFile = createDockerConfigurationFile(RuntimeTransport.HTTP);
        try (
                DockerImageHttpRuntime runtime = new DockerImageHttpRuntime(getConfiguredContainerImage(), configFile);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertContainerRuntime(RuntimeTransport.HTTP, interactionClient);
        }
    }
    
    @Test
    void assertLaunchContainerOverStdio() throws IOException, InterruptedException, SQLException {
        Path configFile = createDockerConfigurationFile(RuntimeTransport.STDIO);
        try (MCPInteractionClient interactionClient = new DockerImageStdioInteractionClient(getConfiguredContainerImage(), configFile)) {
            interactionClient.open();
            assertContainerRuntime(RuntimeTransport.STDIO, interactionClient);
        }
    }
    
    @Test
    void assertLaunchContainerWithDefaultHttpConfiguration() throws IOException, InterruptedException {
        try (
                DockerImageHttpRuntime runtime = new DockerImageHttpRuntime(getConfiguredContainerImage(), null);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertDefaultContainerRuntime(RuntimeTransport.HTTP, interactionClient);
        }
    }
    
    @Test
    void assertLaunchContainerWithDefaultStdioConfiguration() throws IOException, InterruptedException {
        try (MCPInteractionClient interactionClient = new DockerImageStdioInteractionClient(getConfiguredContainerImage(), null)) {
            interactionClient.open();
            assertDefaultContainerRuntime(RuntimeTransport.STDIO, interactionClient);
        }
    }
    
    @Test
    void assertLaunchPackagedDistributionWithPostgreSQL() throws IOException, InterruptedException, SQLException {
        preparePostgreSQLContainer();
        PreparedPackagedDistribution distribution = PackagedDistributionTestSupport.prepare(tempDir.resolve("postgresql-http"), RuntimeTransport.HTTP,
                PostgreSQLRuntimeTestSupport.createRuntimeDatabases(postgresqlContainer, LOGICAL_DATABASE_NAME));
        try (
                PackagedDistributionHttpRuntime runtime = new PackagedDistributionHttpRuntime(distribution);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertOfficialRuntime(distribution.home(), RuntimeTransport.HTTP, interactionClient);
            assertPostgreSQLMetadata(interactionClient);
        }
    }
    
    private PreparedPackagedDistribution preparePackagedDistribution(final String caseName, final RuntimeTransport transport) throws IOException, SQLException {
        prepareMySQLContainer();
        return PackagedDistributionTestSupport.prepare(tempDir.resolve(caseName), transport,
                MySQLRuntimeTestSupport.createRuntimeDatabases(mysqlContainer, LOGICAL_DATABASE_NAME));
    }
    
    private PreparedPackagedDistribution prepareReusableOfficialPackagedDistribution(final RuntimeTransport transport) throws IOException, SQLException {
        prepareMySQLContainer();
        return PackagedDistributionTestSupport.prepareReusable(tempDir.resolve("official-distribution-home-" + transport.name().toLowerCase(Locale.ENGLISH)), transport,
                MySQLRuntimeTestSupport.createRuntimeDatabases(mysqlContainer, LOGICAL_DATABASE_NAME));
    }
    
    private Path createDockerConfigurationFile(final RuntimeTransport transport) throws IOException, SQLException {
        prepareMySQLContainer();
        return PackagedDistributionTestSupport.createDockerConfigurationFile(tempDir.resolve("container-" + transport.name().toLowerCase(Locale.ENGLISH) + ".yaml"), transport,
                MySQLRuntimeTestSupport.createDockerHostRuntimeDatabases(mysqlContainer, LOGICAL_DATABASE_NAME));
    }
    
    private void prepareMySQLContainer() throws SQLException {
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException(MySQLRuntimeTestSupport.createDockerRequiredMessage("Docker is required for the MySQL-backed MCP distribution E2E test."));
        }
        if (null != mysqlContainer) {
            return;
        }
        mysqlContainer = MySQLRuntimeTestSupport.createContainer();
        mysqlContainer.start();
        MySQLRuntimeTestSupport.initializeDatabase(mysqlContainer);
    }
    
    private void preparePostgreSQLContainer() throws SQLException {
        if (!PostgreSQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException("Docker is required for the PostgreSQL-backed MCP distribution E2E test.");
        }
        if (null != postgresqlContainer) {
            return;
        }
        postgresqlContainer = PostgreSQLRuntimeTestSupport.createContainer();
        postgresqlContainer.start();
        PostgreSQLRuntimeTestSupport.initializeDatabase(postgresqlContainer);
    }
    
    private String getConfiguredContainerImage() {
        String result = EnvironmentPropertiesLoader.loadProperties().getProperty(IMAGE_PROPERTY, "").trim();
        if (result.isBlank()) {
            throw new IllegalStateException("Set " + IMAGE_PROPERTY + " in env/e2e-env.properties or pass -D" + IMAGE_PROPERTY + " to run MCP container distribution E2E.");
        }
        return result;
    }
    
    private void assertOfficialRuntime(final Path distributionHome, final RuntimeTransport transport,
                                       final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertBootstrapDirectoriesCreated(distributionHome);
        assertRemovedExtensionDirectoryAbsent(distributionHome);
        assertOfficialFeatureJarsPackaged(distributionHome);
        assertRuntimeDiagnostics(interactionClient.readResource("shardingsphere://runtime"), transport, "available", "ready");
        assertDatabaseNames(interactionClient.readResource("shardingsphere://databases"));
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
    }
    
    private void assertContainerRuntime(final RuntimeTransport transport, final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertRuntimeDiagnostics(interactionClient.readResource("shardingsphere://runtime"), transport, "available", "ready");
        assertDatabaseNames(interactionClient.readResource("shardingsphere://databases"));
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
        assertMySQLMetadata(interactionClient);
        assertExecuteQuery(interactionClient);
    }
    
    private void assertDefaultContainerRuntime(final RuntimeTransport transport, final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertRuntimeDiagnostics(interactionClient.readResource("shardingsphere://runtime"), transport, "configuration_required", "invalid_configuration");
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
    }
    
    private void assertBootstrapDirectoriesCreated(final Path distributionHome) {
        assertTrue(Files.isDirectory(distributionHome.resolve("data")));
        assertTrue(Files.isDirectory(distributionHome.resolve("logs")));
        assertTrue(Files.isDirectory(distributionHome.resolve("plugins")));
    }
    
    private void assertRemovedExtensionDirectoryAbsent(final Path distributionHome) {
        assertFalse(Files.exists(distributionHome.resolve("ext-lib")));
    }
    
    private void assertRuntimeDiagnostics(final Map<String, Object> runtimeStatus, final RuntimeTransport transport, final String expectedStatus, final String expectedCategory) {
        assertThat(runtimeStatus.get("status"), is(expectedStatus));
        assertThat(runtimeStatus.get("active_transport"), is(getTransportName(transport)));
        Map<String, Object> actualDiagnostics = MCPInteractionPayloads.getRequiredObject(runtimeStatus, "diagnostics");
        assertThat(actualDiagnostics.get("current_category"), is(expectedCategory));
        assertTrue(((List<?>) actualDiagnostics.get("safe_categories")).contains("invalid_configuration"));
        assertTrue(MCPInteractionPayloads.getRequiredObjectList(actualDiagnostics, "operator_next_actions").stream()
                .anyMatch(each -> "invalid_configuration".equals(each.get("category"))));
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
    
    private void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    private void assertMySQLMetadata(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        List<String> actualSearchItems = getItemNames(interactionClient.call("database_gateway_search_metadata",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "query", "order", "object_types", List.of("table", "view"))));
        assertThat(actualSearchItems, hasItems("orders", "order_items", "active_orders"));
    }
    
    private void assertPostgreSQLMetadata(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        List<String> actualSearchItems = getItemNames(interactionClient.call("database_gateway_search_metadata",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", "public", "query", "order", "object_types", List.of("table", "view"))));
        assertThat(actualSearchItems, hasItems("orders", "active_orders"));
    }
    
    private void assertExecuteQuery(final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        Map<String, Object> actualResult = interactionClient.call("database_gateway_execute_query",
                Map.of("database", LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME, "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
        assertThat(String.valueOf(actualResult.get("result_kind")), is("result_set"));
    }
    
    private void assertDiscoveredTools(final List<Map<String, Object>> tools) {
        List<String> actualToolNames = tools.stream().map(each -> String.valueOf(each.get("name"))).toList();
        assertThat(actualToolNames, hasItems("database_gateway_search_metadata", "database_gateway_validate_runtime_database", "database_gateway_execute_query",
                "database_gateway_execute_update", "database_gateway_apply_workflow", "database_gateway_validate_workflow", "fixture_ping"));
        for (String each : REMOVED_FEATURE_TOOL_NAMES) {
            assertFalse(actualToolNames.contains(each));
        }
    }
    
    private void assertDiscoveredResources(final Map<String, Object> payload) {
        List<Map<String, Object>> actualResources = MCPInteractionPayloads.getRequiredObjectList(payload, "resources");
        assertTrue(actualResources.stream().anyMatch(each -> FIXTURE_RESOURCE_URI.equals(each.get("uri"))));
    }
    
    private void assertFixtureTool(final Map<String, Object> payload) {
        assertThat(payload.get("status"), is("ready"));
        assertThat(payload.get("echo"), is("hello"));
    }
    
    private void assertFixtureResource(final Map<String, Object> payload) {
        List<Map<String, Object>> actualItems = MCPInteractionPayloads.getRequiredObjectList(payload, "items");
        assertThat(actualItems.size(), is(1));
        assertThat(actualItems.getFirst().get("feature"), is("test-fixture"));
        assertThat(actualItems.getFirst().get("status"), is("ready"));
    }
    
    private void assertCapabilities(final Map<String, Object> payload) {
        assertFalse(((Collection<?>) payload.get("supportedStatementClasses")).isEmpty());
        assertFalse(((List<?>) payload.get("completionTargets")).isEmpty());
        assertFalse(((List<?>) payload.get("resourceNavigation")).isEmpty());
        assertFalse(payload.containsKey("supportedTools"));
        assertFalse(payload.containsKey("supportedResources"));
        assertFalse(payload.containsKey("protocolAvailability"));
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return getPayloadItems(payload).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
    
    private List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.getRequiredObjectList(payload, "items");
    }
}
