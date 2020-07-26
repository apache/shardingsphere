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

import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationConfigurationYamlSwapperTest {
    
    private static final String LOGIC_SCHEMA = "logic_schema";
    
    @Test
    public void assertSwapToYamlOrchestrationConfiguration() {
        OrchestrationConfiguration data = getOrchestrationConfiguration();
        YamlOrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swapToYamlConfiguration(data);
        assertThat(result.getRegistryRepositoryConfiguration().getInstanceType(), is(data.getRegistryRepositoryConfiguration().getType()));
        assertThat(result.getRegistryRepositoryConfiguration().getNamespace(), is(data.getRegistryRepositoryConfiguration().getNamespace()));
        assertThat(result.getRegistryRepositoryConfiguration().getServerLists(), is(data.getRegistryRepositoryConfiguration().getServerLists()));
        assertThat(result.getRegistryRepositoryConfiguration().getProps(), is(data.getRegistryRepositoryConfiguration().getProps()));
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        OrchestrationRepositoryConfiguration repositoryConfiguration = new OrchestrationRepositoryConfiguration("zookeeper", new Properties());
        repositoryConfiguration.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        repositoryConfiguration.setNamespace("orchestration");
        return new OrchestrationConfiguration(LOGIC_SCHEMA, repositoryConfiguration);
    }
    
    @Test
    public void assertSwapToOrchestrationConfiguration() {
        YamlOrchestrationConfiguration data = getYamlOrchestrationConfiguration();
        OrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swapToObject(data);
        assertThat(result.getRegistryRepositoryConfiguration().getType(), is(data.getRegistryRepositoryConfiguration().getInstanceType()));
        assertThat(result.getRegistryRepositoryConfiguration().getNamespace(), is(data.getRegistryRepositoryConfiguration().getNamespace()));
        assertThat(result.getRegistryRepositoryConfiguration().getServerLists(), is(data.getRegistryRepositoryConfiguration().getServerLists()));
        assertThat(result.getRegistryRepositoryConfiguration().getProps(), is(data.getRegistryRepositoryConfiguration().getProps()));
    }
    
    private YamlOrchestrationConfiguration getYamlOrchestrationConfiguration() {
        YamlOrchestrationRepositoryConfiguration registryRepositoryConfiguration = new YamlOrchestrationRepositoryConfiguration();
        registryRepositoryConfiguration.setInstanceType("zookeeper");
        registryRepositoryConfiguration.setProps(new Properties());
        registryRepositoryConfiguration.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        registryRepositoryConfiguration.setNamespace("orchestration");
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName(LOGIC_SCHEMA);
        result.setRegistryRepositoryConfiguration(registryRepositoryConfiguration);
        return result;
    }
}
