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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ReadinessProbe.ReadinessResult;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Docker image HTTP runtime support for MCP distribution E2E tests.
 */
@RequiredArgsConstructor
public final class DockerImageHttpRuntime implements AutoCloseable {
    
    private static final String CONTAINER_CONFIG_FILE = "/tmp/shardingsphere-mcp-e2e.yaml";
    
    private static final long STARTUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    
    private static final long STARTUP_POLL_INTERVAL_MILLIS = 250L;
    
    private static final long PORT_QUERY_TIMEOUT_SECONDS = 5L;
    
    private final String imageName;
    
    private final Path configFile;
    
    private final List<String> outputMessages = new CopyOnWriteArrayList<>();
    
    private Process process;
    
    private Thread outputCollector;
    
    private int httpPort;
    
    private String containerName;
    
    /**
     * Open an HTTP interaction client after the Docker image becomes ready.
     *
     * @return opened interaction client
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    public MCPInteractionClient openInteractionClient() throws IOException, InterruptedException {
        startIfNeeded();
        return new ReadinessProbe(STARTUP_TIMEOUT_MILLIS, STARTUP_POLL_INTERVAL_MILLIS, STARTUP_POLL_INTERVAL_MILLIS)
                .waitUntilReady(this::openInteractionClientIfReady, this::createStartupFailure);
    }
    
    @Override
    public void close() {
        if (null == process) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(5L, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5L, TimeUnit.SECONDS);
            }
            if (null != outputCollector) {
                outputCollector.join(TimeUnit.SECONDS.toMillis(5L));
            }
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            process = null;
            outputCollector = null;
            httpPort = 0;
            containerName = null;
            outputMessages.clear();
        }
    }
    
    private void startIfNeeded() throws IOException {
        if (null != process) {
            return;
        }
        containerName = "shardingsphere-mcp-e2e-" + UUID.randomUUID();
        try {
            process = new ProcessBuilder(createDockerCommand(imageName, configFile, containerName)).redirectErrorStream(true).start();
            outputCollector = startOutputCollector(process);
        } catch (final IOException ex) {
            containerName = null;
            throw ex;
        }
    }
    
    static List<String> createDockerCommand(final String imageName, final Path configFile, final String containerName) {
        List<String> result = new LinkedList<>(List.of("docker", "run", "--rm", "--name", containerName,
                "--add-host=host.docker.internal:host-gateway", "-p", "127.0.0.1::18088", "-e", "SHARDINGSPHERE_MCP_TRANSPORT=http"));
        if (null != configFile) {
            result.addAll(List.of("-v", configFile.toAbsolutePath().normalize() + ":" + CONTAINER_CONFIG_FILE + ":ro", "-e",
                    "SHARDINGSPHERE_MCP_CONFIG=" + CONTAINER_CONFIG_FILE));
        }
        result.add(imageName);
        return result;
    }
    
    private Thread startOutputCollector(final Process process) {
        Thread result = new Thread(() -> collectOutput(process), "mcp-container-http-e2e");
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    private void collectOutput(final Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (null != (line = reader.readLine())) {
                outputMessages.add(line);
            }
        } catch (final IOException ignored) {
        }
    }
    
    private ReadinessResult<MCPInteractionClient> openInteractionClientIfReady() throws InterruptedException {
        if (!process.isAlive()) {
            return ReadinessResult.failed(null);
        }
        if (0 == httpPort) {
            try {
                httpPort = queryPublishedPort();
            } catch (final IOException ex) {
                return ReadinessResult.retry(new IllegalStateException("Docker MCP HTTP published port is not available yet.", ex));
            }
            if (0 == httpPort) {
                return ReadinessResult.retry(new IllegalStateException("Docker MCP HTTP published port has not been assigned yet."));
            }
        }
        MCPHttpInteractionClient result = new MCPHttpInteractionClient(URI.create("http://127.0.0.1:" + httpPort + "/mcp"), HttpClient.newHttpClient());
        try {
            result.open();
            return ReadinessResult.ready(result);
        } catch (final IOException | IllegalStateException ex) {
            closeInteractionClientQuietly(result);
            return ReadinessResult.retry(new IllegalStateException("Docker MCP HTTP distribution is not ready yet.", ex));
        }
    }
    
    private int queryPublishedPort() throws IOException, InterruptedException {
        Process portQuery = new ProcessBuilder("docker", "port", containerName, "18088/tcp").redirectErrorStream(true).start();
        try {
            if (!portQuery.waitFor(PORT_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS) || 0 != portQuery.exitValue()) {
                return 0;
            }
            return parsePublishedPort(new String(portQuery.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).orElse(0);
        } finally {
            if (portQuery.isAlive()) {
                portQuery.destroyForcibly();
            }
        }
    }
    
    static OptionalInt parsePublishedPort(final String output) {
        for (String each : output.lines().toList()) {
            int separatorIndex = each.lastIndexOf(':');
            if (separatorIndex < 0 || separatorIndex == each.length() - 1) {
                continue;
            }
            try {
                int result = Integer.parseInt(each.substring(separatorIndex + 1).trim());
                if (result > 0 && result <= 65535) {
                    return OptionalInt.of(result);
                }
            } catch (final NumberFormatException ignored) {
            }
        }
        return OptionalInt.empty();
    }
    
    private IllegalStateException createStartupFailure(final Exception cause, final int ignoredAttemptCount, final long ignoredElapsedMillis) {
        return new IllegalStateException("Docker MCP HTTP distribution failed to become ready. output: " + ProcessOutputDiagnostics.format(outputMessages), cause);
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
