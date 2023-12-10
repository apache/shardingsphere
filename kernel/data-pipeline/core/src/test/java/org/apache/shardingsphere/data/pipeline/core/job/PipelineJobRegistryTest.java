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

import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineJobRegistryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PipelineJob job;
    
    @BeforeEach
    void setUp() {
        PipelineJobRegistry.add("foo_job", job);
    }
    
    @AfterEach
    void reset() {
        PipelineJobRegistry.stop("foo_job");
    }
    
    @Test
    void assertAdd() {
        assertFalse(PipelineJobRegistry.isExisting("bar_job"));
        PipelineJobRegistry.add("bar_job", mock(PipelineJob.class));
        assertTrue(PipelineJobRegistry.isExisting("bar_job"));
    }
    
    @Test
    void assertIsExisting() {
        assertTrue(PipelineJobRegistry.isExisting("foo_job"));
    }
    
    @Test
    void assertGet() {
        assertThat(PipelineJobRegistry.get("foo_job"), is(job));
    }
    
    @Test
    void assertStop() {
        PipelineJobRegistry.stop("foo_job");
        verify(job.getJobRunnerManager()).stop();
        assertFalse(PipelineJobRegistry.isExisting("foo_job"));
    }
    
    @Test
    void assertGetExistedItemContext() {
        PipelineJobItemContext jobItemContext = mock(PipelineJobItemContext.class);
        PipelineTasksRunner tasksRunner = mock(PipelineTasksRunner.class);
        when(tasksRunner.getJobItemContext()).thenReturn(jobItemContext);
        when(job.getJobRunnerManager().getTasksRunner(anyInt())).thenReturn(Optional.of(tasksRunner));
        Optional<PipelineJobItemContext> actual = PipelineJobRegistry.getItemContext("foo_job", 1);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(jobItemContext));
    }
    
    @Test
    void assertGetNotExistedItemContext() {
        assertThat(PipelineJobRegistry.getItemContext("bar_job", 1), is(Optional.empty()));
    }
    
    @Test
    void assertGetExistedShardingItems() {
        when(job.getJobRunnerManager().getShardingItems()).thenReturn(Arrays.asList(1, 2, 3));
        assertThat(PipelineJobRegistry.getShardingItems("foo_job"), is(Arrays.asList(1, 2, 3)));
    }
    
    @Test
    void assertGetNotExistedShardingItems() {
        assertThat(PipelineJobRegistry.getShardingItems("bar_job"), is(Collections.emptyList()));
    }
}
