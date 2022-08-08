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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * Rule altered job.
 */
@Slf4j
@RequiredArgsConstructor
public final class RuleAlteredJob extends AbstractPipelineJob implements SimpleJob, PipelineJob {
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
    
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    // Shared by all sharding items
    private final RuleAlteredJobPreparer jobPreparer = new RuleAlteredJobPreparer();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Execute job {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
        if (isStopping()) {
            log.info("stopping true, ignore");
            return;
        }
        setJobId(shardingContext.getJobName());
        RuleAlteredJobConfiguration jobConfig = RuleAlteredJobConfigurationSwapper.swapToObject(shardingContext.getJobParameter());
        JobProgress initProgress = governanceRepositoryAPI.getJobProgress(shardingContext.getJobName(), shardingContext.getShardingItem());
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig, shardingContext.getShardingItem(), initProgress, dataSourceManager);
        int shardingItem = jobContext.getShardingItem();
        if (getTasksRunnerMap().containsKey(shardingItem)) {
            // If the following log is output, it is possible that the elasticjob task was not shutdown correctly
            log.warn("schedulerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start RuleAlteredJobScheduler, jobId={}, shardingItem={}", getJobId(), shardingItem);
        RuleAlteredJobScheduler jobScheduler = new RuleAlteredJobScheduler(jobContext);
        runInBackground(() -> {
            prepare(jobContext);
            jobScheduler.start();
        });
        getTasksRunnerMap().put(shardingItem, jobScheduler);
        PipelineJobProgressPersistService.addJobProgressPersistContext(getJobId(), shardingItem);
    }
    
    /**
     * Stop job.
     */
    public void stop() {
        setStopping(true);
        dataSourceManager.close();
        if (null != getOneOffJobBootstrap()) {
            getOneOffJobBootstrap().shutdown();
        }
        if (null == getJobId()) {
            log.info("stop, jobId is null, ignore");
            return;
        }
        log.info("stop job scheduler, jobId={}", getJobId());
        for (PipelineTasksRunner each : getTasksRunnerMap().values()) {
            each.stop();
        }
        getTasksRunnerMap().clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(getJobId());
    }
    
    private void prepare(final RuleAlteredJobContext jobContext) {
        try {
            jobPreparer.prepare(jobContext);
        } catch (final PipelineIgnoredException ex) {
            log.info("pipeline ignore exception: {}", ex.getMessage());
            PipelineJobCenter.stop(getJobId());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("job prepare failed, {}-{}", getJobId(), jobContext.getShardingItem(), ex);
            PipelineJobCenter.stop(getJobId());
            jobContext.setStatus(JobStatus.PREPARING_FAILURE);
            PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobProgress(jobContext);
            throw ex;
        }
    }
}
