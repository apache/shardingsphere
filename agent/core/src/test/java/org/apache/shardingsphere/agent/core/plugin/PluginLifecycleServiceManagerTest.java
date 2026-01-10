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

import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PluginLifecycleServiceManagerTest {
    
    @Test
    void assertInitPluginLifecycleService() {
        assertDoesNotThrow(() -> PluginLifecycleServiceManager.init(Collections.emptyMap(), Collections.emptyList(), new MultipleParentClassLoader(Collections.emptyList()), true));
    }
    
    @Test
    void assertInitPluginLifecycleServiceWithMap() {
        Map<String, PluginConfiguration> pluginConfigs = Collections.singletonMap("Key", new PluginConfiguration("localhost", 8080, "random", new Properties()));
        assertDoesNotThrow(() -> PluginLifecycleServiceManager.init(pluginConfigs, Collections.emptyList(), new MultipleParentClassLoader(Collections.emptyList()), true));
    }
    
    @Test
    void assertInitPluginLifecycleServiceWithMockHandler() throws MalformedURLException {
        URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri().toURL()},
                new MultipleParentClassLoader(Collections.emptyList()), urlStreamHandlerFactory);
        PluginLifecycleServiceManager.init(Collections.emptyMap(), Collections.emptyList(), urlClassLoader, true);
        verify(urlStreamHandlerFactory).createURLStreamHandler(anyString());
    }
}
