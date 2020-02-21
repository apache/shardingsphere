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

import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.configuration.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlOrchestrationConfiguration;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class OrchestrationConfigurationYamlSwapperTest {
    
    private static final String LOGIC_SCHEMA = "logic_schema";
    
    @Test
    public void assertSwapToYamlOrchestrationConfiguration() {
        OrchestrationConfiguration data = getOrchestrationConfiguration();
        YamlOrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swap(data);
        for (String each : result.getInstanceConfigurationMap().keySet()) {
            assertNotNull(result.getInstanceConfigurationMap().get(each));
            assertThat(result.getInstanceConfigurationMap().get(each).getOrchestrationType(),
                    is(data.getInstanceConfigurationMap().get(each).getOrchestrationType()));
            assertThat(result.getInstanceConfigurationMap().get(each).getInstanceType(),
                    is(data.getInstanceConfigurationMap().get(each).getType()));
            assertThat(result.getInstanceConfigurationMap().get(each).getNamespace(),
                    is(data.getInstanceConfigurationMap().get(each).getNamespace()));
            assertThat(result.getInstanceConfigurationMap().get(each).getServerLists(),
                    is(data.getInstanceConfigurationMap().get(each).getServerLists()));
            assertThat(result.getInstanceConfigurationMap().get(each).getProps(),
                    is(data.getInstanceConfigurationMap().get(each).getProperties()));
        }
    }
    
    @Test
    public void assertSwapToOrchestrationConfiguration() {
        YamlOrchestrationConfiguration data = getYamlOrchestrationConfiguration();
        OrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swap(data);
        for (String each : result.getInstanceConfigurationMap().keySet()) {
            assertNotNull(result.getInstanceConfigurationMap().get(each));
            assertThat(result.getInstanceConfigurationMap().get(each).getOrchestrationType(),
                    is(data.getInstanceConfigurationMap().get(each).getOrchestrationType()));
            assertThat(result.getInstanceConfigurationMap().get(each).getType(),
                    is(data.getInstanceConfigurationMap().get(each).getInstanceType()));
            assertThat(result.getInstanceConfigurationMap().get(each).getNamespace(),
                    is(data.getInstanceConfigurationMap().get(each).getNamespace()));
            assertThat(result.getInstanceConfigurationMap().get(each).getServerLists(),
                    is(data.getInstanceConfigurationMap().get(each).getServerLists()));
            assertThat(result.getInstanceConfigurationMap().get(each).getProperties(),
                    is(data.getInstanceConfigurationMap().get(each).getProps()));
        }
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        InstanceConfiguration instanceConfiguration = new InstanceConfiguration("zookeeper", new Properties());
        instanceConfiguration.setOrchestrationType("config_center");
        instanceConfiguration.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        instanceConfiguration.setNamespace("orchestration");
        Map<String, InstanceConfiguration> instanceConfigurationMap = new HashMap<>();
        instanceConfigurationMap.put(LOGIC_SCHEMA, instanceConfiguration);
        OrchestrationConfiguration result = new OrchestrationConfiguration();
        result.setInstanceConfigurationMap(instanceConfigurationMap);
        return result;
    }
    
    private YamlOrchestrationConfiguration getYamlOrchestrationConfiguration() {
        YamlInstanceConfiguration yamlInstanceConfiguration = new YamlInstanceConfiguration();
        yamlInstanceConfiguration.setInstanceType("zookeeper");
        yamlInstanceConfiguration.setProps(new Properties());
        yamlInstanceConfiguration.setOrchestrationType("config_center");
        yamlInstanceConfiguration.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        yamlInstanceConfiguration.setNamespace("orchestration");
        Map<String, YamlInstanceConfiguration> yamlInstanceConfigurationMap = new HashMap<>();
        yamlInstanceConfigurationMap.put(LOGIC_SCHEMA, yamlInstanceConfiguration);
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setInstanceConfigurationMap(yamlInstanceConfigurationMap);
        return result;
    }
}
