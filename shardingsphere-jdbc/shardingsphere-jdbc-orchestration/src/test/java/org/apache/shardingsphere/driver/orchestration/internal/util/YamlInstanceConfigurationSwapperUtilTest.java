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

package org.apache.shardingsphere.driver.orchestration.internal.util;

import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public final class YamlInstanceConfigurationSwapperUtilTest {
    
    @Test
    public void marshal() {
        YamlOrchestrationRepositoryConfiguration yamlOrchestrationRepositoryConfiguration = getYamlOrchestrationRepositoryConfiguration();
        YamlOrchestrationConfiguration yamlConfiguration = new YamlOrchestrationConfiguration();
        yamlConfiguration.setName("test");
        yamlConfiguration.setRegistryRepositoryConfiguration(yamlOrchestrationRepositoryConfiguration);
        OrchestrationConfiguration orchestrationConfiguration = YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(yamlConfiguration);
        OrchestrationRepositoryConfiguration configuration = orchestrationConfiguration.getRegistryRepositoryConfiguration();
        assertEquals(configuration.getType(), yamlOrchestrationRepositoryConfiguration.getInstanceType());
        assertEquals(configuration.getNamespace(), yamlOrchestrationRepositoryConfiguration.getNamespace());
        assertEquals(configuration.getServerLists(), yamlOrchestrationRepositoryConfiguration.getServerLists());
        assertEquals(configuration.getProps(), yamlOrchestrationRepositoryConfiguration.getProps());
    }
    
    private YamlOrchestrationRepositoryConfiguration getYamlOrchestrationRepositoryConfiguration() {
        YamlOrchestrationRepositoryConfiguration yamlConfiguration = new YamlOrchestrationRepositoryConfiguration();
        yamlConfiguration.setInstanceType("zookeeper");
        yamlConfiguration.setNamespace("test");
        yamlConfiguration.setServerLists("localhost:2181");
        yamlConfiguration.setProps(new Properties());
        return yamlConfiguration;
    }
}
