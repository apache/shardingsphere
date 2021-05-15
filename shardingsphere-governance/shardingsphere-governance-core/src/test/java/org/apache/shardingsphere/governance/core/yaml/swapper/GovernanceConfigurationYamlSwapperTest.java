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

import org.apache.shardingsphere.governance.core.yaml.config.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GovernanceConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration() {
        GovernanceConfiguration expected = createGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration();
        YamlGovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenter().getType(), is(expected.getRegistryCenterConfiguration().getType()));
        assertThat(actual.getRegistryCenter().getServerLists(), is(expected.getRegistryCenterConfiguration().getServerLists()));
        assertThat(actual.getRegistryCenter().getProps(), is(expected.getRegistryCenterConfiguration().getProps()));
    }
    
    private GovernanceConfiguration createGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration() {
        return new GovernanceConfiguration("logic_schema", new RegistryCenterConfiguration("TEST", "127.0.0.1:2181", new Properties()), false);
    }
    
    @Test
    public void assertSwapToYamlGovernanceConfigurationWithAdditionalConfigCenterConfiguration() {
        GovernanceConfiguration expected = createGovernanceConfigurationWithAdditionalConfigCenterConfiguration();
        YamlGovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenter().getType(), is(expected.getRegistryCenterConfiguration().getType()));
        assertThat(actual.getRegistryCenter().getServerLists(), is(expected.getRegistryCenterConfiguration().getServerLists()));
        assertThat(actual.getRegistryCenter().getProps(), is(expected.getRegistryCenterConfiguration().getProps()));
    }
    
    private GovernanceConfiguration createGovernanceConfigurationWithAdditionalConfigCenterConfiguration() {
        return new GovernanceConfiguration("logic_schema", 
                new RegistryCenterConfiguration("TEST", "127.0.0.1:2181", new Properties()), false);
    }
    
    @Test
    public void assertSwapToGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration() {
        YamlGovernanceConfiguration expected = createYamlGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration();
        GovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToObject(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenterConfiguration().getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getRegistryCenterConfiguration().getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getRegistryCenterConfiguration().getProps(), is(expected.getRegistryCenter().getProps()));
    }
    
    private YamlGovernanceConfiguration createYamlGovernanceConfigurationWithoutAdditionalConfigCenterConfiguration() {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setName("logic_schema");
        result.setRegistryCenter(createYamlRegistryCenterConfiguration());
        return result;
    }
    
    @Test
    public void assertSwapToGovernanceConfigurationWithAdditionalConfigCenterConfiguration() {
        YamlGovernanceConfiguration expected = createYamlGovernanceConfigurationWithAdditionalConfigCenterConfiguration();
        GovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToObject(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenterConfiguration().getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getRegistryCenterConfiguration().getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getRegistryCenterConfiguration().getProps(), is(expected.getRegistryCenter().getProps()));
    }
    
    private YamlGovernanceConfiguration createYamlGovernanceConfigurationWithAdditionalConfigCenterConfiguration() {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setName("logic_schema");
        result.setRegistryCenter(createYamlRegistryCenterConfiguration());
        return result;
    }
    
    private YamlRegistryCenterConfiguration createYamlRegistryCenterConfiguration() {
        YamlRegistryCenterConfiguration result = new YamlRegistryCenterConfiguration();
        result.setType("TEST");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181");
        return result;
    }
}
