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
import org.apache.shardingsphere.cluster.configuration.config.HeartBeatConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlHeartBeatConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

public final class ClusterConfigurationYamlSwapperTest {
    
    private static final String SQL = "select 1";
    
    private static final Integer INTERVAL = 60;
    
    private static final Integer MAXIMUM = 3;
    
    @Test
    public void assertSwapToClusterConfiguration() {
        YamlHeartBeatConfiguration yamlHeartBeatConfiguration = new YamlHeartBeatConfiguration();
        yamlHeartBeatConfiguration.setSql(SQL);
        yamlHeartBeatConfiguration.setInterval(INTERVAL);
        yamlHeartBeatConfiguration.setRetryEnable(Boolean.TRUE);
        yamlHeartBeatConfiguration.setRetryMaximum(MAXIMUM);
    
        YamlClusterConfiguration yamlClusterConfiguration = new YamlClusterConfiguration();
        yamlClusterConfiguration.setHeartBeat(yamlHeartBeatConfiguration);
    
        ClusterConfiguration clusterConfiguration = new ClusterConfigurationYamlSwapper().swap(yamlClusterConfiguration);
        assertThat(clusterConfiguration.getHeartBeat().getSql(), is(SQL));
        assertThat(clusterConfiguration.getHeartBeat().getInterval(), is(INTERVAL));
        assertThat(clusterConfiguration.getHeartBeat().getRetryMaximum(), is(MAXIMUM));
        assertTrue(clusterConfiguration.getHeartBeat().getRetryEnable());
    }
    
    @Test
    public void assertSwapToYamlClusterConfiguration() {
        HeartBeatConfiguration heartBeatConfiguration = new HeartBeatConfiguration();
        heartBeatConfiguration.setSql(SQL);
        heartBeatConfiguration.setInterval(INTERVAL);
        heartBeatConfiguration.setRetryEnable(Boolean.TRUE);
        heartBeatConfiguration.setRetryMaximum(MAXIMUM);
    
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHeartBeat(heartBeatConfiguration);
    
        YamlClusterConfiguration yamlClusterConfiguration = new ClusterConfigurationYamlSwapper().swap(clusterConfiguration);
        assertThat(yamlClusterConfiguration.getHeartBeat().getSql(), is(SQL));
        assertThat(yamlClusterConfiguration.getHeartBeat().getInterval(), is(INTERVAL));
        assertThat(yamlClusterConfiguration.getHeartBeat().getRetryMaximum(), is(MAXIMUM));
        assertTrue(yamlClusterConfiguration.getHeartBeat().getRetryEnable());
    }
}
