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
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * Abstract simple pipeline job.
 */
@Slf4j
public abstract class AbstractSimplePipelineJob extends AbstractPipelineJob implements SimpleJob {
    
    protected AbstractSimplePipelineJob(final String jobId) {
        super(jobId);
    }
    
    /**
     * Build pipeline job item context.
     * 
     * @param shardingContext sharding context
     * @return pipeline job item context
     */
    protected abstract PipelineJobItemContext buildPipelineJobItemContext(ShardingContext shardingContext);
    
    protected abstract PipelineTasksRunner buildPipelineTasksRunner(PipelineJobItemContext pipelineJobItemContext);
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}", jobId, shardingItem);
        if (isStopping()) {
            log.info("stopping true, ignore");
            return;
        }
        try {
            PipelineJobItemContext jobItemContext = buildPipelineJobItemContext(shardingContext);
            execute0(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            processFailed(new PipelineJobManager(getJobAPI()), jobId, shardingItem, ex);
            throw ex;
        }
    }
    
    private void execute0(final PipelineJobItemContext jobItemContext) {
        String jobId = jobItemContext.getJobId();
        int shardingItem = jobItemContext.getShardingItem();
        PipelineTasksRunner tasksRunner = buildPipelineTasksRunner(jobItemContext);
        if (!addTasksRunner(shardingItem, tasksRunner)) {
            return;
        }
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
        prepare(jobItemContext);
        log.info("start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        tasksRunner.start();
    }
    
    private void processFailed(final PipelineJobManager jobManager, final String jobId, final int shardingItem, final Exception ex) {
        log.error("job execution failed, {}-{}", jobId, shardingItem, ex);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        try {
            jobManager.stop(jobId);
        } catch (final PipelineJobNotFoundException ignored) {
        }
    }
}
