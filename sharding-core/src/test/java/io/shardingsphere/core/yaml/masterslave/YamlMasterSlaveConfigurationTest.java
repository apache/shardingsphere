/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.masterslave;

import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
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

public final class YamlMasterSlaveConfigurationTest {
    
    @Test
    public void assertUnmarshalWithYamlFile() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/master-slave-rule.yaml");
        assertNotNull(url);
        assertYamlMasterSlaveConfig(YamlMasterSlaveConfiguration.unmarshal(new File(url.getFile())));
    }
    
    @Test
    public void assertUnmarshalWithYamlBytes() throws IOException {
        URL url = getClass().getClassLoader().getResource("yaml/master-slave-rule.yaml");
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
        assertYamlMasterSlaveConfig(YamlMasterSlaveConfiguration.unmarshal(yamlContent.toString().getBytes()));
    }
    
    private void assertYamlMasterSlaveConfig(final YamlMasterSlaveConfiguration actual) {
        assertDataSourceMap(actual);
        assertMasterSlaveRule(actual);
        assertConfigMap(actual);
    }
    
    private void assertDataSourceMap(final YamlMasterSlaveConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(3));
        assertTrue(actual.getDataSources().containsKey("master_ds"));
        assertTrue(actual.getDataSources().containsKey("slave_ds_0"));
        assertTrue(actual.getDataSources().containsKey("slave_ds_1"));
    }
    
    private void assertMasterSlaveRule(final YamlMasterSlaveConfiguration actual) {
        assertThat(actual.getMasterSlaveRule().getName(), is("master-slave-ds"));
        assertThat(actual.getMasterSlaveRule().getMasterDataSourceName(), is("master_ds"));
        assertThat(actual.getMasterSlaveRule().getSlaveDataSourceNames(), CoreMatchers.<Collection<String>>is(Arrays.asList("slave_ds_0", "slave_ds_1")));
        assertThat(actual.getMasterSlaveRule().getLoadBalanceAlgorithmType(), is(MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN));
        assertThat(actual.getMasterSlaveRule().getLoadBalanceAlgorithmClassName(), is("TestAlgorithmClass"));
    }
    
    private void assertConfigMap(final YamlMasterSlaveConfiguration actual) {
        assertThat(actual.getConfigMap().size(), is(2));
        assertThat(actual.getConfigMap().get("key1"), is((Object) "value1"));
        assertThat(actual.getConfigMap().get("key2"), is((Object) "value2"));
    }
}
