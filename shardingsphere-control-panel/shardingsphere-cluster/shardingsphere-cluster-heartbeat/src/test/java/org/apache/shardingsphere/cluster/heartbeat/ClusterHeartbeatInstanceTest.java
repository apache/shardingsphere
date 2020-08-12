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

package org.apache.shardingsphere.cluster.heartbeat;

import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterHeartbeatInstanceTest {
    
    private static final ClusterHeartbeatInstance INSTANCE = ClusterHeartbeatInstance.getInstance();
    
    @Before
    public void setUp() {
        HeartbeatConfiguration heartBeatConfig = new HeartbeatConfiguration();
        heartBeatConfig.setSql("select 1");
        heartBeatConfig.setInterval(60);
        heartBeatConfig.setRetryEnable(true);
        heartBeatConfig.setRetryMaximum(3);
        INSTANCE.init(heartBeatConfig);
    }
    
    @Test
    public void assertGetInstance() {
        assertNotNull(ClusterHeartbeatInstance.getInstance());
    }
    
    @After
    public void close() {
        INSTANCE.close();
    }
}
