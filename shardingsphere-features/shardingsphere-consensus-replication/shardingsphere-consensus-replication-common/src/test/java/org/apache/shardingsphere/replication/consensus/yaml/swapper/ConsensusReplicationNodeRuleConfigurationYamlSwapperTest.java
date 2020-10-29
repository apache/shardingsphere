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

package org.apache.shardingsphere.replication.consensus.yaml.swapper;

import org.apache.shardingsphere.replication.consensus.api.config.ConsensusReplicationNodeRuleConfiguration;
import org.apache.shardingsphere.replication.consensus.yaml.config.YamlConsensusReplicationNodeRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ConsensusReplicationNodeRuleConfigurationYamlSwapperTest {
    
    private final ConsensusReplicationNodeRuleConfigurationYamlSwapper swapper = new ConsensusReplicationNodeRuleConfigurationYamlSwapper();
    
    private final String dataSourceName = "demo_ds_0";
    
    private final String replicaPeer = "127.0.0.1:9090";
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToYamlConfigurationWithMinProperties() {
        swapper.swapToYamlConfiguration(new ConsensusReplicationNodeRuleConfiguration(null, null));
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        YamlConsensusReplicationNodeRuleConfiguration yamlConfig = swapper.swapToYamlConfiguration(
                new ConsensusReplicationNodeRuleConfiguration(replicaPeer, dataSourceName));
        assertNotNull(yamlConfig);
        assertThat(yamlConfig.getReplicaPeer(), is(replicaPeer));
        assertThat(yamlConfig.getDataSourceName(), is(dataSourceName));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToObjectWithMinProperties() {
        new ConsensusReplicationNodeRuleConfiguration(null, null);
    }
    
    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlConsensusReplicationNodeRuleConfiguration yamlConfig = new YamlConsensusReplicationNodeRuleConfiguration();
        yamlConfig.setReplicaPeer(replicaPeer);
        yamlConfig.setDataSourceName(dataSourceName);
        ConsensusReplicationNodeRuleConfiguration config = swapper.swapToObject(yamlConfig);
        assertNotNull(config);
        assertThat(config.getReplicaPeer(), is(replicaPeer));
        assertThat(config.getDataSourceName(), is(dataSourceName));
    }
}
