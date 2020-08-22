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

import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.detect.HeartbeatHandler;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterHeartbeatInstanceTest {
    
    private static final ClusterHeartbeatInstance INSTANCE = ClusterHeartbeatInstance.getInstance();
    
    @Mock
    private RegistryCenter registryCenter;
    
    @Mock
    private HeartbeatHandler heartbeatHandler;
    
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
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Test
    public void assertDetect() {
        FieldSetter.setField(INSTANCE, ClusterHeartbeatInstance.class.getDeclaredField("registryCenter"), registryCenter);
        FieldSetter.setField(INSTANCE, ClusterHeartbeatInstance.class.getDeclaredField("heartbeatHandler"), heartbeatHandler);
        when(registryCenter.loadDisabledDataSources()).thenReturn(Arrays.asList("logic_db.ds_0"));
        INSTANCE.detect(new HashMap<>());
        verify(heartbeatHandler).handle(anyMap(), anyCollection());
    }
    
    @After
    public void close() {
        INSTANCE.close();
    }
}
