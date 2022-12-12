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

package org.apache.shardingsphere.data.pipeline.cdc.core.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.job.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CreateSubscriptionRequest.SubscriptionMode;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.List;

/**
 * CDC job preparer.
 */
@Slf4j
public final class CDCJobPreparer {
    
    private final CDCJobAPI jobAPI = CDCJobAPIFactory.getInstance();
    
    /**
     * Do prepare work.
     *
     * @param jobItemContext job item context
     */
    public void prepare(final CDCJobItemContext jobItemContext) {
        if (jobItemContext.isStopping()) {
            PipelineJobCenter.stop(jobItemContext.getJobId());
            return;
        }
        CDCJobConfiguration jobConfig = jobItemContext.getJobConfig();
        if (SubscriptionMode.FULL.name().equals(jobConfig.getSubscriptionMode())) {
            initInventoryTasks(jobItemContext);
        }
        jobAPI.persistJobItemProgress(jobItemContext);
    }
    
    private void initInventoryTasks(final CDCJobItemContext jobItemContext) {
        CDCTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        // TODO importer and channel requires a new implementation
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), inventoryDumperConfig, taskConfig.getImporterConfig());
        List<InventoryTask> allInventoryTasks = inventoryTaskSplitter.splitInventoryData(jobItemContext);
        jobItemContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final CDCJobItemContext jobItemContext) {
        // TODO to be implemented
    }
}
