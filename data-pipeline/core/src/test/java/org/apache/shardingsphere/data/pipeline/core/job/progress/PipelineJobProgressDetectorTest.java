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

package org.apache.shardingsphere.data.pipeline.core.job.progress;

import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PipelineJobProgressDetectorTest {
    
    @Test
    void assertAllInventoryTasksAreFinishedWhenCollectionIsEmpty() {
        assertTrue(PipelineJobProgressDetector.isAllInventoryTasksFinished(Collections.emptyList()));
    }
    
    @Test
    void assertAllInventoryTasksAreFinishedWhenNotAllTasksAreFinished() {
        AtomicReference<IngestPosition> finishedPosition = new AtomicReference<>(new IngestFinishedPosition());
        AtomicReference<IngestPosition> unfinishedPosition = new AtomicReference<>(new IngestPlaceholderPosition());
        InventoryTask actualTask1 = new InventoryTask("foo_id_1", mock(PipelineExecuteEngine.class), mock(PipelineExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        InventoryTask actualTask2 = new InventoryTask("foo_id_2", mock(PipelineExecuteEngine.class), mock(PipelineExecuteEngine.class), mock(Dumper.class), mock(Importer.class), unfinishedPosition);
        assertFalse(PipelineJobProgressDetector.isAllInventoryTasksFinished(Arrays.asList(actualTask1, actualTask2)));
    }
    
    @Test
    void assertAllInventoryTasksAreFinished() {
        AtomicReference<IngestPosition> finishedPosition = new AtomicReference<>(new IngestFinishedPosition());
        InventoryTask actualTask1 = new InventoryTask("foo_id_1", mock(PipelineExecuteEngine.class), mock(PipelineExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        InventoryTask actualTask2 = new InventoryTask("foo_id_2", mock(PipelineExecuteEngine.class), mock(PipelineExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        assertTrue(PipelineJobProgressDetector.isAllInventoryTasksFinished(Arrays.asList(actualTask1, actualTask2)));
    }
    
    @Test
    void assertIsInventoryFinishedWhenCollectionElementIsNull() {
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, Collections.singleton(null)));
    }
    
    @Test
    void assertIsInventoryFinishedWhenJobCountDoesNotMatchJobItemProgresses() {
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(2, Collections.singleton(transmissionJobItemProgress)));
    }
    
    @Test
    void assertIsInventoryFinishedWhenInventoryTaskProgressHasEmptyMap() {
        JobItemInventoryTasksProgress jobItemInventoryTasksProgress = new JobItemInventoryTasksProgress(Collections.emptyMap());
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(jobItemInventoryTasksProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, Collections.singleton(transmissionJobItemProgress)));
    }
    
    @Test
    void assertIsInventoryFinishedWhenNotAllInventoryTasksCompleted() {
        JobItemInventoryTasksProgress inventoryTasksProgress = new JobItemInventoryTasksProgress(Collections.singletonMap("TEST", new InventoryTaskProgress(new IngestPlaceholderPosition())));
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(inventoryTasksProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, Collections.singleton(transmissionJobItemProgress)));
    }
    
    @Test
    void assertIsInventoryFinished() {
        JobItemInventoryTasksProgress inventoryTasksProgress = new JobItemInventoryTasksProgress(Collections.singletonMap("TEST", new InventoryTaskProgress(new IngestFinishedPosition())));
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(inventoryTasksProgress);
        assertTrue(PipelineJobProgressDetector.isInventoryFinished(1, Collections.singleton(transmissionJobItemProgress)));
    }
}
