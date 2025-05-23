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

package org.apache.shardingsphere.schedule.core.job.statistics.collect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatisticsCollectJobWorkerTest {
    
    private StatisticsCollectJobWorker jobWorker;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        jobWorker = new StatisticsCollectJobWorker();
    }
    
    @Test
    void assertInitializeTwice() {
        jobWorker.initialize(contextManager);
        jobWorker.initialize(contextManager);
        verify(contextManager.getComputeNodeInstanceContext()).getModeConfiguration();
    }
    
    @Test
    void assertInitializeWithNotZooKeeperRepository() {
        jobWorker.initialize(contextManager);
        assertNull(getScheduleJobBootstrap());
    }
    
    @Test
    void assertDestroy() {
        jobWorker.destroy();
        jobWorker.destroy();
        assertNull(getScheduleJobBootstrap());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ScheduleJobBootstrap getScheduleJobBootstrap() {
        return (ScheduleJobBootstrap) Plugins.getMemberAccessor().get(StatisticsCollectJobWorker.class.getDeclaredField("scheduleJobBootstrap"), StatisticsCollectJobWorker.class);
    }
}
