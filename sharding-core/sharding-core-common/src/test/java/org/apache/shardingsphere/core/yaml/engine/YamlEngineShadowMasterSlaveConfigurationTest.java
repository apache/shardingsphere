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

package org.apache.shardingsphere.core.yaml.engine;

import org.apache.shardingsphere.core.yaml.config.shadow.YamlRootShadowConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlEngineShadowMasterSlaveConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/shadow-master-slave-rule.yaml");
        assertNotNull(url);
        assertYamlMasterSlaveConfig(YamlEngine.unmarshal(new File(url.getFile()), YamlRootShadowConfiguration.class));
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/shadow-master-slave-rule.yaml");
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
        assertYamlMasterSlaveConfig(YamlEngine.unmarshal(yamlContent.toString().getBytes(), YamlRootShadowConfiguration.class));
    }
    
    private void assertYamlMasterSlaveConfig(final YamlRootShadowConfiguration actual) {
        assertDataSourceMap(actual);
        assertMasterSlaveRule(actual);
        assertThat(actual.getShadowRule().getColumn(), is("is_shadow"));
        assertShadowMappings(actual);
    }
    
    private void assertDataSourceMap(final YamlRootShadowConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(6));
        assertTrue(actual.getDataSources().containsKey("master_ds"));
        assertTrue(actual.getDataSources().containsKey("slave_ds_0"));
        assertTrue(actual.getDataSources().containsKey("slave_ds_1"));
    }
    
    private void assertMasterSlaveRule(final YamlRootShadowConfiguration actual) {
        assertThat(actual.getShadowRule().getMasterSlaveRule().getName(), is("master-slave-ds"));
        assertThat(actual.getShadowRule().getMasterSlaveRule().getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getShadowRule().getMasterSlaveRule().getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertThat(actual.getShadowRule().getMasterSlaveRule().getLoadBalanceAlgorithmType(), is("ROUND_ROBIN"));
    }
    
    private void assertShadowMappings(final YamlRootShadowConfiguration actual) {
        assertThat(actual.getShadowRule().getShadowMappings().size(), is(3));
        assertThat(actual.getShadowRule().getShadowMappings().get("master_ds"), is("shadow_master_ds"));
        assertThat(actual.getShadowRule().getShadowMappings().get("slave_ds_0"), is("shadow_slave_ds_0"));
        assertThat(actual.getShadowRule().getShadowMappings().get("slave_ds_1"), is("shadow_slave_ds_1"));
    }
}
