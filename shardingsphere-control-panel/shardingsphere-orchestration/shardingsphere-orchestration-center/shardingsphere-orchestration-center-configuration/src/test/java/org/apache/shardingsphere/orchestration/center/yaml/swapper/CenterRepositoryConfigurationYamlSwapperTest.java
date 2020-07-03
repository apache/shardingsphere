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

import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CenterRepositoryConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYamlConfiguration() {
        CenterConfiguration configuration = getConfiguration();
        YamlCenterRepositoryConfiguration yamlConfiguration = new CenterRepositoryConfigurationYamlSwapper().swapToYamlConfiguration(configuration);
        assertThat(yamlConfiguration.getOrchestrationType(), is(configuration.getOrchestrationType()));
        assertThat(yamlConfiguration.getInstanceType(), is(configuration.getType()));
        assertThat(yamlConfiguration.getServerLists(), is(configuration.getServerLists()));
        assertThat(yamlConfiguration.getNamespace(), is(configuration.getNamespace()));
        assertThat(yamlConfiguration.getProps(), is(configuration.getProps()));
    }
    
    private CenterConfiguration getConfiguration() {
        CenterConfiguration result = new CenterConfiguration("zookeeper", new Properties());
        result.setOrchestrationType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        YamlCenterRepositoryConfiguration yamlConfiguration = getYamlInstanceConfiguration();
        CenterConfiguration instanceConfiguration = new CenterRepositoryConfigurationYamlSwapper().swapToObject(yamlConfiguration);
        assertThat(instanceConfiguration.getOrchestrationType(), is(yamlConfiguration.getOrchestrationType()));
        assertThat(instanceConfiguration.getType(), is(yamlConfiguration.getInstanceType()));
        assertThat(instanceConfiguration.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(instanceConfiguration.getNamespace(), is(yamlConfiguration.getNamespace()));
        assertThat(instanceConfiguration.getProps(), is(yamlConfiguration.getProps()));
    }
    
    private YamlCenterRepositoryConfiguration getYamlInstanceConfiguration() {
        YamlCenterRepositoryConfiguration result = new YamlCenterRepositoryConfiguration();
        result.setInstanceType("zookeeper");
        result.setProps(new Properties());
        result.setOrchestrationType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
}
