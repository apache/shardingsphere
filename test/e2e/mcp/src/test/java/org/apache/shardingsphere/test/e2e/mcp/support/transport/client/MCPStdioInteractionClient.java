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

import org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * STDIO MCP interaction client backed by one child process.
 */
@SuppressWarnings("UseOfProcessBuilder")
public final class MCPStdioInteractionClient extends AbstractProcessMCPStdioInteractionClient {
    
    private static final String CLIENT_NAME = "mcp-e2e-stdio";
    
    private static final String LOGBACK_CONFIG_FILE_NAME = "mcp-e2e-stdio-logback.xml";
    
    private final Path configFile;
    
    public MCPStdioInteractionClient(final Path configFile) {
        this.configFile = configFile;
    }
    
    @Override
    protected ProcessBuilder createProcessBuilder() throws IOException {
        Path logbackConfigFile = createLogbackConfigurationFile();
        return new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dlogback.configurationFile=" + logbackConfigFile, "-cp", System.getProperty("java.class.path"), MCPBootstrap.class.getName(), configFile.toString());
    }
    
    @Override
    protected String getClientName() {
        return CLIENT_NAME;
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
}
