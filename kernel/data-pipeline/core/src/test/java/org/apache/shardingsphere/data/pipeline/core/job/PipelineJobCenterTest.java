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

package org.apache.shardingsphere.data.pipeline.core.job;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineJobCenterTest {
    
    @Test
    void testPipelineJobCenter() {
        PipelineJob pipelineJob = mock(PipelineJob.class);
        PipelineJobCenter.addJob("Job1", pipelineJob);
        
        assertTrue(PipelineJobCenter.isJobExisting("Job1"));
        assertFalse(PipelineJobCenter.isJobExisting("Job2"));
        assertNotNull(PipelineJobCenter.getJob("Job1"));
        assertEquals(pipelineJob, PipelineJobCenter.getJob("Job1"));
        assertNull(PipelineJobCenter.getJob("Job2"));
        
        PipelineJob pipelineJob1 = mock(PipelineJob.class);
        PipelineJobCenter.addJob("Job3", pipelineJob1);
        assertNotNull(PipelineJobCenter.getJob("Job3"));
        assertEquals(pipelineJob1, PipelineJobCenter.getJob("Job3"));
        PipelineJobCenter.stop("Job3");
    }
    
    @Test
    void getJobItemContext() {
        PipelineJob pipelineJob = mock(PipelineJob.class);
        PipelineTasksRunner pipelineTasksRunner = mock(PipelineTasksRunner.class);
        PipelineJobItemContext pipelineJobItemContext = mock(PipelineJobItemContext.class);
        Optional<PipelineJobItemContext> pipelineJobItemContext1 = Optional.ofNullable(pipelineJobItemContext);
        when(pipelineJob.getTasksRunner(anyInt())).thenReturn(Optional.of(pipelineTasksRunner));
        when(pipelineTasksRunner.getJobItemContext()).thenReturn(pipelineJobItemContext);
        PipelineJobCenter.addJob("Job1", pipelineJob);
        Optional<PipelineJobItemContext> result = PipelineJobCenter.getJobItemContext("Job1", 1);
        Optional<PipelineJobItemContext> result1 = PipelineJobCenter.getJobItemContext("Job2", 1);
        assertTrue(result.isPresent());
        assertEquals(pipelineJobItemContext1, result);
        assertFalse(result1.isPresent());
        assertNotNull(result1);
        PipelineJobCenter.stop("Job1");
    }
    
    @Test
    void getShardingItems() {
        PipelineJob pipelineJob = mock(PipelineJob.class);
        PipelineJobCenter.addJob("Job1", pipelineJob);
        when(pipelineJob.getShardingItems()).thenReturn(Arrays.asList(1, 2, 3));
        Collection<Integer> shardingItems = pipelineJob.getShardingItems();
        
        Assertions.assertFalse(shardingItems.isEmpty());
        Assertions.assertEquals(Arrays.asList(1, 2, 3), PipelineJobCenter.getShardingItems("Job1"));
        assertEquals(Collections.EMPTY_LIST, PipelineJobCenter.getShardingItems("Job2"));
        PipelineJobCenter.stop("Job1");
    }
}
