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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.management.loading.PrivateMLet;

import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.junit.Test;

public final class PluginLifecycleServiceManagerTest {
    
    @Test
    public void assertInitPluginLifecycleService() {
        Map<String, PluginConfiguration> pluginConfigs = new HashMap<>();
        Collection<JarFile> pluginJars = new LinkedList<>();
        PluginLifecycleServiceManager.init(pluginConfigs, pluginJars, new MultipleParentClassLoader(new LinkedList<>()),
                true);
    }
    
    @Test
    public void assertInitPluginLifecycleServiceWithMap() {
        Map<String, PluginConfiguration> stringPluginConfigurationMap = new HashMap<>();
        stringPluginConfigurationMap.put("Key", new PluginConfiguration("localhost", 8080, "random", new Properties()));
        Collection<JarFile> pluginJars = new LinkedList<>();
        PluginLifecycleServiceManager.init(stringPluginConfigurationMap, pluginJars,
                new MultipleParentClassLoader(new LinkedList<>()), true);
    }
    
    @Test
    public void assertInitPluginLifecycleServiceWithMockHandler() throws MalformedURLException {
        Map<String, PluginConfiguration> pluginConfigs = new HashMap<>();
        Collection<JarFile> pluginJars = new LinkedList<>();
        URLStreamHandlerFactory urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
        when(urlStreamHandlerFactory.createURLStreamHandler((String) any())).thenReturn(null);
        PluginLifecycleServiceManager.init(pluginConfigs, pluginJars,
                new PrivateMLet(new URL[]{Paths.get(System.getProperty("java.io.tmpdir"), "test.txt").toUri().toURL()},
                        new MultipleParentClassLoader(new LinkedList<>()), urlStreamHandlerFactory, true),
                true);
        verify(urlStreamHandlerFactory).createURLStreamHandler((String) any());
    }
}
