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

package org.apache.shardingsphere.mcp.registry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPDockerImageMetadataValidator {
    
    private static final String SERVER_NAME_ARGUMENT_PREFIX = "ARG MCP_SERVER_NAME=";
    
    private static final String IMAGE_VERSION_ARGUMENT = "ARG MCP_IMAGE_VERSION=unknown";
    
    private static final String SERVER_NAME_LABEL = "io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\"";
    
    private static final String IMAGE_VERSION_LABEL = "org.opencontainers.image.version=\"${MCP_IMAGE_VERSION}\"";
    
    static void validate(final Path dockerfilePath, final String serverName) throws IOException {
        String dockerfile = Files.readString(dockerfilePath);
        require(containsLine(dockerfile, SERVER_NAME_ARGUMENT_PREFIX + serverName), "Dockerfile must define ARG MCP_SERVER_NAME=" + serverName + ".");
        require(containsLine(dockerfile, IMAGE_VERSION_ARGUMENT), "Dockerfile must define ARG MCP_IMAGE_VERSION=unknown.");
        require(containsLabel(dockerfile, SERVER_NAME_LABEL), "Dockerfile must label io.modelcontextprotocol.server.name with ${MCP_SERVER_NAME}.");
        require(containsLabel(dockerfile, IMAGE_VERSION_LABEL), "Dockerfile must label org.opencontainers.image.version with ${MCP_IMAGE_VERSION}.");
    }
    
    private static boolean containsLine(final String content, final String expectedLine) {
        return content.lines().map(String::trim).filter(MCPDockerImageMetadataValidator::isActiveLine).anyMatch(expectedLine::equals);
    }
    
    private static boolean containsLabel(final String content, final String expectedLabel) {
        boolean labelInstruction = false;
        for (String each : content.lines().map(String::trim).filter(MCPDockerImageMetadataValidator::isActiveLine).toList()) {
            labelInstruction = each.startsWith("LABEL ") || labelInstruction;
            if (labelInstruction && each.contains(expectedLabel)) {
                return true;
            }
            labelInstruction = labelInstruction && each.endsWith("\\");
        }
        return false;
    }
    
    private static boolean isActiveLine(final String line) {
        return !line.startsWith("#");
    }
    
    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
