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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSimplePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context.ConsistencyCheckJobItemContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.task.ConsistencyCheckTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Optional;

/**
 * Consistency check job.
 */
@Slf4j
public final class ConsistencyCheckJob extends AbstractSimplePipelineJob {
    
    public ConsistencyCheckJob(final String jobId) {
        super(jobId);
    }
    
    @Override
    public ConsistencyCheckJobItemContext buildPipelineJobItemContext(final ShardingContext shardingContext) {
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        ConsistencyCheckJobAPI jobAPI = (ConsistencyCheckJobAPI) getJobAPI();
        Optional<ConsistencyCheckJobItemProgress> jobItemProgress = jobAPI.getJobItemProgress(jobConfig.getJobId(), shardingContext.getShardingItem());
        return new ConsistencyCheckJobItemContext(jobConfig, shardingContext.getShardingItem(), JobStatus.RUNNING, jobItemProgress.orElse(null));
    }
    
    @Override
    protected PipelineTasksRunner buildPipelineTasksRunner(final PipelineJobItemContext pipelineJobItemContext) {
        return new ConsistencyCheckTasksRunner((ConsistencyCheckJobItemContext) pipelineJobItemContext);
    }
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) {
    }
    
    @Override
    protected void doClean() {
    }
}
