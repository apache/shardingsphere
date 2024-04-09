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

import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PipelineJobProgressDetectorTest {
    
    @Test
    void assertAllInventoryTasksAreFinishedWhenCollectionIsEmpty() {
        assertTrue(PipelineJobProgressDetector.isAllInventoryTasksFinished(new ArrayList<>()));
    }
    
    @Test
    void assertAllInventoryTasksAreFinishedWhenNotAllTasksAreFinished() {
        AtomicReference<IngestPosition> finishedPosition = new AtomicReference<>(new IngestFinishedPosition());
        AtomicReference<IngestPosition> unfinishedPosition = new AtomicReference<>(new IngestPlaceholderPosition());
        InventoryTask actualTask1 = new InventoryTask("foo_id_1", mock(ExecuteEngine.class), mock(ExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        InventoryTask actualTask2 = new InventoryTask("foo_id_2", mock(ExecuteEngine.class), mock(ExecuteEngine.class), mock(Dumper.class), mock(Importer.class), unfinishedPosition);
        Collection<PipelineTask> inventoryTaskArrayList = new ArrayList<>();
        inventoryTaskArrayList.add(actualTask1);
        inventoryTaskArrayList.add(actualTask2);
        assertFalse(PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTaskArrayList));
    }
    
    @Test
    void assertAllInventoryTasksAreFinished() {
        AtomicReference<IngestPosition> finishedPosition = new AtomicReference<>(new IngestFinishedPosition());
        InventoryTask actualTask1 = new InventoryTask("foo_id_1", mock(ExecuteEngine.class), mock(ExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        InventoryTask actualTask2 = new InventoryTask("foo_id_2", mock(ExecuteEngine.class), mock(ExecuteEngine.class), mock(Dumper.class), mock(Importer.class), finishedPosition);
        Collection<PipelineTask> inventoryTaskArrayList = new ArrayList<>();
        inventoryTaskArrayList.add(actualTask1);
        inventoryTaskArrayList.add(actualTask2);
        assertTrue(PipelineJobProgressDetector.isAllInventoryTasksFinished(inventoryTaskArrayList));
    }
    
    @Test
    void assertIsInventoryFinishedWhenCollectionElementIsNull() {
        TransmissionJobItemProgress jobItemProgress = null;
        Collection<TransmissionJobItemProgress> jobItemProgresses = new ArrayList<>();
        jobItemProgresses.add(jobItemProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, jobItemProgresses));
    }
    
    @Test
    void assertIsInventoryFinishedWhenJobCountDoesNotMatchJobItemProgresses() {
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        List<TransmissionJobItemProgress> jobItemProgresses = new ArrayList<>();
        jobItemProgresses.add(transmissionJobItemProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(2, jobItemProgresses));
    }
    
    @Test
    void assertIsInventoryFinishedWhenInventoryTaskProgressHasEmptyMap() {
        JobItemInventoryTasksProgress jobItemInventoryTasksProgress = new JobItemInventoryTasksProgress(new HashMap<>());
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(jobItemInventoryTasksProgress);
        List<TransmissionJobItemProgress> jobItemProgresses = new ArrayList<>();
        jobItemProgresses.add(transmissionJobItemProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, jobItemProgresses));
    }
    
    @Test
    void assertIsInventoryFinishedWhenNotAllInventoryTasksCompleted() {
        Map<String, InventoryTaskProgress> progresses = new HashMap<>();
        progresses.put("TEST", new InventoryTaskProgress(new IngestPlaceholderPosition()));
        JobItemInventoryTasksProgress inventoryTasksProgress = new JobItemInventoryTasksProgress(progresses);
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(inventoryTasksProgress);
        List<TransmissionJobItemProgress> jobItemProgresses = new ArrayList<>();
        jobItemProgresses.add(transmissionJobItemProgress);
        assertFalse(PipelineJobProgressDetector.isInventoryFinished(1, jobItemProgresses));
    }
    
    @Test
    void assertIsInventoryFinished() {
        Map<String, InventoryTaskProgress> progresses = new HashMap<>();
        progresses.put("TEST", new InventoryTaskProgress(new IngestFinishedPosition()));
        JobItemInventoryTasksProgress inventoryTasksProgress = new JobItemInventoryTasksProgress(progresses);
        TransmissionJobItemProgress transmissionJobItemProgress = new TransmissionJobItemProgress();
        transmissionJobItemProgress.setInventory(inventoryTasksProgress);
        List<TransmissionJobItemProgress> jobItemProgresses = new ArrayList<>();
        jobItemProgresses.add(transmissionJobItemProgress);
        assertTrue(PipelineJobProgressDetector.isInventoryFinished(1, jobItemProgresses));
    }
}
