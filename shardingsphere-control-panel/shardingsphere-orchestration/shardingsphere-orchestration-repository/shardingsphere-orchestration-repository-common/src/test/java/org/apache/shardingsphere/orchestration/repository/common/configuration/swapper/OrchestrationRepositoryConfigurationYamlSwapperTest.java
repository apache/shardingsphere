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

package org.apache.shardingsphere.orchestration.repository.common.configuration.swapper;

import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationRepositoryConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYamlConfiguration() {
        OrchestrationRepositoryConfiguration configuration = getConfiguration();
        YamlOrchestrationRepositoryConfiguration yamlConfiguration = new OrchestrationRepositoryConfigurationYamlSwapper().swapToYamlConfiguration(configuration);
        assertThat(yamlConfiguration.getInstanceType(), is(configuration.getType()));
        assertThat(yamlConfiguration.getServerLists(), is(configuration.getServerLists()));
        assertThat(yamlConfiguration.getNamespace(), is(configuration.getNamespace()));
        assertThat(yamlConfiguration.getProps(), is(configuration.getProps()));
    }
    
    private OrchestrationRepositoryConfiguration getConfiguration() {
        OrchestrationRepositoryConfiguration result = new OrchestrationRepositoryConfiguration("zookeeper", new Properties());
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        YamlOrchestrationRepositoryConfiguration yamlConfiguration = getYamlInstanceConfiguration();
        OrchestrationRepositoryConfiguration instanceConfiguration = new OrchestrationRepositoryConfigurationYamlSwapper().swapToObject(yamlConfiguration);
        assertThat(instanceConfiguration.getType(), is(yamlConfiguration.getInstanceType()));
        assertThat(instanceConfiguration.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(instanceConfiguration.getNamespace(), is(yamlConfiguration.getNamespace()));
        assertThat(instanceConfiguration.getProps(), is(yamlConfiguration.getProps()));
    }
    
    private YamlOrchestrationRepositoryConfiguration getYamlInstanceConfiguration() {
        YamlOrchestrationRepositoryConfiguration result = new YamlOrchestrationRepositoryConfiguration();
        result.setInstanceType("zookeeper");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
}
