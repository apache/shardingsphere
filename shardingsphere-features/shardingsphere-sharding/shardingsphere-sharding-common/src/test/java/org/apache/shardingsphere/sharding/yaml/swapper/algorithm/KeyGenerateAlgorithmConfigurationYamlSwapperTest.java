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

package org.apache.shardingsphere.sharding.yaml.swapper.algorithm;

import org.apache.shardingsphere.sharding.api.config.algorithm.KeyGenerateAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.algorithm.YamlKeyGenerateAlgorithmConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class KeyGenerateAlgorithmConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlKeyGenerateAlgorithmConfiguration actual = new KeyGenerateAlgorithmConfigurationYamlSwapper().swap(new KeyGenerateAlgorithmConfiguration("UUID", new Properties()));
        assertThat(actual.getType(), is("UUID"));
        assertThat(actual.getProperties(), is(new Properties()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlKeyGenerateAlgorithmConfiguration yamlConfiguration = new YamlKeyGenerateAlgorithmConfiguration();
        yamlConfiguration.setType("UUID");
        KeyGenerateAlgorithmConfiguration actual = new KeyGenerateAlgorithmConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(actual.getType(), is("UUID"));
        assertThat(actual.getProperties(), is(new Properties()));
    }
}
