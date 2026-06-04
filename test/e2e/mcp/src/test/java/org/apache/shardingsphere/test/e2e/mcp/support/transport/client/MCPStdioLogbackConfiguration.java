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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * STDIO logback configuration support for MCP E2E clients.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPStdioLogbackConfiguration {
    
    private static final String CONTENT = """
            <configuration>
                <appender name="STDERR" class="ch.qos.logback.core.ConsoleAppender">
                    <target>System.err</target>
                    <encoder>
                        <pattern>%msg%n</pattern>
                    </encoder>
                </appender>
                <root level="WARN">
                    <appender-ref ref="STDERR" />
                </root>
            </configuration>
            """;
    
    /**
     * Create logback configuration next to the MCP runtime configuration file.
     *
     * @param configFile MCP runtime configuration file
     * @param fileName logback configuration file name
     * @return created logback configuration file
     * @throws IOException I/O exception
     */
    public static Path createForConfig(final Path configFile, final String fileName) throws IOException {
        return Files.writeString(configFile.resolveSibling(fileName), CONTENT);
    }
}
