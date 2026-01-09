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

import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.task.TaskExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class CDCIncrementalTaskTest {
    
    @Test
    void assertStartAndStopWithImporter() {
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        Dumper dumper = mock(Dumper.class);
        Importer importer = mock(Importer.class);
        IncrementalTaskProgress taskProgress = new IncrementalTaskProgress(mock(IngestPosition.class));
        CDCIncrementalTask task = new CDCIncrementalTask("task-id", executeEngine, dumper, importer, taskProgress);
        Collection<CompletableFuture<?>> actualFutures = task.start();
        assertThat(actualFutures.size(), is(2));
        assertTrue(taskProgress.getIncrementalTaskDelay().getLatestActiveTimeMillis() > 0L);
        verify(executeEngine).submit(eq(dumper), any(TaskExecuteCallback.class));
        verify(executeEngine).submit(eq(importer), any(TaskExecuteCallback.class));
        task.stop();
        verify(dumper).stop();
        verify(importer).stop();
    }
    
    @Test
    void assertStartAndStopWithoutImporter() {
        PipelineExecuteEngine executeEngine = mock(PipelineExecuteEngine.class);
        Dumper dumper = mock(Dumper.class);
        IncrementalTaskProgress taskProgress = new IncrementalTaskProgress(mock(IngestPosition.class));
        CDCIncrementalTask task = new CDCIncrementalTask("task-id", executeEngine, dumper, null, taskProgress);
        Collection<CompletableFuture<?>> actualFutures = task.start();
        assertThat(actualFutures.size(), is(1));
        assertTrue(taskProgress.getIncrementalTaskDelay().getLatestActiveTimeMillis() > 0L);
        verify(executeEngine).submit(eq(dumper), any(TaskExecuteCallback.class));
        verifyNoMoreInteractions(executeEngine);
        task.stop();
        verify(dumper).stop();
    }
}
