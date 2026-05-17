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

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Docker image STDIO MCP interaction client.
 */
@RequiredArgsConstructor
public final class DockerImageStdioInteractionClient extends AbstractProcessMCPStdioInteractionClient {
    
    private static final String CONTAINER_CONFIG_FILE = "/tmp/shardingsphere-mcp-e2e.yaml";
    
    private final String imageName;
    
    private final Path configFile;
    
    @Override
    protected ProcessBuilder createProcessBuilder() {
        List<String> command = new LinkedList<>();
        command.addAll(List.of("docker", "run", "--rm", "-i", "--add-host=host.docker.internal:host-gateway", "-e", "SHARDINGSPHERE_MCP_TRANSPORT=stdio"));
        if (null != configFile) {
            command.addAll(List.of("-v", configFile.toAbsolutePath().normalize() + ":" + CONTAINER_CONFIG_FILE + ":ro", "-e",
                    "SHARDINGSPHERE_MCP_CONFIG=" + CONTAINER_CONFIG_FILE));
        }
        command.add(imageName);
        return new ProcessBuilder(command);
    }
    
    @Override
    protected String getClientName() {
        return "mcp-e2e-container-stdio";
    }
}
