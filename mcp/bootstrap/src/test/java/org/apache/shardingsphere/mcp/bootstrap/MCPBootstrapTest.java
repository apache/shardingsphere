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
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MCPBootstrapTest {
    
    @Test
    void assertMainWithDefaultConfigPath() throws IOException {
        MCPLaunchConfiguration launchConfig = createLaunchConfiguration(false);
        MCPRuntimeServer runtimeServer = mock(MCPRuntimeServer.class);
        Runtime runtime = mock(Runtime.class);
        try (
                MockedStatic<MCPConfigurationLoader> mockedConfigurationLoader = mockStatic(MCPConfigurationLoader.class);
                MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
                MockedConstruction<MCPRuntimeLauncher> mockedConstruction = mockConstruction(MCPRuntimeLauncher.class,
                        (mock, context) -> when(mock.launch(launchConfig)).thenReturn(runtimeServer))) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
            mockedConfigurationLoader.when(() -> MCPConfigurationLoader.load("conf/mcp.yaml")).thenReturn(launchConfig);
            MCPBootstrap.main(new String[0]);
            assertThat(mockedConstruction.constructed().size(), is(1));
            MCPRuntimeLauncher actualLauncher = mockedConstruction.constructed().iterator().next();
            verify(actualLauncher).launch(launchConfig);
        }
        verify(runtime).addShutdownHook(any(Thread.class));
        verifyNoInteractions(runtimeServer);
    }
    
    @Test
    void assertMainWithTrimmedConfigPath() throws IOException {
        MCPLaunchConfiguration launchConfig = createLaunchConfiguration(false);
        MCPRuntimeServer runtimeServer = mock(MCPRuntimeServer.class);
        Runtime runtime = mock(Runtime.class);
        try (
                MockedStatic<MCPConfigurationLoader> mockedConfigurationLoader = mockStatic(MCPConfigurationLoader.class);
                MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
                MockedConstruction<MCPRuntimeLauncher> mockedConstruction = mockConstruction(MCPRuntimeLauncher.class,
                        (mock, context) -> when(mock.launch(launchConfig)).thenReturn(runtimeServer))) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
            mockedConfigurationLoader.when(() -> MCPConfigurationLoader.load("custom.yaml")).thenReturn(launchConfig);
            MCPBootstrap.main(new String[]{"  custom.yaml  "});
            assertThat(mockedConstruction.constructed().size(), is(1));
            MCPRuntimeLauncher actualLauncher = mockedConstruction.constructed().iterator().next();
            verify(actualLauncher).launch(launchConfig);
        }
        verify(runtime).addShutdownHook(any(Thread.class));
        verifyNoInteractions(runtimeServer);
    }
    
    @Test
    void assertMainCloseServerWhenShutdownHookRuns() throws IOException {
        MCPLaunchConfiguration launchConfig = createLaunchConfiguration(true);
        MCPRuntimeServer runtimeServer = mock(MCPRuntimeServer.class);
        Runtime runtime = mock(Runtime.class);
        AtomicReference<Thread> shutdownHook = new AtomicReference<>();
        try (
                MockedStatic<MCPConfigurationLoader> mockedConfigurationLoader = mockStatic(MCPConfigurationLoader.class);
                MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
                MockedConstruction<MCPRuntimeLauncher> mockedConstruction = mockConstruction(MCPRuntimeLauncher.class,
                        (mock, context) -> when(mock.launch(launchConfig)).thenReturn(runtimeServer))) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
            mockedConfigurationLoader.when(() -> MCPConfigurationLoader.load("conf/mcp.yaml")).thenReturn(launchConfig);
            doAnswer(invocation -> {
                shutdownHook.set(invocation.getArgument(0, Thread.class));
                return null;
            }).when(runtime).addShutdownHook(any(Thread.class));
            MCPBootstrap.main(new String[0]);
            assertThat(mockedConstruction.constructed().size(), is(1));
            MCPRuntimeLauncher actualLauncher = mockedConstruction.constructed().iterator().next();
            verify(actualLauncher).launch(launchConfig);
            shutdownHook.get().run();
        }
        verify(runtimeServer).stop();
        verifyNoMoreInteractions(runtimeServer);
    }
    
    @Test
    void assertMainCloseServerOnceWhenShutdownHookRunsRepeatedly() throws IOException {
        MCPLaunchConfiguration launchConfig = createLaunchConfiguration(true);
        MCPRuntimeServer runtimeServer = mock(MCPRuntimeServer.class);
        Runtime runtime = mock(Runtime.class);
        AtomicReference<Thread> shutdownHook = new AtomicReference<>();
        try (
                MockedStatic<MCPConfigurationLoader> mockedConfigurationLoader = mockStatic(MCPConfigurationLoader.class);
                MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
                MockedConstruction<MCPRuntimeLauncher> mockedConstruction = mockConstruction(MCPRuntimeLauncher.class,
                        (mock, context) -> when(mock.launch(launchConfig)).thenReturn(runtimeServer))) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
            mockedConfigurationLoader.when(() -> MCPConfigurationLoader.load("stdio.yaml")).thenReturn(launchConfig);
            doAnswer(invocation -> {
                shutdownHook.set(invocation.getArgument(0, Thread.class));
                return null;
            }).when(runtime).addShutdownHook(any(Thread.class));
            MCPBootstrap.main(new String[]{"stdio.yaml"});
            assertThat(mockedConstruction.constructed().size(), is(1));
            MCPRuntimeLauncher actualLauncher = mockedConstruction.constructed().iterator().next();
            verify(actualLauncher).launch(launchConfig);
            shutdownHook.get().run();
            shutdownHook.get().run();
        }
        verify(runtimeServer).stop();
        verifyNoMoreInteractions(runtimeServer);
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean stdioEnabled) {
        return new MCPLaunchConfiguration(new HttpTransportConfiguration(!stdioEnabled, "127.0.0.1", false, 18080, "/mcp"), new StdioTransportConfiguration(stdioEnabled), Collections.emptyMap());
    }
}
