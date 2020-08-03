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

package org.apache.shardingsphere.cluster.facade;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.facade.init.ClusterInitFacade;
import org.apache.shardingsphere.control.panel.spi.ControlPanelConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterInitFacadeTest {
    
    @Before
    public void setUp() {
        HeartbeatConfiguration heartBeatConfig = new HeartbeatConfiguration();
        heartBeatConfig.setSql("select 1");
        heartBeatConfig.setInterval(60);
        heartBeatConfig.setRetryEnable(true);
        heartBeatConfig.setRetryMaximum(3);
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHeartbeat(heartBeatConfig);
        List<ControlPanelConfiguration> controlPanelConfigs = new LinkedList<>();
        controlPanelConfigs.add(clusterConfiguration);
        new ControlPanelFacadeEngine().init(controlPanelConfigs);
    }
    
    @Test
    public void assertInit() {
        assertTrue(ClusterInitFacade.isEnabled());
    }
    
    @Test
    public void assertStop() {
        ClusterInitFacade.stop();
        assertFalse(ClusterInitFacade.isEnabled());
    }
    
    @Test
    public void assertRestart() {
        HeartbeatConfiguration heartBeatConfiguration = new HeartbeatConfiguration();
        heartBeatConfiguration.setSql("select 2");
        heartBeatConfiguration.setInterval(30);
        heartBeatConfiguration.setRetryEnable(false);
        heartBeatConfiguration.setRetryMaximum(3);
        ClusterConfiguration clusterConfiguration = new ClusterConfiguration();
        clusterConfiguration.setHeartbeat(heartBeatConfiguration);
        ClusterInitFacade.restart(clusterConfiguration);
        assertTrue(ClusterInitFacade.isEnabled());
    }
    
    @Test
    public void assertEnable() {
        ClusterInitFacade.enable(false);
        assertFalse(ClusterInitFacade.isEnabled());
    }
    
    @After
    public void stop() {
        ClusterInitFacade.stop();
    }
}
