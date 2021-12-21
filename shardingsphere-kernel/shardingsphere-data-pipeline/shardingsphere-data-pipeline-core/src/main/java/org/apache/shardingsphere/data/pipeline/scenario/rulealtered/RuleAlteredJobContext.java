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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.LinkedList;
import java.util.List;

/**
 * Rule altered job context.
 */
@Getter
@Setter
// TODO extract JobContext
public final class RuleAlteredJobContext {
    
    private final long jobId;
    
    private final int shardingItem;
    
    private JobStatus status = JobStatus.RUNNING;
    
    private JobProgress initProgress;
    
    private final List<TaskConfiguration> taskConfigs;
    
    private final List<InventoryTask> inventoryTasks = new LinkedList<>();
    
    private final List<IncrementalTask> incrementalTasks = new LinkedList<>();
    
    private final JobConfiguration jobConfig;
    
    private final RuleAlteredContext ruleAlteredContext;
    
    private RuleAlteredJobPreparer jobPreparer;
    
    public RuleAlteredJobContext(final JobConfiguration jobConfig) {
        ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        this.jobConfig = jobConfig;
        jobConfig.buildHandleConfig();
        jobId = jobConfig.getHandleConfig().getJobId();
        shardingItem = jobConfig.getHandleConfig().getJobShardingItem();
        taskConfigs = jobConfig.buildTaskConfigs();
    }
}
