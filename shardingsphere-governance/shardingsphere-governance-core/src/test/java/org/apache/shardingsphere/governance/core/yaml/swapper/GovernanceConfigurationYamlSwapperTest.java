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

import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GovernanceConfigurationYamlSwapperTest {
    
    @Test
    public void assertSwapToYamlGovernanceConfiguration() {
        GovernanceConfiguration expected = createGovernanceConfiguration();
        YamlGovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToYamlConfiguration(expected);
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenter().getType(), is(expected.getRegistryCenterConfiguration().getType()));
        assertThat(actual.getRegistryCenter().getNamespace(), is(expected.getRegistryCenterConfiguration().getNamespace()));
        assertThat(actual.getRegistryCenter().getServerLists(), is(expected.getRegistryCenterConfiguration().getServerLists()));
        assertThat(actual.getRegistryCenter().getProps(), is(expected.getRegistryCenterConfiguration().getProps()));
    }
    
    private GovernanceConfiguration createGovernanceConfiguration() {
        return new GovernanceConfiguration(new RegistryCenterConfiguration("TEST", "logic_schema", "127.0.0.1:2181", new Properties()), false);
    }
    
    @Test
    public void assertSwapToGovernanceConfiguration() {
        YamlGovernanceConfiguration expected = createYamlGovernanceConfiguration();
        GovernanceConfiguration actual = new GovernanceConfigurationYamlSwapper().swapToObject(expected);
        assertThat(actual.getRegistryCenterConfiguration().getNamespace(), is(expected.getRegistryCenter().getNamespace()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegistryCenterConfiguration().getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getRegistryCenterConfiguration().getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getRegistryCenterConfiguration().getProps(), is(expected.getRegistryCenter().getProps()));
    }
    
    private YamlGovernanceConfiguration createYamlGovernanceConfiguration() {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setRegistryCenter(createYamlRegistryCenterConfiguration());
        return result;
    }
    
    private YamlRegistryCenterConfiguration createYamlRegistryCenterConfiguration() {
        YamlRegistryCenterConfiguration result = new YamlRegistryCenterConfiguration();
        result.setType("TEST");
        result.setNamespace("logic_schema");
        result.setServerLists("127.0.0.1:2181");
        result.setProps(new Properties());
        return result;
    }
}
