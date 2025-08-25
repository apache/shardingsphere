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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Transmission job item progress.
 */
@NoArgsConstructor
@Getter
@Setter
public final class TransmissionJobItemProgress implements PipelineJobItemProgress {
    
    private DatabaseType sourceDatabaseType;
    
    private String dataSourceName;
    
    private JobItemInventoryTasksProgress inventory;
    
    private JobItemIncrementalTasksProgress incremental;
    
    private long inventoryRecordsCount;
    
    private long processedRecordsCount;
    
    private boolean active;
    
    private JobStatus status = JobStatus.RUNNING;
    
    public TransmissionJobItemProgress(final TransmissionJobItemContext context) {
        sourceDatabaseType = context.getJobConfig().getSourceDatabaseType();
        dataSourceName = context.getDataSourceName();
        inventory = getInventoryTasksProgress(context.getInventoryTasks());
        incremental = getIncrementalTasksProgress(context.getIncrementalTasks());
        inventoryRecordsCount = context.getInventoryRecordsCount();
        processedRecordsCount = context.getProcessedRecordsCount();
        status = context.getStatus();
    }
    
    private JobItemIncrementalTasksProgress getIncrementalTasksProgress(final Collection<PipelineTask> incrementalTasks) {
        return new JobItemIncrementalTasksProgress(incrementalTasks.isEmpty() ? null : (IncrementalTaskProgress) incrementalTasks.iterator().next().getTaskProgress());
    }
    
    private JobItemInventoryTasksProgress getInventoryTasksProgress(final Collection<PipelineTask> inventoryTasks) {
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new HashMap<>(inventoryTasks.size(), 1F);
        for (PipelineTask each : inventoryTasks) {
            inventoryTaskProgressMap.put(each.getTaskId(), (InventoryTaskProgress) each.getTaskProgress());
        }
        return new JobItemInventoryTasksProgress(inventoryTaskProgressMap);
    }
}
