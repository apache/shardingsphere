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
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * Consistency check job.
 */
@Slf4j
public final class ConsistencyCheckJob extends AbstractPipelineJob implements SimpleJob, PipelineJob {
    
    private final ConsistencyCheckJobAPI jobAPI = ConsistencyCheckJobAPIFactory.getInstance();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String checkJobId = shardingContext.getJobName();
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}", checkJobId, shardingItem);
        if (isStopping()) {
            log.info("stopping true, ignore");
            return;
        }
        setJobId(checkJobId);
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        ConsistencyCheckJobItemContext jobItemContext = new ConsistencyCheckJobItemContext(jobConfig, shardingItem, JobStatus.RUNNING);
        if (containsTasksRunner(shardingItem)) {
            log.warn("tasksRunnerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start tasks runner, jobId={}, shardingItem={}", getJobId(), shardingItem);
        jobAPI.cleanJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        ConsistencyCheckTasksRunner tasksRunner = new ConsistencyCheckTasksRunner(jobItemContext);
        tasksRunner.start();
        addTasksRunner(shardingItem, tasksRunner);
    }
    
    @Override
    public void stop() {
        setStopping(true);
        if (null != getJobBootstrap()) {
            getJobBootstrap().shutdown();
        }
        if (null == getJobId()) {
            log.info("stop consistency check job, jobId is null, ignore");
            return;
        }
        for (PipelineTasksRunner each : getTasksRunners()) {
            each.stop();
        }
        clearTasksRunner();
    }
}
