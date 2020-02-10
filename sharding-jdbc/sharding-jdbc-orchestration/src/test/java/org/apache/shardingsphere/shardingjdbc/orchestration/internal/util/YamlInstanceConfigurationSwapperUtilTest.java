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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.util;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;
import org.apache.shardingsphere.orchestration.constant.OrchestrationType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class YamlInstanceConfigurationSwapperUtilTest {
    
    @Test
    public void marshal() {
        YamlInstanceConfiguration yamlInstanceConfiguration = getYamlInstanceConfiguration();
        Map<String, YamlInstanceConfiguration> yamlInstanceConfigurationMap = Collections.singletonMap("test", yamlInstanceConfiguration);
        Map<String, InstanceConfiguration> instanceConfigurationMap = YamlInstanceConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap);
        InstanceConfiguration instanceConfiguration = instanceConfigurationMap.get("test");
        assertEquals(instanceConfiguration.getType(), yamlInstanceConfiguration.getInstanceType());
        assertEquals(instanceConfiguration.getOrchestrationType(), yamlInstanceConfiguration.getOrchestrationType());
        assertEquals(instanceConfiguration.getNamespace(), yamlInstanceConfiguration.getNamespace());
        assertEquals(instanceConfiguration.getServerLists(), yamlInstanceConfiguration.getServerLists());
        assertEquals(instanceConfiguration.getProperties(), yamlInstanceConfiguration.getProps());
    }
    
    private YamlInstanceConfiguration getYamlInstanceConfiguration() {
        YamlInstanceConfiguration yamlInstanceConfiguration = new YamlInstanceConfiguration();
        yamlInstanceConfiguration.setOrchestrationType(OrchestrationType.REGISTRY_CENTER.getValue());
        yamlInstanceConfiguration.setInstanceType("zookeeper");
        yamlInstanceConfiguration.setNamespace("test");
        yamlInstanceConfiguration.setServerLists("localhost:2181");
        yamlInstanceConfiguration.setProps(new Properties());
        return yamlInstanceConfiguration;
    }
}
