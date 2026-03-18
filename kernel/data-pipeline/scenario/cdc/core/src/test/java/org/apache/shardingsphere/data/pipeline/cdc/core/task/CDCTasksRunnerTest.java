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

package org.apache.shardingsphere.data.pipeline.cdc.core.task;

import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CDCTasksRunnerTest {
    
    @Test
    void assertStartAndStop() {
        PipelineTask inventoryTask = mock(PipelineTask.class);
        PipelineTask incrementalTask = mock(PipelineTask.class);
        TransmissionJobItemContext jobItemContext = mock(TransmissionJobItemContext.class);
        when(jobItemContext.getInventoryTasks()).thenReturn(Collections.singleton(inventoryTask));
        when(jobItemContext.getIncrementalTasks()).thenReturn(Collections.singleton(incrementalTask));
        CDCTasksRunner runner = new CDCTasksRunner(jobItemContext);
        runner.start();
        runner.stop();
        verify(jobItemContext).setStopping(true);
        verify(inventoryTask).stop();
        verify(incrementalTask).stop();
    }
}
