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

import org.apache.shardingsphere.data.pipeline.core.job.AbstractSeparablePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.task.ConsistencyCheckTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Optional;

/**
 * Consistency check job.
 */
public final class ConsistencyCheckJob extends AbstractSeparablePipelineJob<ConsistencyCheckJobItemContext> {
    
    public ConsistencyCheckJob(final String jobId) {
        super(jobId);
    }
    
    @Override
    public ConsistencyCheckJobItemContext buildJobItemContext(final ShardingContext shardingContext) {
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        PipelineJobItemManager<ConsistencyCheckJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new ConsistencyCheckJobType().getYamlJobItemProgressSwapper());
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobConfig.getJobId(), shardingContext.getShardingItem());
        return new ConsistencyCheckJobItemContext(jobConfig, shardingContext.getShardingItem(), JobStatus.RUNNING, jobItemProgress.orElse(null));
    }
    
    @Override
    protected PipelineTasksRunner buildTasksRunner(final ConsistencyCheckJobItemContext jobItemContext) {
        return new ConsistencyCheckTasksRunner(jobItemContext);
    }
    
    @Override
    protected void doPrepare(final ConsistencyCheckJobItemContext jobItemContext) {
    }
    
    @Override
    protected void clean() {
    }
}
