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

package org.apache.shardingsphere.shadow.yaml.swapper;

import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class YamlRootRuleConfigurationsForYamlShadowRuleConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/shadow-rule.yaml");
        assertNotNull(url);
        YamlRootRuleConfigurations rootRuleConfigurations = YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigurations.getRules().size(), is(1));
        assertShadowRule((YamlShadowRuleConfiguration) rootRuleConfigurations.getRules().iterator().next());
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/shadow-rule.yaml");
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
        YamlRootRuleConfigurations rootRuleConfigurations = YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigurations.getRules().size(), is(1));
        assertShadowRule((YamlShadowRuleConfiguration) rootRuleConfigurations.getRules().iterator().next());
    }
    
    private void assertShadowRule(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getColumn(), is("shadow"));
        assertThat(actual.getSourceDataSourceNames().size(), is(actual.getShadowDataSourceNames().size()));
        assertThat(actual.getSourceDataSourceNames(), is(Arrays.asList("ds", "ds1")));
        assertThat(actual.getShadowDataSourceNames(), is(Arrays.asList("shadow_ds", "shadow_ds1")));
    }
}
