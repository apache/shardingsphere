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

package org.apache.shardingsphere.agent.core.plugin;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.spi.AgentServiceLoader;
import org.apache.shardingsphere.agent.spi.PluginLifecycleService;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.configuration.plugins.Plugins;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({AgentServiceLoader.class, Runtime.class})
class PluginLifecycleServiceManagerTest {
    
    @BeforeEach
    void resetStartedFlag() throws ReflectiveOperationException {
        AtomicBoolean startedFlag = (AtomicBoolean) Plugins.getMemberAccessor().get(PluginLifecycleServiceManager.class.getDeclaredField("STARTED_FLAG"), PluginLifecycleServiceManager.class);
        startedFlag.set(false);
    }
    
    @Test
    void assertInitStartsPluginAndClosesJars() throws IOException {
        Runtime runtime = mock(Runtime.class);
        when(Runtime.getRuntime()).thenReturn(runtime);
        PluginLifecycleService pluginLifecycleService = mock(PluginLifecycleService.class);
        when(pluginLifecycleService.getType()).thenReturn("test");
        mockAgentServiceLoader(pluginLifecycleService);
        Map<String, PluginConfiguration> pluginConfigs = Collections.singletonMap("test", new PluginConfiguration("localhost", 3307, "pwd", new Properties()));
        JarFile jarFile = mock(JarFile.class);
        JarFile errorJarFile = mock(JarFile.class);
        doThrow(IOException.class).when(errorJarFile).close();
        ArgumentCaptor<Thread> shutdownHookCaptor = ArgumentCaptor.forClass(Thread.class);
        ClassLoader expectedClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginClassLoader = new URLClassLoader(new URL[0], expectedClassLoader);
        PluginLifecycleServiceManager.init(pluginConfigs, Arrays.asList(jarFile, errorJarFile), pluginClassLoader, true);
        assertThat(Thread.currentThread().getContextClassLoader(), is(expectedClassLoader));
        verify(runtime).addShutdownHook(shutdownHookCaptor.capture());
        shutdownHookCaptor.getValue().run();
        verify(pluginLifecycleService).start(pluginConfigs.get("test"), true);
        verify(pluginLifecycleService).close();
        verify(jarFile).close();
        verify(errorJarFile).close();
    }
    
    @Test
    void assertInitWhenAlreadyStarted() throws ReflectiveOperationException {
        AtomicBoolean startedFlag = (AtomicBoolean) Plugins.getMemberAccessor().get(PluginLifecycleServiceManager.class.getDeclaredField("STARTED_FLAG"), PluginLifecycleServiceManager.class);
        startedFlag.set(true);
        AgentServiceLoader<PluginLifecycleService> serviceLoader = AgentServiceLoader.getServiceLoader(PluginLifecycleService.class);
        PluginLifecycleServiceManager.init(Collections.emptyMap(), Collections.emptyList(), new URLClassLoader(new URL[0]), true);
        assertTrue(startedFlag.get());
        verifyNoInteractions(serviceLoader);
    }
    
    @Test
    void assertInitWithoutMatchingService() {
        PluginLifecycleService pluginLifecycleService = mock(PluginLifecycleService.class);
        when(pluginLifecycleService.getType()).thenReturn("mismatch");
        mockAgentServiceLoader(pluginLifecycleService);
        PluginLifecycleServiceManager.init(Collections.singletonMap("test", new PluginConfiguration("127.0.0.1", 8080, "foo", new Properties())),
                Collections.emptyList(), new URLClassLoader(new URL[0]), false);
        verify(pluginLifecycleService, never()).start(any(PluginConfiguration.class), anyBoolean());
    }
    
    @Test
    void assertInitStartWithException() {
        PluginLifecycleService pluginLifecycleService = mock(PluginLifecycleService.class);
        when(pluginLifecycleService.getType()).thenReturn("test");
        doThrow(IllegalStateException.class).when(pluginLifecycleService).start(any(PluginConfiguration.class), anyBoolean());
        mockAgentServiceLoader(pluginLifecycleService);
        PluginConfiguration pluginConfig = new PluginConfiguration("0.0.0.0", 9000, "bar", new Properties());
        PluginLifecycleServiceManager.init(Collections.singletonMap("test", pluginConfig), Collections.emptyList(), new URLClassLoader(new URL[0]), true);
        verify(pluginLifecycleService).start(pluginConfig, true);
    }
    
    @SuppressWarnings("unchecked")
    private void mockAgentServiceLoader(final PluginLifecycleService pluginLifecycleService) {
        AgentServiceLoader<PluginLifecycleService> agentServiceLoader = mock(AgentServiceLoader.class);
        when(agentServiceLoader.getServices()).thenReturn(Collections.singleton(pluginLifecycleService));
        when(AgentServiceLoader.getServiceLoader(PluginLifecycleService.class)).thenReturn(agentServiceLoader);
    }
}
