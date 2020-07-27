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

package org.apache.shardingsphere.cluster.configuration.swapper;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlHeartbeatConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

public final class ClusterConfigurationYamlSwapperTest {
    
    private static final String SQL = "select 1";
    
    private static final int INTERVAL = 60;
    
    private static final int MAXIMUM = 3;
    
    @Test
    public void assertSwapToClusterConfiguration() {
        YamlHeartbeatConfiguration yamlHeartBeatConfiguration = new YamlHeartbeatConfiguration();
        yamlHeartBeatConfiguration.setSql(SQL);
        yamlHeartBeatConfiguration.setInterval(INTERVAL);
        yamlHeartBeatConfiguration.setRetryEnable(true);
        yamlHeartBeatConfiguration.setRetryMaximum(MAXIMUM);
        YamlClusterConfiguration yamlClusterConfiguration = new YamlClusterConfiguration();
        yamlClusterConfiguration.setHeartbeat(yamlHeartBeatConfiguration);
        ClusterConfiguration clusterConfiguration = new ClusterConfigurationYamlSwapper().swapToObject(yamlClusterConfiguration);
        assertThat(clusterConfiguration.getHeartbeat().getSql(), is(SQL));
        assertThat(clusterConfiguration.getHeartbeat().getInterval(), is(INTERVAL));
        assertThat(clusterConfiguration.getHeartbeat().getRetryMaximum(), is(MAXIMUM));
        assertTrue(clusterConfiguration.getHeartbeat().isRetryEnable());
    }
    
    @Test
    public void assertSwapToYamlClusterConfiguration() {
        HeartbeatConfiguration heartBeatConfiguration = new HeartbeatConfiguration();
        heartBeatConfiguration.setSql(SQL);
        heartBeatConfiguration.setInterval(INTERVAL);
        heartBeatConfiguration.setRetryEnable(true);
        heartBeatConfiguration.setRetryMaximum(MAXIMUM);
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHeartbeat(heartBeatConfiguration);
        YamlClusterConfiguration yamlClusterConfiguration = new ClusterConfigurationYamlSwapper().swapToYamlConfiguration(clusterConfiguration);
        assertThat(yamlClusterConfiguration.getHeartbeat().getSql(), is(SQL));
        assertThat(yamlClusterConfiguration.getHeartbeat().getInterval(), is(INTERVAL));
        assertThat(yamlClusterConfiguration.getHeartbeat().getRetryMaximum(), is(MAXIMUM));
        assertTrue(yamlClusterConfiguration.getHeartbeat().isRetryEnable());
    }
}
