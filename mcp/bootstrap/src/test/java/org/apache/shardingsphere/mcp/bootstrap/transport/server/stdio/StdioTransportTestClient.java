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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * STDIO MCP test client backed by one child process.
 */
final class StdioTransportTestClient implements AutoCloseable {
    
    private static final long PROCESS_STOP_TIMEOUT_SECONDS = 5L;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final List<String> stdErrorMessages = new CopyOnWriteArrayList<>();
    
    private final Process process;
    
    private final Thread stdErrorCollector;
    
    private final BufferedWriter writer;
    
    private final BufferedReader reader;
    
    private boolean closed;
    
    StdioTransportTestClient(final Path configFile) throws IOException {
        process = createProcessBuilder(configFile, createLogbackConfigurationFile(configFile)).start();
        stdErrorCollector = startStdErrorCollector(process, stdErrorMessages);
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }
    
    Map<String, Object> request(final String requestId, final String method, final Map<String, Object> params) throws IOException {
        writer.write(objectMapper.writeValueAsString(Map.of("jsonrpc", "2.0", "id", requestId, "method", method, "params", params)));
        writer.newLine();
        writer.flush();
        return getResult(readResponse(requestId));
    }
    
    void notifyServer(final String method, final Map<String, Object> params) throws IOException {
        writer.write(objectMapper.writeValueAsString(Map.of("jsonrpc", "2.0", "method", method, "params", params)));
        writer.newLine();
        writer.flush();
    }
    
    String getStdErrorOutput() {
        return String.join(System.lineSeparator(), stdErrorMessages);
    }
    
    @Override
    public void close() throws Exception {
        if (closed) {
            return;
        }
        closed = true;
        try {
            writer.close();
            waitForNormalExit();
        } finally {
            try {
                reader.close();
            } finally {
                stdErrorCollector.join(TimeUnit.SECONDS.toMillis(PROCESS_STOP_TIMEOUT_SECONDS));
            }
        }
    }
    
    private ProcessBuilder createProcessBuilder(final Path configFile, final Path logbackConfigFile) {
        return new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dlogback.configurationFile=" + logbackConfigFile, "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString());
    }
    
    private Path createLogbackConfigurationFile(final Path configFile) throws IOException {
        Path result = configFile.resolveSibling("logback-stdio-test.xml");
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
        Thread result = new Thread(() -> collectStdError(process, stdErrorMessages), "mcp-stdio-stderr");
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
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> readResponse(final String requestId) throws IOException {
        String line;
        while (null != (line = reader.readLine())) {
            if (line.isBlank()) {
                continue;
            }
            Map<String, Object> result = objectMapper.readValue(line, Map.class);
            Object actualId = result.get("id");
            if (requestId.equals(String.valueOf(actualId))) {
                return result;
            }
        }
        throw new AssertionError("STDIO bootstrap failed. No response for request `" + requestId + "`. stderr: " + getStdErrorOutput());
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getResult(final Map<String, Object> response) {
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        if (null != error) {
            throw new AssertionError("STDIO bootstrap failed. response error: " + error + ", stderr: " + getStdErrorOutput());
        }
        return (Map<String, Object>) response.get("result");
    }
    
    private void waitForNormalExit() throws InterruptedException {
        if (!process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            destroyProcess();
            throw new AssertionError("STDIO bootstrap process did not exit after stdin closed. stderr: " + getStdErrorOutput());
        }
        if (0 != process.exitValue()) {
            throw new AssertionError("STDIO bootstrap process exited with code " + process.exitValue() + ". stderr: " + getStdErrorOutput());
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
}
