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

package org.apache.shardingsphere.scaling.core.config.yaml;

import org.apache.shardingsphere.governance.core.yaml.config.YamlRegistryCenterConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ServerConfigurationYamlSwapperTest {
    
    private final ServerConfigurationYamlSwapper serverConfigurationYamlSwapper = new ServerConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlServerConfiguration yamlServerConfig = serverConfigurationYamlSwapper.swapToYamlConfiguration(mockServerConfig());
        assertThat(yamlServerConfig.getScaling().getWorkerThread(), is(10));
        assertThat(yamlServerConfig.getGovernance().getName(), is("test"));
        assertThat(yamlServerConfig.getGovernance().getRegistryCenter().getType(), is("Zookeeper"));
    }
    
    @Test
    public void assertSwapToObject() {
        ServerConfiguration serverConfig = serverConfigurationYamlSwapper.swapToObject(mockYamlServerConfig());
        assertThat(serverConfig.getWorkerThread(), is(10));
        assertThat(serverConfig.getGovernanceConfig().getName(), is("test"));
    }
    
    private ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setWorkerThread(10);
        result.setGovernanceConfig(new GovernanceConfiguration("test", new RegistryCenterConfiguration("Zookeeper", "localhost:2181", null), false));
        return result;
    }
    
    private YamlServerConfiguration mockYamlServerConfig() {
        YamlServerConfiguration result = new YamlServerConfiguration();
        YamlGovernanceConfiguration config = new YamlGovernanceConfiguration();
        config.setName("test");
        config.setRegistryCenter(new YamlRegistryCenterConfiguration());
        result.setGovernance(config);
        result.getScaling().setWorkerThread(10);
        return result;
    }
}
