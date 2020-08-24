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

package org.apache.shardingsphere.cluster.heartbeat.task;

import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartbeatDetectNoticeEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public final class HeartbeatTaskManagerTest {
    
    @Mock
    private ScheduledExecutorService executorService;
    
    private HeartbeatTaskManager heartbeatTaskManager;
    
    @Before
    @SneakyThrows({NoSuchFieldException.class, SecurityException.class})
    public void init() {
        heartbeatTaskManager = new HeartbeatTaskManager(60);
        FieldSetter.setField(heartbeatTaskManager, heartbeatTaskManager.getClass().getDeclaredField("executorService"), executorService);
    }
    
    @Test
    public void start() {
        HeartbeatTask heartbeatTask = new HeartbeatTask(new HeartbeatDetectNoticeEvent());
        doAnswer(invocationOnMock -> {
            heartbeatTask.run();
            return null;
        }).when(executorService).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
        heartbeatTaskManager.start(heartbeatTask);
        verify(executorService).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }
    
    @After
    public void close() {
        heartbeatTaskManager.close();
    }
}
