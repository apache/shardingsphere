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

package org.apache.shardingsphere.data.pipeline.core.task;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskDelay;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineTaskUtilsTest {
    
    @Test
    void assertGenerateInventoryTaskId() {
        InventoryDumperContext dumperContext = new InventoryDumperContext(new DumperCommonContext("foo_ds", null, null, null));
        dumperContext.setActualTableName("foo_actual_tbl");
        dumperContext.setShardingItem(1);
        assertThat(PipelineTaskUtils.generateInventoryTaskId(dumperContext), is("foo_ds.foo_actual_tbl#1"));
    }
    
    @Test
    void assertCreateIncrementalTaskProgressWithNullInitProgress() {
        IncrementalTaskProgress actual = PipelineTaskUtils.createIncrementalTaskProgress(mock(IngestPosition.class), null);
        assertThat(actual.getIncrementalTaskDelay().getLastEventTimestamps(), is(0L));
    }
    
    @Test
    void assertCreateIncrementalTaskProgressWithNullIncremental() {
        IncrementalTaskProgress actual = PipelineTaskUtils.createIncrementalTaskProgress(mock(IngestPosition.class), mock(TransmissionJobItemProgress.class));
        assertThat(actual.getIncrementalTaskDelay().getLastEventTimestamps(), is(0L));
    }
    
    @Test
    void assertCreateIncrementalTaskProgress() {
        IncrementalTaskProgress actual = PipelineTaskUtils.createIncrementalTaskProgress(mock(IngestPosition.class), mockTransmissionJobItemProgress());
        assertThat(actual.getIncrementalTaskDelay().getLastEventTimestamps(), is(1L));
    }
    
    private static TransmissionJobItemProgress mockTransmissionJobItemProgress() {
        TransmissionJobItemProgress result = mock(TransmissionJobItemProgress.class);
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress(null);
        IncrementalTaskDelay taskDelay = new IncrementalTaskDelay();
        taskDelay.setLastEventTimestamps(1L);
        incrementalTaskProgress.setIncrementalTaskDelay(taskDelay);
        JobItemIncrementalTasksProgress itemIncrementalTasksProgress = new JobItemIncrementalTasksProgress(incrementalTaskProgress);
        when(result.getIncremental()).thenReturn(itemIncrementalTasksProgress);
        return result;
    }
}
