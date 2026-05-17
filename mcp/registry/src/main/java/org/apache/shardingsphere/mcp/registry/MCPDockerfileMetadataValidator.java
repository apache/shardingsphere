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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MCPDockerfileMetadataValidator {
    
    private static final String SERVER_NAME_ARGUMENT_PREFIX = "ARG MCP_SERVER_NAME=";
    
    private static final String IMAGE_VERSION_ARGUMENT = "ARG MCP_IMAGE_VERSION=unknown";
    
    private static final String SERVER_NAME_LABEL = "io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\"";
    
    private static final String IMAGE_VERSION_LABEL = "org.opencontainers.image.version=\"${MCP_IMAGE_VERSION}\"";
    
    static void validate(final Path dockerfilePath, final String serverName) throws IOException {
        String dockerfile = Files.readString(dockerfilePath);
        ShardingSpherePreconditions.checkState(containsLine(dockerfile, SERVER_NAME_ARGUMENT_PREFIX + serverName),
                () -> new IllegalArgumentException("Dockerfile must define ARG MCP_SERVER_NAME=" + serverName + "."));
        ShardingSpherePreconditions.checkState(containsLine(dockerfile, IMAGE_VERSION_ARGUMENT),
                () -> new IllegalArgumentException("Dockerfile must define ARG MCP_IMAGE_VERSION=unknown."));
        ShardingSpherePreconditions.checkState(containsLabel(dockerfile, SERVER_NAME_LABEL),
                () -> new IllegalArgumentException("Dockerfile must label io.modelcontextprotocol.server.name with ${MCP_SERVER_NAME}."));
        ShardingSpherePreconditions.checkState(containsLabel(dockerfile, IMAGE_VERSION_LABEL),
                () -> new IllegalArgumentException("Dockerfile must label org.opencontainers.image.version with ${MCP_IMAGE_VERSION}."));
    }
    
    private static boolean containsLine(final String content, final String expectedLine) {
        return content.lines().map(String::trim).filter(MCPDockerfileMetadataValidator::isActiveLine).anyMatch(expectedLine::equals);
    }
    
    private static boolean containsLabel(final String content, final String expectedLabel) {
        boolean labelInstruction = false;
        for (String each : content.lines().map(String::trim).filter(MCPDockerfileMetadataValidator::isActiveLine).toList()) {
            labelInstruction = each.startsWith("LABEL ") || labelInstruction;
            if (labelInstruction && each.contains(expectedLabel)) {
                return true;
            }
            labelInstruction = labelInstruction && each.endsWith("\\");
        }
        return false;
    }
    
    private static boolean isActiveLine(final String line) {
        return !line.isBlank() && !line.startsWith("#");
    }
}
