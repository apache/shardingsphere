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

package org.apache.shardingsphere.test.e2e.mcp.support.transport.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * STDIO MCP interaction client backed by one child process.
 */
public final class MCPStdioInteractionClient implements MCPInteractionClient {

    private static final long PROCESS_STOP_TIMEOUT_SECONDS = 5L;

    private static final String INITIALIZE_REQUEST_ID = "init-1";

    private static final String CLIENT_NAME = "mcp-e2e-stdio";

    private static final String LOGBACK_CONFIG_FILE_NAME = "mcp-e2e-stdio-logback.xml";

    private static final String STDERR_COLLECTOR_THREAD_NAME = "mcp-e2e-stdio-stderr";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Path configFile;

    private final List<String> stdErrorMessages = new CopyOnWriteArrayList<>();

    private Process process;

    private Thread stdErrorCollector;

    private BufferedWriter writer;

    private BufferedReader reader;

    public MCPStdioInteractionClient(final Path configFile) {
        this.configFile = configFile;
    }

    @Override
    public void open() throws IOException {
        if (null != process) {
            return;
        }
        try {
            startProcess();
            initializeSession();
        } catch (final IOException | IllegalStateException ex) {
            closeQuietly();
            throw ex;
        }
    }

    @Override
    public Map<String, Object> call(final String actionName, final Map<String, Object> arguments) throws IOException {
        ensureOpened();
        Map<String, Object> response = sendRequest(actionName + "-1", "tools/call", Map.of("name", actionName, "arguments", arguments));
        return MCPInteractionPayloads.getStructuredContent(response);
    }

    @Override
    public Map<String, Object> listResources() throws IOException {
        ensureOpened();
        Map<String, Object> response = sendRequest("resources-list-1", "resources/list", Map.of());
        return MCPInteractionPayloads.getListResourcesPayload(response);
    }

    @Override
    public Map<String, Object> readResource(final String resourceUri) throws IOException {
        ensureOpened();
        Map<String, Object> response = sendRequest("resources-read-1", "resources/read", Map.of("uri", resourceUri));
        return MCPInteractionPayloads.getFirstResourcePayload(response);
    }

    @Override
    public void close() throws IOException {
        if (null == process) {
            return;
        }
        try {
            writer.close();
            waitForNormalExit();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("STDIO MCP process was interrupted during shutdown.", ex);
        } finally {
            closeQuietly();
        }
    }

    private ProcessBuilder createProcessBuilder(final Path logbackConfigFile) {
        return new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dlogback.configurationFile=" + logbackConfigFile, "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString());
    }

    private void startProcess() throws IOException {
        Path logbackConfigFile = createLogbackConfigurationFile();
        process = createProcessBuilder(logbackConfigFile).start();
        stdErrorCollector = startStdErrorCollector(process, stdErrorMessages);
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    private void initializeSession() throws IOException {
        Map<String, Object> initializeResponse = sendRequest(INITIALIZE_REQUEST_ID, "initialize", createInitializeRequestParams());
        if (MCPInteractionPayloads.hasJsonRpcError(initializeResponse)) {
            throw new IllegalStateException("Failed to initialize STDIO MCP session: "
                    + MCPInteractionPayloads.getJsonRpcErrorPayload(initializeResponse).get("message")
                    + ". stderr: " + getStdErrorOutput());
        }
        notifyServer("notifications/initialized", Map.of());
    }

    private Path createLogbackConfigurationFile() throws IOException {
        Path result = configFile.resolveSibling(LOGBACK_CONFIG_FILE_NAME);
        Files.writeString(result, "<configuration>\n"
                + "    <appender name=\"STDERR\" class=\"ch.qos.logback.core.ConsoleAppender\">\n"
                + "        <target>System.err</target>\n"
                + "        <encoder>\n"
                + "            <pattern>%msg%n</pattern>\n"
                + "        </encoder>\n"
                + "    </appender>\n"
                + "    <root level=\"WARN\">\n"
                + "        <appender-ref ref=\"STDERR\" />\n"
                + "    </root>\n"
                + "</configuration>\n");
        return result;
    }

    private Thread startStdErrorCollector(final Process process, final List<String> stdErrorMessages) {
        Thread result = new Thread(() -> collectStdError(process, stdErrorMessages), STDERR_COLLECTOR_THREAD_NAME);
        result.setDaemon(true);
        result.start();
        return result;
    }

    private void collectStdError(final Process process, final List<String> stdErrorMessages) {
        try (BufferedReader actualReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while (null != (line = actualReader.readLine())) {
                stdErrorMessages.add(line);
            }
        } catch (final IOException ignored) {
        }
    }

    private Map<String, Object> sendRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException {
        writeJsonRpcMessage(createJsonRpcRequest(requestId, method, params));
        return readResponse(requestId);
    }

    private void notifyServer(final String method, final Map<String, Object> params) throws IOException {
        writeJsonRpcMessage(createJsonRpcNotification(method, params));
    }

    private void writeJsonRpcMessage(final Map<String, Object> payload) throws IOException {
        writer.write(OBJECT_MAPPER.writeValueAsString(payload));
        writer.newLine();
        writer.flush();
    }

    private Map<String, Object> readResponse(final String requestId) throws IOException {
        String line;
        while (null != (line = reader.readLine())) {
            if (line.isBlank()) {
                continue;
            }
            Map<String, Object> result = OBJECT_MAPPER.readValue(line, new TypeReference<>() {
            });
            if (requestId.equals(String.valueOf(result.get("id")))) {
                return result;
            }
        }
        throw createRuntimeFailureException("STDIO MCP runtime did not return a response. stderr: " + getStdErrorOutput());
    }

    private void waitForNormalExit() throws InterruptedException {
        if (!process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            destroyProcess();
            throw createRuntimeFailureException("STDIO MCP process did not exit after stdin closed. stderr: " + getStdErrorOutput());
        }
        if (0 != process.exitValue()) {
            throw createRuntimeFailureException("STDIO MCP process exited with code " + process.exitValue() + ". stderr: " + getStdErrorOutput());
        }
    }

    private void destroyProcess() throws InterruptedException {
        if (!process.isAlive()) {
            return;
        }
        process.destroy();
        if (process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            return;
        }
        process.destroyForcibly();
        process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private String getStdErrorOutput() {
        return String.join(System.lineSeparator(), stdErrorMessages);
    }

    private IllegalStateException createRuntimeFailureException(final String defaultMessage) {
        String actualMessage = getRuntimeFailureMessage();
        return new IllegalStateException(null == actualMessage ? defaultMessage : actualMessage);
    }

    private String getRuntimeFailureMessage() {
        return stdErrorMessages.stream().filter(each -> each.startsWith("Exception in thread "))
                .map(this::extractFailureMessage).findFirst().orElse(null);
    }

    private String extractFailureMessage(final String errorLine) {
        int separatorIndex = errorLine.lastIndexOf(": ");
        return -1 == separatorIndex ? errorLine : errorLine.substring(separatorIndex + 2);
    }

    private Map<String, Object> createInitializeRequestParams() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("protocolVersion", MCPTransportConstants.PROTOCOL_VERSION);
        result.put("capabilities", Map.of());
        result.put("clientInfo", Map.of("name", CLIENT_NAME, "version", "1.0.0"));
        return result;
    }

    private Map<String, Object> createJsonRpcRequest(final String requestId, final String method, final Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("jsonrpc", "2.0");
        result.put("id", requestId);
        result.put("method", method);
        result.put("params", params);
        return result;
    }

    private Map<String, Object> createJsonRpcNotification(final String method, final Map<String, Object> params) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("jsonrpc", "2.0");
        result.put("method", method);
        result.put("params", params);
        return result;
    }

    private void ensureOpened() {
        if (null == process) {
            throw new IllegalStateException("MCP session is not initialized.");
        }
    }

    private void closeQuietly() {
        closeReaderQuietly();
        closeWriterQuietly();
        destroyProcessQuietly();
        if (null != stdErrorCollector) {
            try {
                stdErrorCollector.join(TimeUnit.SECONDS.toMillis(PROCESS_STOP_TIMEOUT_SECONDS));
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        process = null;
        reader = null;
        writer = null;
        stdErrorCollector = null;
        stdErrorMessages.clear();
    }

    private void closeReaderQuietly() {
        try {
            if (null != reader) {
                reader.close();
            }
        } catch (final IOException ignored) {
        }
    }

    private void closeWriterQuietly() {
        try {
            if (null != writer) {
                writer.close();
            }
        } catch (final IOException ignored) {
        }
    }

    private void destroyProcessQuietly() {
        if (null == process || !process.isAlive()) {
            return;
        }
        process.destroy();
        try {
            if (process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return;
            }
            process.destroyForcibly();
            process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
