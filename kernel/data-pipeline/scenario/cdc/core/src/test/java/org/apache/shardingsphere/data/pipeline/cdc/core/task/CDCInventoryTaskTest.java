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
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.task.TaskExecuteCallback;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CDCInventoryTaskTest {
    
    @Test
    void assertStartWhenPositionFinished() {
        PipelineExecuteEngine inventoryDumperExecuteEngine = mock(PipelineExecuteEngine.class);
        PipelineExecuteEngine inventoryImporterExecuteEngine = mock(PipelineExecuteEngine.class);
        CDCInventoryTask task = new CDCInventoryTask("task-id", inventoryDumperExecuteEngine, inventoryImporterExecuteEngine,
                mock(Dumper.class), mock(Importer.class), new AtomicReference<>(new IngestFinishedPosition()));
        Collection<CompletableFuture<?>> actualFutures = task.start();
        assertTrue(actualFutures.isEmpty());
        verifyNoInteractions(inventoryDumperExecuteEngine, inventoryImporterExecuteEngine);
    }
    
    @Test
    void assertStartAndStopWithImporterWhenPositionUnfinished() {
        PipelineExecuteEngine inventoryDumperExecuteEngine = mock(PipelineExecuteEngine.class);
        PipelineExecuteEngine inventoryImporterExecuteEngine = mock(PipelineExecuteEngine.class);
        Dumper dumper = mock(Dumper.class);
        Importer importer = mock(Importer.class);
        AtomicReference<IngestPosition> position = new AtomicReference<>(mock(IngestPosition.class));
        CDCInventoryTask task = new CDCInventoryTask("task-id", inventoryDumperExecuteEngine, inventoryImporterExecuteEngine, dumper, importer, position);
        Collection<CompletableFuture<?>> actualFutures = task.start();
        assertThat(actualFutures.size(), is(2));
        assertThat(task.getTaskProgress().getPosition(), is(position.get()));
        verify(inventoryDumperExecuteEngine).submit(eq(dumper), any(TaskExecuteCallback.class));
        verify(inventoryImporterExecuteEngine).submit(eq(importer), any(TaskExecuteCallback.class));
        task.stop();
        verify(dumper).stop();
        verify(importer).stop();
    }
    
    @Test
    void assertStartAndStopWithoutImporterWhenPositionUnfinished() {
        PipelineExecuteEngine inventoryDumperExecuteEngine = mock(PipelineExecuteEngine.class);
        PipelineExecuteEngine inventoryImporterExecuteEngine = mock(PipelineExecuteEngine.class);
        Dumper dumper = mock(Dumper.class);
        AtomicReference<IngestPosition> position = new AtomicReference<>(mock(IngestPosition.class));
        CDCInventoryTask task = new CDCInventoryTask("task-id", inventoryDumperExecuteEngine, inventoryImporterExecuteEngine, dumper, null, position);
        Collection<CompletableFuture<?>> actualFutures = task.start();
        assertThat(actualFutures.size(), is(1));
        assertThat(task.getTaskProgress().getPosition(), is(position.get()));
        verify(inventoryDumperExecuteEngine).submit(eq(dumper), any(TaskExecuteCallback.class));
        verifyNoInteractions(inventoryImporterExecuteEngine);
        task.stop();
        verify(dumper).stop();
    }
}
