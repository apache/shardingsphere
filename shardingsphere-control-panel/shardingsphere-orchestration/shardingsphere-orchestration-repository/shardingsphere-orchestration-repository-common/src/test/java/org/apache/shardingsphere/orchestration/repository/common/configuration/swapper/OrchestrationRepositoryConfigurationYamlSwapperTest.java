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

import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationRepositoryConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYamlConfiguration() {
        OrchestrationCenterConfiguration config = new OrchestrationCenterConfiguration("zookeeper", "127.0.0.1:2181,127.0.0.1:2182", new Properties());
        YamlOrchestrationCenterConfiguration yamlConfiguration = new OrchestrationCenterConfigurationYamlSwapper().swapToYamlConfiguration(config);
        assertThat(yamlConfiguration.getType(), is(config.getType()));
        assertThat(yamlConfiguration.getServerLists(), is(config.getServerLists()));
        assertThat(yamlConfiguration.getProps(), is(config.getProps()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlOrchestrationCenterConfiguration yamlConfiguration = getYamlInstanceConfiguration();
        OrchestrationCenterConfiguration orchestrationCenterConfig = new OrchestrationCenterConfigurationYamlSwapper().swapToObject(yamlConfiguration);
        assertThat(orchestrationCenterConfig.getType(), is(yamlConfiguration.getType()));
        assertThat(orchestrationCenterConfig.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(orchestrationCenterConfig.getProps(), is(yamlConfiguration.getProps()));
    }
    
    private YamlOrchestrationCenterConfiguration getYamlInstanceConfiguration() {
        YamlOrchestrationCenterConfiguration result = new YamlOrchestrationCenterConfiguration();
        result.setType("zookeeper");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        return result;
    }
}
