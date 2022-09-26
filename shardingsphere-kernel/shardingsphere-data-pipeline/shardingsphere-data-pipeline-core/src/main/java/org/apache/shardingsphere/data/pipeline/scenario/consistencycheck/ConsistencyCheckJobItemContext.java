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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Consistency check job item context.
 */
@Getter
@Setter
@Slf4j
public final class ConsistencyCheckJobItemContext implements InventoryIncrementalJobItemContext {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private String dataSourceName;
    
    private volatile boolean stopping;
    
    private volatile JobStatus status;
    
    private final Collection<InventoryTask> inventoryTasks = new LinkedList<>();
    
    private final Collection<IncrementalTask> incrementalTasks = new LinkedList<>();
    
    private final AtomicLong processedRecordsCount = new AtomicLong(0);
    
    private final ConsistencyCheckJobConfiguration jobConfig;
    
    public ConsistencyCheckJobItemContext(final ConsistencyCheckJobConfiguration jobConfig, final int shardingItem, final JobStatus status) {
        this.jobConfig = jobConfig;
        jobId = jobConfig.getJobId();
        this.shardingItem = shardingItem;
        this.status = status;
    }
    
    @Override
    public void onProgressUpdated(final PipelineJobProgressUpdatedParameter parameter) {
        // TODO not support yet
    }
    
    @Override
    public InventoryIncrementalProcessContext getJobProcessContext() {
        return null;
    }
    
    @Override
    public long getProcessedRecordsCount() {
        return processedRecordsCount.get();
    }
}
