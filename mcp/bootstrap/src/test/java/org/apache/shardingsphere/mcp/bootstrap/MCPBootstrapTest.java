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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.OAuthIntrospectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MCPBootstrapTest {
    
    @Test
    void assertMainCloseServerWhenShutdownHookRuns() throws IOException {
        assertMain(new String[0], "conf/mcp-http.yaml", false);
    }
    
    @Test
    void assertMainCloseServerOnceWhenShutdownHookRunsRepeatedly() throws IOException {
        assertMain(new String[]{"stdio.yaml"}, "stdio.yaml", true);
    }
    
    @Test
    void assertMainTrimConfigurationPathArgument() throws IOException {
        assertMain(new String[]{"  stdio.yaml  "}, "stdio.yaml", false);
    }
    
    @Test
    void assertMainUseDefaultConfigurationPathForBlankArgument() throws IOException {
        assertMain(new String[]{"   "}, "conf/mcp-http.yaml", false);
    }
    
    private void assertMain(final String[] args, final String expectedConfigPath, final boolean runShutdownHookTwice) throws IOException {
        MCPLaunchConfiguration launchConfig = createLaunchConfiguration();
        MCPRuntimeServer runtimeServer = mock(MCPRuntimeServer.class);
        Runtime runtime = mock(Runtime.class);
        AtomicReference<Thread> shutdownHook = new AtomicReference<>();
        try (
                MockedStatic<MCPConfigurationLoader> mockedConfigurationLoader = mockStatic(MCPConfigurationLoader.class);
                MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
                MockedConstruction<MCPRuntimeLauncher> mockedConstruction = mockConstruction(MCPRuntimeLauncher.class,
                        (mock, context) -> when(mock.launch(launchConfig)).thenReturn(runtimeServer))) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
            mockedConfigurationLoader.when(() -> MCPConfigurationLoader.load(expectedConfigPath)).thenReturn(launchConfig);
            doAnswer(invocation -> {
                shutdownHook.set(invocation.getArgument(0, Thread.class));
                return null;
            }).when(runtime).addShutdownHook(any(Thread.class));
            MCPBootstrap.main(args);
            MCPRuntimeLauncher actualLauncher = mockedConstruction.constructed().get(0);
            verify(actualLauncher).launch(launchConfig);
            Thread actualShutdownHook = shutdownHook.get();
            actualShutdownHook.run();
            if (runShutdownHookTwice) {
                actualShutdownHook.run();
            }
        }
        verify(runtimeServer).stop();
        verifyNoMoreInteractions(runtimeServer);
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration() {
        return new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true,
                        "127.0.0.1", false, "", 18080, "/mcp", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "", new OAuthIntrospectionConfiguration()),
                new StdioTransportConfiguration(false), Collections.emptyMap());
    }
}
