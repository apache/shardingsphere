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

package org.apache.shardingsphere.infra.yaml.swapper.algorithm;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereAlgorithmConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlShardingSphereAlgorithmConfiguration actual = new ShardingSphereAlgorithmConfigurationYamlSwapper().swapToYamlConfiguration(
                new ShardingSphereAlgorithmConfiguration("TEST", createProps()));
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShardingSphereAlgorithmConfiguration yamlConfig = new YamlShardingSphereAlgorithmConfiguration();
        yamlConfig.setType("TEST");
        yamlConfig.setProps(createProps());
        ShardingSphereAlgorithmConfiguration actual = new ShardingSphereAlgorithmConfigurationYamlSwapper().swapToObject(yamlConfig);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    private Properties createProps() {
        Properties result = new Properties();
        result.setProperty("key", "value");
        return result;
    }
}
