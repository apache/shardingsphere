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

package org.apache.shardingsphere.mcp.bootstrap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.lifecycle.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MCP bootstrap entrance.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPBootstrap {
    
    private static final String DEFAULT_CONFIG_PATH = "conf/mcp.yaml";
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException when the runtime configuration cannot be loaded
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
        // CHECKSTYLE:ON
        MCPLaunchConfiguration<?> launchConfig = MCPConfigurationLoader.load(getConfigurationPath(args));
        AtomicReference<MCPRuntimeTransport> runtimeTransportReference = new AtomicReference<>(new MCPRuntimeLauncher().launch(launchConfig));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeRuntimeTransport(runtimeTransportReference), "shardingsphere-mcp-shutdown"));
        if (launchConfig.getTransport().getStdio().isEnabled()) {
            awaitRuntimeTransportTermination(runtimeTransportReference);
        }
    }
    
    private static String getConfigurationPath(final String[] args) {
        if (0 == args.length || null == args[0]) {
            return DEFAULT_CONFIG_PATH;
        }
        String result = args[0].trim();
        return result.isEmpty() ? DEFAULT_CONFIG_PATH : result;
    }
    
    private static void awaitRuntimeTransportTermination(final AtomicReference<MCPRuntimeTransport> runtimeTransportReference) {
        MCPRuntimeTransport runtimeTransport = runtimeTransportReference.get();
        if (null == runtimeTransport) {
            return;
        }
        try {
            runtimeTransport.awaitTermination();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            closeRuntimeTransport(runtimeTransportReference);
        }
    }
    
    private static void closeRuntimeTransport(final AtomicReference<MCPRuntimeTransport> runtimeTransportReference) {
        MCPRuntimeTransport runtimeTransport = runtimeTransportReference.getAndSet(null);
        if (null != runtimeTransport) {
            runtimeTransport.close();
        }
    }
}
