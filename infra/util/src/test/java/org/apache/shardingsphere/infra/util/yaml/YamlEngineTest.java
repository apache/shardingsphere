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

package org.apache.shardingsphere.infra.util.yaml;

import org.apache.shardingsphere.infra.util.yaml.fixture.shortcuts.YamlShortcutsConfigurationFixture;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.composer.ComposerException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlEngineTest {
    
    private static final String LINE_SEPARATOR = System.lineSeparator();
    
    @Test
    void assertUnmarshalWithFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/shortcuts-fixture.yaml");
        assertNotNull(url);
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal(new File(url.getFile()), YamlShortcutsConfigurationFixture.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    void assertUnmarshalWithEmptyFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/empty-config.yaml");
        assertNotNull(url);
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal(new File(url.getFile()), YamlShortcutsConfigurationFixture.class);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertUnmarshalWithYamlBytes() throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource("yaml/shortcuts-fixture.yaml");
        assertNotNull(url);
        String yamlContent = String.join(System.lineSeparator(), Files.readAllLines(Paths.get(url.toURI())));
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal(yamlContent.getBytes(), YamlShortcutsConfigurationFixture.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    void assertUnmarshalWithEmptyYamlBytes() throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource("yaml/empty-config.yaml");
        assertNotNull(url);
        String yamlContent = String.join(System.lineSeparator(), Files.readAllLines(Paths.get(url.toURI())));
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal(yamlContent.getBytes(), YamlShortcutsConfigurationFixture.class);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertUnmarshalWithYamlContentClassType() {
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal("name: test", YamlShortcutsConfigurationFixture.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    void assertUnmarshalWithYamlContentClassTypeSkipMissingProperties() {
        YamlShortcutsConfigurationFixture actual = YamlEngine.unmarshal("name: test" + LINE_SEPARATOR + "notExistsField: test", YamlShortcutsConfigurationFixture.class, true);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    void assertUnmarshalProperties() {
        Properties actual = YamlEngine.unmarshal("password: pwd", Properties.class);
        assertThat(actual.getProperty("password"), is("pwd"));
    }
    
    @Test
    void assertUnmarshalWithEmptyProperties() {
        Properties actual = YamlEngine.unmarshal("", Properties.class);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertMarshal() {
        YamlShortcutsConfigurationFixture actual = new YamlShortcutsConfigurationFixture();
        actual.setName("test");
        assertThat(YamlEngine.marshal(actual), is("name: test" + System.lineSeparator()));
    }
    
    @Test
    void assertUnmarshalInvalidYaml() throws IOException, URISyntaxException {
        URL url = getClass().getClassLoader().getResource("yaml/accepted-class.yaml");
        assertNotNull(url);
        String yamlContent = String.join(System.lineSeparator(), Files.readAllLines(Paths.get(url.toURI())));
        assertThrows(ComposerException.class, () -> YamlEngine.unmarshal(yamlContent, Object.class));
    }
    
    @Test
    void assertMarshalCollection() {
        YamlShortcutsConfigurationFixture actual = new YamlShortcutsConfigurationFixture();
        actual.setName("test");
        YamlShortcutsConfigurationFixture actualAnother = new YamlShortcutsConfigurationFixture();
        actualAnother.setName("test");
        String expected = "- !FIXTURE" + System.lineSeparator() + "  name: test" + System.lineSeparator() + "- !FIXTURE" + System.lineSeparator() + "  name: test" + System.lineSeparator();
        assertThat(YamlEngine.marshal(Arrays.asList(actual, actualAnother)), is(expected));
    }
}
