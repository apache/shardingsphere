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

package org.apache.shardingsphere.agent.core.plugin.classloader;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ClassLoaderContextTest {
    
    @AfterEach
    void cleanAgentClassLoaders() {
        getAgentClassLoaders().clear();
    }
    
    @Test
    void assertCreatePluginClassLoaderWhenAbsent() {
        ClassLoader appClassLoader = new URLClassLoader(new URL[0], null);
        AgentPluginClassLoader actual = new ClassLoaderContext(appClassLoader, Collections.emptyList()).getPluginClassLoader();
        Map<ClassLoader, AgentPluginClassLoader> agentClassLoaders = getAgentClassLoaders();
        assertThat(agentClassLoaders.size(), is(1));
        assertThat(actual, is(agentClassLoaders.get(appClassLoader)));
    }
    
    @Test
    void assertReuseExistingPluginClassLoader() {
        ClassLoader appClassLoader = new URLClassLoader(new URL[0], null);
        AgentPluginClassLoader expected = new AgentPluginClassLoader(appClassLoader, Collections.emptyList());
        Map<ClassLoader, AgentPluginClassLoader> agentClassLoaders = getAgentClassLoaders();
        agentClassLoaders.put(appClassLoader, expected);
        AgentPluginClassLoader actual = new ClassLoaderContext(appClassLoader, Collections.emptyList()).getPluginClassLoader();
        assertThat(actual, is(expected));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<ClassLoader, AgentPluginClassLoader> getAgentClassLoaders() {
        Field agentClassLoaders = ClassLoaderContext.class.getDeclaredField("AGENT_CLASS_LOADERS");
        agentClassLoaders.setAccessible(true);
        return (Map<ClassLoader, AgentPluginClassLoader>) agentClassLoaders.get(null);
    }
}
