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

import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlInstanceConfigurationSwapperUtilTest {
    
    @Test
    public void marshal() {
        YamlOrchestrationConfiguration expected = createExpectedYamlOrchestrationConfiguration();
        OrchestrationCenterConfiguration actual = YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(expected).getRegistryCenterConfiguration();
        assertThat(actual.getType(), is(expected.getRegistryCenter().getType()));
        assertThat(actual.getServerLists(), is(expected.getRegistryCenter().getServerLists()));
        assertThat(actual.getProps(), is(expected.getRegistryCenter().getProps()));
    }
    
    private YamlOrchestrationConfiguration createExpectedYamlOrchestrationConfiguration() {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName("test");
        result.setRegistryCenter(createYamlOrchestrationRepositoryConfiguration());
        return result;
    }
    
    private YamlOrchestrationCenterConfiguration createYamlOrchestrationRepositoryConfiguration() {
        YamlOrchestrationCenterConfiguration result = new YamlOrchestrationCenterConfiguration();
        result.setType("ZooKeeper");
        result.setServerLists("localhost:2181");
        result.setProps(new Properties());
        return result;
    }
}
