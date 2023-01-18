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

import com.google.common.base.Ascii;
import org.apache.shardingsphere.agent.core.plugin.jar.PluginJar;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

public final class AgentClassLoaderTest {

    @Test
    public void assertNullPointerExceptionIsThrownIfClassNameIsNullAndPluginJarListIsEmpty() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(String.class.getClassLoader(), Collections.emptyList());
        assertThrows(NullPointerException.class, () -> agentClassLoader.findClass(null));
    }

    @Test
    public void assertClassNotFoundExceptionIsThrownWithEmptyPluginJarList() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), Collections.emptyList());
        assertThrows(ClassNotFoundException.class, () -> agentClassLoader.findClass(Ascii.class.getCanonicalName()));
    }

    @Test
    public void assertClassNotFoundExceptionIsThrownIfPluginJarListIsNull() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), null);
        assertThrows(ClassNotFoundException.class, () -> agentClassLoader.findClass(Ascii.class.getCanonicalName()));
    }

    @Test
    public void assertCorrectClassIsReturned() throws IOException, ClassNotFoundException, URISyntaxException {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), getPluginJars());
        assertThat(agentClassLoader.findClass(Ascii.class.getCanonicalName()).toString(), is(Ascii.class.toString()));
        assertThat(agentClassLoader.findClass(Mockito.class.getCanonicalName()).toString(), is(Mockito.class.toString()));
    }

    @Test
    public void assertEmptyResourcesListIsReturnedWhenNameIsNullAndPluginsJarListIsEmpty() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), Collections.emptyList());
        assertThat(Collections.list(agentClassLoader.findResources(null)), is(Collections.emptyList()));
    }

    @Test
    public void assertEmptyResourcesListIsReturnedWhenNameIsNullAndPluginsJarListIsNull() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), null);
        assertThat(Collections.list(agentClassLoader.findResources(null)), is(Collections.emptyList()));
    }

    @Test
    public void assertEmptyResourcesIsReturnedWhenPluginsJarListIsEmpty() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), Collections.emptyList());
        assertThat(Collections.list(agentClassLoader.findResources(Ascii.class.getCanonicalName())), is(Collections.emptyList()));
    }

    @Test
    public void assertCorrectResourcesListIsReturned() throws URISyntaxException, IOException {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), getPluginJars());

        final String asciiName = Ascii.class.getCanonicalName().replaceAll("\\.", "/") + ".class";
        final String mockitoName = Mockito.class.getCanonicalName().replaceAll("\\.", "/") + ".class";

        final List<URL> expectedAsciiResourceList = Collections.singletonList(new URL("jar:file:" + getJarFilePath(Ascii.class) + "!/" + asciiName));
        final List<URL> actualAsciiResourceList = Collections.list(agentClassLoader.findResources(asciiName));

        final List<URL> expectedMockitoResourceList = Collections.singletonList(new URL("jar:file:" + getJarFilePath(Mockito.class) + "!/" + mockitoName));
        final List<URL> actualMockitoResourceList = Collections.list(agentClassLoader.findResources(mockitoName));

        assertThat(actualAsciiResourceList.size(), is(1));
        assertThat(actualAsciiResourceList.get(0), is(expectedAsciiResourceList.get(0)));

        assertThat(actualMockitoResourceList.size(), is(1));
        assertThat(actualMockitoResourceList.get(0), is(expectedMockitoResourceList.get(0)));
    }

    @Test
    public void assertNullResourceIsReturnedWhenWhenNameIsNullAndPluginsJarListIsEmpty() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), Collections.emptyList());
        assertThat(agentClassLoader.findResource(null), nullValue());
    }

    @Test
    public void assertNullResourceIsReturnedWhenPluginsJarListIsEmpty() {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), Collections.emptyList());
        assertThat(agentClassLoader.findResource(Ascii.class.getCanonicalName()), nullValue());
    }

    @Test
    public void assertCorrectResourceIsReturned() throws URISyntaxException, IOException {
        final AgentClassLoader agentClassLoader = new AgentClassLoader(AgentClassLoaderTest.class.getClassLoader(), getPluginJars());
        final String asciiName = Ascii.class.getCanonicalName().replaceAll("\\.", "/") + ".class";

        final URL expectedAsciiResource = new URL("jar:file:" + getJarFilePath(Ascii.class) + "!/" + asciiName);
        final URL actualAsciiResource = agentClassLoader.findResource(asciiName);

        assertThat(actualAsciiResource, is(expectedAsciiResource));
    }

    private List<PluginJar> getPluginJars() throws URISyntaxException, IOException {
        final String guavaJarFilePath = getJarFilePath(Ascii.class);
        final String mockitoJarFilePath = getJarFilePath(Mockito.class);
        return Arrays.asList(
                new PluginJar(new JarFile(guavaJarFilePath), new File(guavaJarFilePath)),
                new PluginJar(new JarFile(mockitoJarFilePath), new File(mockitoJarFilePath))
        );
    }

    private String getJarFilePath(final Class clazz) throws URISyntaxException {
        final URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(url.toURI()).toString();
    }
}
