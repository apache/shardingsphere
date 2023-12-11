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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.sql.SQLException;

/**
 * Abstract separable pipeline job.
 * 
 * @param <T> type of pipeline job item context
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class AbstractSeparablePipelineJob<T extends PipelineJobItemContext> implements PipelineJob {
    
    private final PipelineJobRunnerManager jobRunnerManager;
    
    protected AbstractSeparablePipelineJob() {
        this(new PipelineJobRunnerManager());
    }
    
    @Override
    public final void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}", jobId, shardingItem);
        if (jobRunnerManager.isStopping()) {
            log.info("Stopping true, ignore");
            return;
        }
        PipelineJobType jobType = PipelineJobIdUtils.parseJobType(jobId);
        PipelineJobConfigurationManager jobConfigManager = new PipelineJobConfigurationManager(jobType);
        PipelineJobConfiguration jobConfig = jobConfigManager.getJobConfiguration(jobId);
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
        TransmissionJobItemProgress jobItemProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem).orElse(null);
        try {
            execute(buildJobItemContext(jobConfig, shardingItem, jobItemProgress));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            processFailed(jobId, shardingItem, ex);
            throw ex;
        }
    }
    
    private void execute(final T jobItemContext) {
        String jobId = jobItemContext.getJobId();
        int shardingItem = jobItemContext.getShardingItem();
        PipelineTasksRunner tasksRunner = buildTasksRunner(jobItemContext);
        if (!jobRunnerManager.addTasksRunner(shardingItem, tasksRunner)) {
            return;
        }
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
        prepare(jobItemContext);
        log.info("Start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        tasksRunner.start();
    }
    
    protected abstract T buildJobItemContext(PipelineJobConfiguration jobConfig, int shardingItem, PipelineJobItemProgress jobItemProgress);
    
    protected abstract PipelineTasksRunner buildTasksRunner(T jobItemContext);
    
    protected final void prepare(final T jobItemContext) {
        try {
            doPrepare(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            throw new PipelineInternalException(ex);
        }
    }
    
    protected abstract void doPrepare(T jobItemContext) throws SQLException;
    
    private void processFailed(final String jobId, final int shardingItem, final Exception ex) {
        log.error("Job execution failed, {}-{}", jobId, shardingItem, ex);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
        try {
            new PipelineJobManager(PipelineJobIdUtils.parseJobType(jobId)).stop(jobId);
        } catch (final PipelineJobNotFoundException ignored) {
        }
    }
}
