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

package org.apache.shardingsphere.agent.core.classloader;

import org.apache.shardingsphere.agent.core.path.AgentPath;
import org.apache.shardingsphere.agent.core.plugin.jar.PluginJar;
import org.apache.shardingsphere.agent.core.plugin.jar.PluginJarLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AgentClassLoaderTest {

    @Test
    public void assertClassNotFoundExceptionIsThrownWithEmptyPluginJarList() {
        AgentClassLoader agentClassLoader = new AgentClassLoader(String.class.getClassLoader(), Collections.emptyList());
        assertThrows(ClassNotFoundException.class, () -> agentClassLoader.findClass("java.lang.String"));
    }

    @Test
    public void assertCorrectClassIsReturned() throws IOException, ClassNotFoundException {
        File rootPath = AgentPath.getRootPath();
        Collection<PluginJar> pluginJars = PluginJarLoader.load(rootPath);
        AgentClassLoader agentClassLoader = new AgentClassLoader(String.class.getClassLoader(), pluginJars);
        System.out.println("Class Name is " + agentClassLoader.findClass("java.lang.String"));
    }

    @Test
    public void assertEmptyResourcesIsReturnedWhenPluginsJarListIsEmpty() {
        AgentClassLoader agentClassLoader = new AgentClassLoader(String.class.getClassLoader(), Collections.emptyList());
        assertThat(Collections.list(agentClassLoader.findResources("java.lang.String")), is(Collections.emptyList()));
    }

    @Test
    public void assertNullResourceIsReturnedWhenPluginsJarListIsEmpty() {
        AgentClassLoader agentClassLoader = new AgentClassLoader(String.class.getClassLoader(), Collections.emptyList());
        assertThat(agentClassLoader.findResource("java.lang.String"), nullValue());
    }
}
