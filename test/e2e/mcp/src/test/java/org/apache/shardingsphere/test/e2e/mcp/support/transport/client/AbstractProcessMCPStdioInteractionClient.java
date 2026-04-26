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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * STDIO MCP interaction client backed by one child process.
 */
abstract class AbstractProcessMCPStdioInteractionClient extends AbstractMCPInteractionClient {
    
    private static final long PROCESS_STOP_TIMEOUT_SECONDS = 5L;
    
    private static final String INITIALIZE_REQUEST_ID = "init-1";
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final List<String> stdErrorMessages = new CopyOnWriteArrayList<>();
    
    private Process process;
    
    private Thread stdErrorCollector;
    
    private BufferedWriter writer;
    
    private BufferedReader reader;
    
    @Override
    public final void open() throws IOException {
        if (null != process) {
            return;
        }
        try {
            process = createProcessBuilder().start();
            stdErrorCollector = startStdErrorCollector(process, stdErrorMessages);
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            initializeSession();
        } catch (final IOException | IllegalStateException ex) {
            closeQuietly();
            throw ex;
        }
    }
    
    @Override
    public final void close() throws IOException {
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
    
    protected abstract ProcessBuilder createProcessBuilder() throws IOException;
    
    protected abstract String getClientName();
    
    @Override
    protected final void ensureOpened() {
        if (null == process) {
            throw new IllegalStateException("MCP session is not initialized.");
        }
    }
    
    @Override
    protected final Map<String, Object> sendRequest(final String requestId, final String method, final Map<String, Object> params) throws IOException {
        writeJsonRpcMessage(MCPInteractionProtocolSupport.createJsonRpcRequest(requestId, method, params));
        return readResponse(requestId);
    }
    
    private void initializeSession() throws IOException {
        Map<String, Object> initializeResponse = sendRequest(INITIALIZE_REQUEST_ID, "initialize",
                MCPInteractionProtocolSupport.createInitializeRequestParams(getClientName()));
        if (MCPInteractionPayloads.hasJsonRpcError(initializeResponse)) {
            throw new IllegalStateException("Failed to initialize STDIO MCP session: "
                    + MCPInteractionPayloads.getJsonRpcErrorPayload(initializeResponse).get("message")
                    + ". stderr: " + getStdErrorOutput());
        }
        notifyServer("notifications/initialized", Map.of());
    }
    
    private Thread startStdErrorCollector(final Process process, final List<String> stdErrorMessages) {
        Thread result = new Thread(() -> collectStdError(process, stdErrorMessages), getClientName() + "-stderr");
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
    
    private void notifyServer(final String method, final Map<String, Object> params) throws IOException {
        writeJsonRpcMessage(MCPInteractionProtocolSupport.createJsonRpcNotification(method, params));
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
