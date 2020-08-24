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

package org.apache.shardingsphere.orchestration.core.common.yaml.swapper;

import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationCenterConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYamlConfiguration() {
        OrchestrationCenterConfiguration expected = new OrchestrationCenterConfiguration("TEST", "127.0.0.1:2181", new Properties());
        YamlOrchestrationCenterConfiguration actual = new OrchestrationCenterConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getType(), is(expected.getType()));
        assertThat(actual.getServerLists(), is(expected.getServerLists()));
        assertThat(actual.getProps(), is(expected.getProps()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlOrchestrationCenterConfiguration yamlConfiguration = getYamlOrchestrationCenterConfiguration();
        OrchestrationCenterConfiguration orchestrationCenterConfig = new OrchestrationCenterConfigurationYamlSwapper().swapToObject(yamlConfiguration);
        assertThat(orchestrationCenterConfig.getType(), is(yamlConfiguration.getType()));
        assertThat(orchestrationCenterConfig.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(orchestrationCenterConfig.getProps(), is(yamlConfiguration.getProps()));
    }
    
    private YamlOrchestrationCenterConfiguration getYamlOrchestrationCenterConfiguration() {
        YamlOrchestrationCenterConfiguration result = new YamlOrchestrationCenterConfiguration();
        result.setType("TEST");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181");
        return result;
    }
}
