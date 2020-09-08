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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GovernanceCenterConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYamlConfiguration() {
        GovernanceCenterConfiguration expected = new GovernanceCenterConfiguration("TEST", "127.0.0.1:2181", new Properties());
        YamlGovernanceCenterConfiguration actual = new GovernanceCenterConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getType(), is(expected.getType()));
        assertThat(actual.getServerLists(), is(expected.getServerLists()));
        assertThat(actual.getProps(), is(expected.getProps()));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlGovernanceCenterConfiguration yamlConfiguration = getYamlGovernanceCenterConfiguration();
        GovernanceCenterConfiguration governanceCenterConfig = new GovernanceCenterConfigurationYamlSwapper().swapToObject(yamlConfiguration);
        assertThat(governanceCenterConfig.getType(), is(yamlConfiguration.getType()));
        assertThat(governanceCenterConfig.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(governanceCenterConfig.getProps(), is(yamlConfiguration.getProps()));
    }
    
    private YamlGovernanceCenterConfiguration getYamlGovernanceCenterConfiguration() {
        YamlGovernanceCenterConfiguration result = new YamlGovernanceCenterConfiguration();
        result.setType("TEST");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181");
        return result;
    }
}
