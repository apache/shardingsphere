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

package org.apache.shardingsphere.readwritesplitting.swapper;

import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
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

public final class YamlRootRuleConfigurationsForYamlReadwriteSplittingRuleConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/readwrite-splitting-rule.yaml");
        assertNotNull(url);
        YamlRootRuleConfigurations rootRuleConfigs = YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigs.getRules().size(), is(1));
        assertReadwriteSplittingRule((YamlReadwriteSplittingRuleConfiguration) rootRuleConfigs.getRules().iterator().next());
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/readwrite-splitting-rule.yaml");
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append(System.lineSeparator());
            }
        }
        YamlRootRuleConfigurations rootRuleConfigs = YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootRuleConfigurations.class);
        assertThat(rootRuleConfigs.getRules().size(), is(1));
        assertReadwriteSplittingRule((YamlReadwriteSplittingRuleConfiguration) rootRuleConfigs.getRules().iterator().next());
    }
    
    private void assertReadwriteSplittingRule(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(2));
        assertReadwriteSplittingRuleForDs0(actual);
        assertReadwriteSplittingRuleForDs1(actual);
    }
    
    private void assertReadwriteSplittingRuleForDs0(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSources().get("ds_0").getWriteDataSourceName(), is("write_ds_0"));
        assertThat(actual.getDataSources().get("ds_0").getReadDataSourceNames(), is(Arrays.asList("write_ds_0_read_0", "write_ds_0_read_1")));
        assertThat(actual.getDataSources().get("ds_0").getLoadBalancerName(), is("roundRobin"));
    }
    
    private void assertReadwriteSplittingRuleForDs1(final YamlReadwriteSplittingRuleConfiguration actual) {
        assertThat(actual.getDataSources().get("ds_1").getWriteDataSourceName(), is("write_ds_1"));
        assertThat(actual.getDataSources().get("ds_1").getReadDataSourceNames(), is(Arrays.asList("write_ds_1_read_0", "write_ds_1_read_1")));
        assertThat(actual.getDataSources().get("ds_1").getLoadBalancerName(), is("random"));
    }
}
