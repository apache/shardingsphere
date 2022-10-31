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

package org.apache.shardingsphere.data.pipeline.core.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * Abstract simple pipeline job.
 */
@Slf4j
public abstract class AbstractSimplePipelineJob extends AbstractPipelineJob implements SimpleJob {
    
    protected abstract PipelineJobItemContext buildPipelineJobItemContext(ShardingContext shardingContext);
    
    protected abstract PipelineTasksRunner buildPipelineTasksRunner(PipelineJobItemContext pipelineJobItemContext);
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}", shardingContext.getJobName(), shardingItem);
        if (isStopping()) {
            log.info("stopping true, ignore");
            return;
        }
        setJobId(shardingContext.getJobName());
        PipelineJobItemContext jobItemContext = buildPipelineJobItemContext(shardingContext);
        if (containsTasksRunner(shardingItem)) {
            log.warn("tasksRunnerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        PipelineTasksRunner tasksRunner = buildPipelineTasksRunner(jobItemContext);
        getJobAPI().cleanJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        runInBackground(() -> {
            prepare(jobItemContext);
            tasksRunner.start();
            log.info("start tasks runner, jobId={}, shardingItem={}", getJobId(), shardingItem);
        });
        addTasksRunner(shardingItem, tasksRunner);
    }
}
