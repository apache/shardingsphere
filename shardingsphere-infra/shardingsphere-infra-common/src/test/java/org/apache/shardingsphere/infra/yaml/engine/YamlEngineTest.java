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

package org.apache.shardingsphere.infra.yaml.engine;

import org.apache.shardingsphere.infra.yaml.swapper.fixture.FixtureYamlRuleConfiguration;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class YamlEngineTest {
    
    @Test
    public void assertUnmarshalWithFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/fixture-rule.yaml");
        assertNotNull(url);
        FixtureYamlRuleConfiguration actual = YamlEngine.unmarshal(new File(url.getFile()), FixtureYamlRuleConfiguration.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/fixture-rule.yaml");
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append("\n");
            }
        }
        FixtureYamlRuleConfiguration actual = YamlEngine.unmarshal(yamlContent.toString().getBytes(), FixtureYamlRuleConfiguration.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    public void assertUnmarshalWithYamlContentClassType() {
        FixtureYamlRuleConfiguration actual = YamlEngine.unmarshal("name: test", FixtureYamlRuleConfiguration.class);
        assertThat(actual.getName(), is("test"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertUnmarshalWithYamlContent() {
        Map<String, Object> actual = (Map<String, Object>) YamlEngine.unmarshal("name: test");
        assertThat(actual.get("name").toString(), is("test"));
    }
    
    @Test
    public void assertUnmarshalProperties() {
        Properties actual = YamlEngine.unmarshalProperties("password: pwd\nauthorizedSchemas: db1");
        assertThat(actual.getProperty("authorizedSchemas"), is("db1"));
        assertThat(actual.getProperty("password"), is("pwd"));
    }
    
    @Test
    public void assertMarshal() {
        FixtureYamlRuleConfiguration actual = new FixtureYamlRuleConfiguration();
        actual.setName("test");
        assertThat(YamlEngine.marshal(actual), is("name: test\n"));
    }
}
