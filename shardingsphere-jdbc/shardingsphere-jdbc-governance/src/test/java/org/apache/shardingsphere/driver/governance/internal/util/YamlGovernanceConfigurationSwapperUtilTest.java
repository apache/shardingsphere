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

package org.apache.shardingsphere.driver.governance.internal.util;

import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlGovernanceConfigurationSwapperUtilTest {
    
    @Test
    public void marshal() {
        YamlGovernanceConfiguration expected = createExpectedYamlGovernanceConfiguration();
        RegistryCenterConfiguration actual = YamlGovernanceConfigurationSwapperUtil.marshal(expected).getRegistryCenterConfiguration();
        assertThat(actual.getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getProps(), is(expected.getRegistryCenter().getProps()));
    }
    
    private YamlGovernanceConfiguration createExpectedYamlGovernanceConfiguration() {
        YamlGovernanceConfiguration result = new YamlGovernanceConfiguration();
        result.setName("test");
        result.setRegistryCenter(createYamlRegistryRepositoryConfiguration());
        return result;
    }
    
    private YamlRegistryCenterConfiguration createYamlRegistryRepositoryConfiguration() {
        YamlRegistryCenterConfiguration result = new YamlRegistryCenterConfiguration();
        result.setType("ZooKeeper");
        result.setServerLists("localhost:2181");
        result.setProps(new Properties());
        return result;
    }
}
