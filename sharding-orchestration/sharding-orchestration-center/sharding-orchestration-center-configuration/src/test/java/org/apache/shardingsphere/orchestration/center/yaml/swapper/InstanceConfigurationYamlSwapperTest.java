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

package org.apache.shardingsphere.orchestration.center.yaml.swapper;

import java.util.Properties;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InstanceConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYaml() {
        InstanceConfiguration instanceConfiguration = getInstanceConfiguration();
        YamlInstanceConfiguration yamlConfiguration = new InstanceConfigurationYamlSwapper().swap(instanceConfiguration);
        assertThat(yamlConfiguration.getOrchestrationType(), is(instanceConfiguration.getOrchestrationType()));
        assertThat(yamlConfiguration.getInstanceType(), is(instanceConfiguration.getType()));
        assertThat(yamlConfiguration.getServerLists(), is(instanceConfiguration.getServerLists()));
        assertThat(yamlConfiguration.getNamespace(), is(instanceConfiguration.getNamespace()));
        assertThat(yamlConfiguration.getProps(), is(instanceConfiguration.getProperties()));
    }
    
    private InstanceConfiguration getInstanceConfiguration() {
        InstanceConfiguration result = new InstanceConfiguration("zookeeper", new Properties());
        result.setOrchestrationType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
    
    @Test
    public void assertSwapToConfiguration() {
        YamlInstanceConfiguration yamlConfiguration = getYamlInstanceConfiguration();
        InstanceConfiguration instanceConfiguration = new InstanceConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(instanceConfiguration.getOrchestrationType(), is(yamlConfiguration.getOrchestrationType()));
        assertThat(instanceConfiguration.getType(), is(yamlConfiguration.getInstanceType()));
        assertThat(instanceConfiguration.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(instanceConfiguration.getNamespace(), is(yamlConfiguration.getNamespace()));
        assertThat(instanceConfiguration.getProperties(), is(yamlConfiguration.getProps()));
    }
    
    private YamlInstanceConfiguration getYamlInstanceConfiguration() {
        YamlInstanceConfiguration result = new YamlInstanceConfiguration();
        result.setInstanceType("zookeeper");
        result.setProps(new Properties());
        result.setOrchestrationType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
}
