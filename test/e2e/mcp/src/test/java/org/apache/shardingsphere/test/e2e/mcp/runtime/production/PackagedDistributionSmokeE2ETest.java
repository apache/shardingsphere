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

import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.PackagedDistributionStdioInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs({OS.LINUX, OS.MAC})
@EnabledIf("isEnabled")
class PackagedDistributionSmokeE2ETest {
    
    private static final List<String> EXPECTED_RUNTIME_ARTIFACT_IDS = List.of(
            "shardingsphere-mcp-bootstrap", "shardingsphere-mcp-feature-encrypt", "shardingsphere-mcp-feature-mask");
    
    private static final long STARTUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(20L);
    
    private static final long PROCESS_STOP_TIMEOUT_SECONDS = 5L;
    
    @TempDir
    private Path tempDir;
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDistributionEnabled();
    }
    
    @Test
    void assertLaunchPackagedDistributionOverHttp() throws IOException, InterruptedException {
        PreparedPackagedDistribution distribution = PackagedDistributionTestSupport.prepare(tempDir.resolve("http"), RuntimeTransport.HTTP);
        try (
                PackagedDistributionHttpRuntime runtime = new PackagedDistributionHttpRuntime(distribution);
                MCPInteractionClient interactionClient = runtime.openInteractionClient()) {
            assertOfficialPackagedRuntime(distribution.home(), interactionClient);
            List<String> actualSearchItems = getItemNames(interactionClient.call("search_metadata",
                    Map.of("database", "orders", "query", "order", "object_types", List.of("TABLE", "VIEW"))));
            assertThat(actualSearchItems, hasItems("orders", "order_items", "active_orders"));
            Map<String, Object> actualResult = interactionClient.call("execute_query",
                    Map.of("database", "orders", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actualResult.get("result_kind")), is("result_set"));
        }
    }
    
    @Test
    void assertLaunchPackagedDistributionOverStdio() throws IOException, InterruptedException {
        PreparedPackagedDistribution distribution = PackagedDistributionTestSupport.prepare(tempDir.resolve("stdio"), RuntimeTransport.STDIO);
        try (MCPInteractionClient interactionClient = new PackagedDistributionStdioInteractionClient(distribution.home(), distribution.configFile())) {
            interactionClient.open();
            assertOfficialPackagedRuntime(distribution.home(), interactionClient);
            List<String> actualSearchItems = getItemNames(interactionClient.call("search_metadata",
                    Map.of("database", "orders", "query", "order", "object_types", List.of("TABLE"))));
            assertThat(actualSearchItems, hasItems("orders", "order_items"));
            Map<String, Object> actualResult = interactionClient.call("execute_query",
                    Map.of("database", "orders", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
            assertThat(String.valueOf(actualResult.get("result_kind")), is("result_set"));
        }
    }
    
    private void assertBootstrapDirectoriesCreated(final Path distributionHome) {
        assertTrue(Files.isDirectory(distributionHome.resolve("data")));
        assertTrue(Files.isDirectory(distributionHome.resolve("logs")));
        assertTrue(Files.isDirectory(distributionHome.resolve("plugins")));
    }
    
    private void assertLegacyExtensionDirectoryRemoved(final Path distributionHome) {
        assertFalse(Files.exists(distributionHome.resolve("ext-lib")));
    }
    
    private void assertOfficialPackagedRuntime(final Path distributionHome, final MCPInteractionClient interactionClient) throws IOException, InterruptedException {
        assertBootstrapDirectoriesCreated(distributionHome);
        assertLegacyExtensionDirectoryRemoved(distributionHome);
        assertOfficialFeatureJarsPackaged(distributionHome);
        assertDatabaseNames(interactionClient.readResource("shardingsphere://databases"), "orders", "billing");
        assertSupportedTools(interactionClient.readResource("shardingsphere://capabilities").get("supportedTools"));
        assertOfficialToolNames(interactionClient.listTools().stream().map(each -> String.valueOf(each.get("name"))).toList());
    }
    
    private void assertOfficialFeatureJarsPackaged(final Path distributionHome) throws IOException {
        try (Stream<Path> paths = Files.list(distributionHome.resolve("lib"))) {
            List<String> actualJarNames = paths.map(each -> each.getFileName().toString()).toList();
            for (String each : EXPECTED_RUNTIME_ARTIFACT_IDS) {
                assertTrue(actualJarNames.stream().anyMatch(actual -> actual.contains(each)));
            }
        }
    }
    
    private void assertDatabaseNames(final Map<String, Object> payload, final String... expectedDatabases) {
        List<String> actualDatabaseNames = getPayloadItems(payload).stream().map(each -> String.valueOf(each.get("database"))).toList();
        assertThat(actualDatabaseNames, containsInAnyOrder(expectedDatabases));
    }
    
    private void assertSupportedTools(final Object supportedTools) {
        assertOfficialToolNames(((List<?>) supportedTools).stream().map(String::valueOf).toList());
    }
    
    private void assertOfficialToolNames(final List<String> actualToolNames) {
        assertThat(actualToolNames, containsInAnyOrder(OfficialMCPToolNames.getAll().toArray()));
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return getPayloadItems(payload).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
    
    private List<Map<String, Object>> getPayloadItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items"));
    }
    
    private static final class PackagedDistributionHttpRuntime implements AutoCloseable {
        
        private final PreparedPackagedDistribution distribution;
        
        private final List<String> outputMessages = new CopyOnWriteArrayList<>();
        
        private Process process;
        
        private Thread outputCollector;
        
        private PackagedDistributionHttpRuntime(final PreparedPackagedDistribution distribution) {
            this.distribution = distribution;
        }
        
        private MCPInteractionClient openInteractionClient() throws IOException, InterruptedException {
            startProcessIfNeeded();
            long deadline = System.currentTimeMillis() + STARTUP_TIMEOUT_MILLIS;
            IllegalStateException lastException = null;
            while (System.currentTimeMillis() < deadline) {
                if (!process.isAlive()) {
                    throw createStartupFailure(lastException);
                }
                MCPHttpInteractionClient result = new MCPHttpInteractionClient(distribution.getEndpointUri(), HttpClient.newHttpClient());
                try {
                    result.open();
                    return result;
                } catch (final IOException | IllegalStateException ex) {
                    lastException = new IllegalStateException("Packaged MCP HTTP distribution is not ready yet.", ex);
                    closeInteractionClientQuietly(result);
                    Thread.sleep(250L);
                }
            }
            throw createStartupFailure(lastException);
        }
        
        private void startProcessIfNeeded() throws IOException {
            if (null != process) {
                return;
            }
            ProcessBuilder processBuilder = new ProcessBuilder(distribution.getStartScript().toString(), distribution.configFile().toString());
            processBuilder.directory(distribution.home().toFile());
            processBuilder.environment().put("JAVA_HOME", System.getProperty("java.home"));
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            outputCollector = startOutputCollector(process, outputMessages);
        }
        
        private Thread startOutputCollector(final Process process, final List<String> outputMessages) {
            Thread result = new Thread(() -> collectOutput(process, outputMessages), "mcp-packaged-http-smoke");
            result.setDaemon(true);
            result.start();
            return result;
        }
        
        private void collectOutput(final Process process, final List<String> outputMessages) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (null != (line = reader.readLine())) {
                    outputMessages.add(line);
                }
            } catch (final IOException ignored) {
            }
        }
        
        private IllegalStateException createStartupFailure(final IllegalStateException cause) {
            return new IllegalStateException("Packaged MCP HTTP distribution failed to become ready. output: "
                    + String.join(System.lineSeparator(), outputMessages), cause);
        }
        
        @Override
        public void close() {
            if (null == process) {
                return;
            }
            process.destroy();
            try {
                if (!process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
                if (null != outputCollector) {
                    outputCollector.join(TimeUnit.SECONDS.toMillis(PROCESS_STOP_TIMEOUT_SECONDS));
                }
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void closeInteractionClientQuietly(final MCPInteractionClient interactionClient) {
            try {
                interactionClient.close();
            } catch (final IOException ignored) {
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
