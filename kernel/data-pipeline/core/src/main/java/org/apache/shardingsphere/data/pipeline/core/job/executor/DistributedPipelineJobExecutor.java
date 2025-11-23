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

package org.apache.shardingsphere.data.pipeline.core.job.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.engine.PipelineJobRunnerManager;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.sql.SQLException;

/**
 * Distributed pipeline job executor.
 */
@RequiredArgsConstructor
@Slf4j
public final class DistributedPipelineJobExecutor {
    
    @SuppressWarnings("rawtypes")
    private final DistributedPipelineJobExecutorCallback callback;
    
    @Getter
    private final PipelineJobRunnerManager jobRunnerManager = new PipelineJobRunnerManager();
    
    /**
     * Execute job.
     *
     * @param shardingContext sharding context
     */
    @SuppressWarnings("unchecked")
    public void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}.", jobId, shardingItem);
        if (jobRunnerManager.isStopping()) {
            log.info("Job is stopping, ignore.");
            return;
        }
        PipelineJobType<?> jobType = PipelineJobIdUtils.parseJobType(jobId);
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        PipelineJobConfiguration jobConfig = jobType.getOption().getYamlJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        PipelineJobItemManager<PipelineJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
        PipelineJobItemProgress jobItemProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem).orElse(null);
        TransmissionProcessContext jobProcessContext = createTransmissionProcessContext(jobId, jobType, contextKey);
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey);
        boolean started = false;
        try {
            started = execute(callback.buildJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext, jobRunnerManager.getDataSourceManager()), governanceFacade);
            if (started) {
                PipelineJobProgressPersistService.persistNow(jobId, shardingItem);
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            if (!jobRunnerManager.isStopping()) {
                log.error("Job {}-{} execution failed.", jobId, shardingItem, ex);
                governanceFacade.getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
                throw ex;
            }
        } finally {
            if (started) {
                jobRunnerManager.getTasksRunner(shardingItem).ifPresent(PipelineTasksRunner::stop);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean execute(final PipelineJobItemContext jobItemContext, final PipelineGovernanceFacade governanceFacade) {
        int shardingItem = jobItemContext.getShardingItem();
        PipelineTasksRunner tasksRunner = callback.buildTasksRunner(jobItemContext);
        if (!jobRunnerManager.addTasksRunner(shardingItem, tasksRunner)) {
            return false;
        }
        String jobId = jobItemContext.getJobId();
        governanceFacade.getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
        prepare(jobItemContext);
        log.info("Start tasks runner, jobId={}, shardingItem={}.", jobId, shardingItem);
        tasksRunner.start();
        return true;
    }
    
    private TransmissionProcessContext createTransmissionProcessContext(final String jobId, final PipelineJobType<?> jobType, final PipelineContextKey contextKey) {
        if (!jobType.getOption().isTransmissionJob()) {
            return null;
        }
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.fillInDefaultValue(new PipelineProcessConfigurationPersistService().load(contextKey, jobType.getType()));
        return new TransmissionProcessContext(jobId, processConfig);
    }
    
    @SuppressWarnings("unchecked")
    private void prepare(final PipelineJobItemContext jobItemContext) {
        try {
            callback.prepare(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            throw new PipelineInternalException(ex);
        }
    }
}
