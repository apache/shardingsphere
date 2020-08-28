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
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrchestrationConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration() {
        OrchestrationConfiguration expected = createOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration();
        YamlOrchestrationConfiguration actual = new OrchestrationConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenter().getType(), is(expected.getRegistryCenterConfiguration().getType()));
        assertThat(actual.getRegistryCenter().getServerLists(), is(expected.getRegistryCenterConfiguration().getServerLists()));
        assertThat(actual.getRegistryCenter().getProps(), is(expected.getRegistryCenterConfiguration().getProps()));
        assertFalse(expected.getAdditionalConfigCenterConfiguration().isPresent());
    }
    
    private OrchestrationConfiguration createOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration() {
        return new OrchestrationConfiguration("logic_schema", new OrchestrationCenterConfiguration("TEST", "127.0.0.1:2181", new Properties()), false);
    }
    
    @Test
    public void assertSwapToYamlOrchestrationConfigurationWithAdditionalConfigCenterConfiguration() {
        OrchestrationConfiguration expected = createOrchestrationConfigurationWithAdditionalConfigCenterConfiguration();
        YamlOrchestrationConfiguration actual = new OrchestrationConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenter().getType(), is(expected.getRegistryCenterConfiguration().getType()));
        assertThat(actual.getRegistryCenter().getServerLists(), is(expected.getRegistryCenterConfiguration().getServerLists()));
        assertThat(actual.getRegistryCenter().getProps(), is(expected.getRegistryCenterConfiguration().getProps()));
        assertTrue(expected.getAdditionalConfigCenterConfiguration().isPresent());
        assertThat(actual.getAdditionalConfigCenter().getType(), is(expected.getAdditionalConfigCenterConfiguration().get().getType()));
        assertThat(actual.getAdditionalConfigCenter().getServerLists(), is(expected.getAdditionalConfigCenterConfiguration().get().getServerLists()));
        assertThat(actual.getAdditionalConfigCenter().getProps(), is(expected.getAdditionalConfigCenterConfiguration().get().getProps()));
    }
    
    private OrchestrationConfiguration createOrchestrationConfigurationWithAdditionalConfigCenterConfiguration() {
        return new OrchestrationConfiguration("logic_schema", 
                new OrchestrationCenterConfiguration("TEST", "127.0.0.1:2181", new Properties()), new OrchestrationCenterConfiguration("ADDITIONAL", "127.0.0.1:2181", new Properties()), false);
    }
    
    @Test
    public void assertSwapToOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration() {
        YamlOrchestrationConfiguration expected = createYamlOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration();
        OrchestrationConfiguration actual = new OrchestrationConfigurationYamlSwapper().swapToObject(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenterConfiguration().getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getRegistryCenterConfiguration().getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getRegistryCenterConfiguration().getProps(), is(expected.getRegistryCenter().getProps()));
        assertFalse(actual.getAdditionalConfigCenterConfiguration().isPresent());
    }
    
    private YamlOrchestrationConfiguration createYamlOrchestrationConfigurationWithoutAdditionalConfigCenterConfiguration() {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName("logic_schema");
        result.setRegistryCenter(createYamlRegistryCenterConfiguration());
        return result;
    }
    
    @Test
    public void assertSwapToOrchestrationConfigurationWithAdditionalConfigCenterConfiguration() {
        YamlOrchestrationConfiguration expected = createYamlOrchestrationConfigurationWithAdditionalConfigCenterConfiguration();
        OrchestrationConfiguration actual = new OrchestrationConfigurationYamlSwapper().swapToObject(expected);
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenterConfiguration().getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getRegistryCenterConfiguration().getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getRegistryCenterConfiguration().getProps(), is(expected.getRegistryCenter().getProps()));
        assertTrue(actual.getAdditionalConfigCenterConfiguration().isPresent());
        assertThat(actual.getAdditionalConfigCenterConfiguration().get().getType(), is(expected.getAdditionalConfigCenter().getType()));
        assertThat(actual.getAdditionalConfigCenterConfiguration().get().getServerLists(), is(expected.getAdditionalConfigCenter().getServerLists()));
        assertThat(actual.getAdditionalConfigCenterConfiguration().get().getProps(), is(expected.getAdditionalConfigCenter().getProps()));
    }
    
    private YamlOrchestrationConfiguration createYamlOrchestrationConfigurationWithAdditionalConfigCenterConfiguration() {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName("logic_schema");
        result.setRegistryCenter(createYamlRegistryCenterConfiguration());
        result.setAdditionalConfigCenter(createYamlAdditionalConfigCenterConfiguration());
        return result;
    }
    
    private YamlOrchestrationCenterConfiguration createYamlRegistryCenterConfiguration() {
        YamlOrchestrationCenterConfiguration result = new YamlOrchestrationCenterConfiguration();
        result.setType("TEST");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2181");
        return result;
    }
    
    private YamlOrchestrationCenterConfiguration createYamlAdditionalConfigCenterConfiguration() {
        YamlOrchestrationCenterConfiguration result = new YamlOrchestrationCenterConfiguration();
        result.setType("ADDITIONAL");
        result.setProps(new Properties());
        result.setServerLists("127.0.0.1:2182");
        return result;
    }
}
