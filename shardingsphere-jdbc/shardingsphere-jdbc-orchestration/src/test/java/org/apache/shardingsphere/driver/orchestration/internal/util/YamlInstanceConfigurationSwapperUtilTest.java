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

import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.orchestration.core.common.CenterType;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public final class YamlInstanceConfigurationSwapperUtilTest {
    
    @Test
    public void marshal() {
        YamlCenterRepositoryConfiguration yamlCenterRepositoryConfiguration = getYamlInstanceConfiguration();
        Map<String, YamlCenterRepositoryConfiguration> yamlConfigurationMap = Collections.singletonMap("test", yamlCenterRepositoryConfiguration);
        Map<String, CenterConfiguration> configurationMap = YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlConfigurationMap);
        CenterConfiguration configuration = configurationMap.get("test");
        assertEquals(configuration.getType(), yamlCenterRepositoryConfiguration.getInstanceType());
        assertEquals(configuration.getOrchestrationType(), yamlCenterRepositoryConfiguration.getOrchestrationType());
        assertEquals(configuration.getNamespace(), yamlCenterRepositoryConfiguration.getNamespace());
        assertEquals(configuration.getServerLists(), yamlCenterRepositoryConfiguration.getServerLists());
        assertEquals(configuration.getProps(), yamlCenterRepositoryConfiguration.getProps());
    }
    
    private YamlCenterRepositoryConfiguration getYamlInstanceConfiguration() {
        YamlCenterRepositoryConfiguration yamlConfiguration = new YamlCenterRepositoryConfiguration();
        yamlConfiguration.setOrchestrationType(CenterType.REGISTRY_CENTER.getValue());
        yamlConfiguration.setInstanceType("zookeeper");
        yamlConfiguration.setNamespace("test");
        yamlConfiguration.setServerLists("localhost:2181");
        yamlConfiguration.setProps(new Properties());
        return yamlConfiguration;
    }
}
