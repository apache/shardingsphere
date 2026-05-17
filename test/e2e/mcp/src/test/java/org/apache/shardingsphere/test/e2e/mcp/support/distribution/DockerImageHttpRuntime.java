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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Docker image HTTP runtime support for MCP distribution E2E tests.
 */
public final class DockerImageHttpRuntime implements AutoCloseable {
    
    private static final String CONTAINER_CONFIG_FILE = "/tmp/shardingsphere-mcp-e2e.yaml";
    
    private static final long STARTUP_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    
    private static final long STARTUP_POLL_INTERVAL_MILLIS = 250L;
    
    private final String imageName;
    
    private final Path configFile;
    
    private final List<String> outputMessages = new CopyOnWriteArrayList<>();
    
    private Process process;
    
    private Thread outputCollector;
    
    private int httpPort;
    
    public DockerImageHttpRuntime(final String imageName, final Path configFile) {
        this.imageName = imageName;
        this.configFile = configFile;
    }
    
    /**
     * Open an HTTP interaction client after the Docker image becomes ready.
     *
     * @return opened interaction client
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    public MCPInteractionClient openInteractionClient() throws IOException, InterruptedException {
        startIfNeeded();
        long deadline = System.currentTimeMillis() + STARTUP_TIMEOUT_MILLIS;
        IllegalStateException lastException = null;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw createStartupFailure(lastException);
            }
            MCPHttpInteractionClient result = new MCPHttpInteractionClient(URI.create("http://127.0.0.1:" + httpPort + "/mcp"), HttpClient.newHttpClient());
            try {
                result.open();
                return result;
            } catch (final IOException | IllegalStateException ex) {
                lastException = new IllegalStateException("Docker MCP HTTP distribution is not ready yet.", ex);
                closeInteractionClientQuietly(result);
                long remainingMillis = deadline - System.currentTimeMillis();
                if (0L < remainingMillis) {
                    Thread.sleep(Math.min(STARTUP_POLL_INTERVAL_MILLIS, remainingMillis));
                }
            }
        }
        throw createStartupFailure(lastException);
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
            outputMessages.clear();
        }
    }
    
    private void startIfNeeded() throws IOException {
        if (null != process) {
            return;
        }
        httpPort = allocatePort();
        process = new ProcessBuilder(createDockerCommand(httpPort)).redirectErrorStream(true).start();
        outputCollector = startOutputCollector(process);
    }
    
    private List<String> createDockerCommand(final int httpPort) {
        List<String> result = new LinkedList<>();
        result.addAll(List.of("docker", "run", "--rm", "--add-host=host.docker.internal:host-gateway",
                "-p", "127.0.0.1:" + httpPort + ":18088",
                "-v", configFile.toAbsolutePath().normalize() + ":" + CONTAINER_CONFIG_FILE + ":ro",
                "-e", "SHARDINGSPHERE_MCP_TRANSPORT=http",
                "-e", "SHARDINGSPHERE_MCP_CONFIG=" + CONTAINER_CONFIG_FILE, imageName));
        return result;
    }
    
    private int allocatePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
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
    
    private IllegalStateException createStartupFailure(final IllegalStateException cause) {
        return new IllegalStateException("Docker MCP HTTP distribution failed to become ready. output: " + String.join(System.lineSeparator(), outputMessages), cause);
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
