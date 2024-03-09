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
import lombok.extern.slf4j.Slf4j;
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
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.sql.SQLException;

/**
 * Abstract separable pipeline job.
 * 
 * @param <T> type of pipeline job configuration
 * @param <I> type of pipeline job item context
 * @param <P> type of pipeline job item progress
 */
@Getter
@Slf4j
public abstract class AbstractSeparablePipelineJob<T extends PipelineJobConfiguration, I extends PipelineJobItemContext, P extends PipelineJobItemProgress> implements PipelineJob {
    
    private final PipelineJobRunnerManager jobRunnerManager;
    
    private final TransmissionProcessContext jobProcessContext;
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    protected AbstractSeparablePipelineJob(final String jobId) {
        this(jobId, true);
    }
    
    protected AbstractSeparablePipelineJob(final String jobId, final boolean isTransmissionProcessContextNeeded) {
        jobRunnerManager = new PipelineJobRunnerManager();
        jobProcessContext = isTransmissionProcessContextNeeded ? createTransmissionProcessContext(jobId) : null;
    }
    
    private TransmissionProcessContext createTransmissionProcessContext(final String jobId) {
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(
                processConfigPersistService.load(PipelineJobIdUtils.parseContextKey(jobId), PipelineJobIdUtils.parseJobType(jobId).getType()));
        return new TransmissionProcessContext(jobId, processConfig);
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
        T jobConfig = jobConfigManager.getJobConfiguration(jobId);
        PipelineJobItemManager<P> jobItemManager = new PipelineJobItemManager<>(jobType.getYamlJobItemProgressSwapper());
        P jobItemProgress = jobItemManager.getProgress(shardingContext.getJobName(), shardingItem).orElse(null);
        boolean started = false;
        try {
            started = execute(buildJobItemContext(jobConfig, shardingItem, jobItemProgress, jobProcessContext));
            if (started) {
                PipelineJobProgressPersistService.persistNow(jobId, shardingItem);
            }
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            if (!jobRunnerManager.isStopping()) {
                log.error("Job execution failed, {}-{}", jobId, shardingItem, ex);
                PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().update(jobId, shardingItem, ex);
                throw ex;
            }
        } finally {
            if (started) {
                jobRunnerManager.getTasksRunner(shardingItem).ifPresent(PipelineTasksRunner::stop);
            }
        }
    }
    
    private boolean execute(final I jobItemContext) {
        int shardingItem = jobItemContext.getShardingItem();
        PipelineTasksRunner tasksRunner = buildTasksRunner(jobItemContext);
        if (!jobRunnerManager.addTasksRunner(shardingItem, tasksRunner)) {
            return false;
        }
        String jobId = jobItemContext.getJobId();
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().clean(jobId, shardingItem);
        prepare(jobItemContext);
        log.info("Start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        tasksRunner.start();
        return true;
    }
    
    protected abstract I buildJobItemContext(T jobConfig, int shardingItem, P jobItemProgress, TransmissionProcessContext jobProcessContext);
    
    protected abstract PipelineTasksRunner buildTasksRunner(I jobItemContext);
    
    protected final void prepare(final I jobItemContext) {
        try {
            doPrepare(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            throw new PipelineInternalException(ex);
        }
    }
    
    protected abstract void doPrepare(I jobItemContext) throws SQLException;
}
