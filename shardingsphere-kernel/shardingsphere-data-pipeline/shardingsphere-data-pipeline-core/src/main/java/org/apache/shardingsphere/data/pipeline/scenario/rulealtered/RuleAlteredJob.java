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
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryIncrementalTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

/**
 * Rule altered job.
 */
@Slf4j
@RequiredArgsConstructor
public final class RuleAlteredJob extends AbstractPipelineJob implements SimpleJob, PipelineJob {
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
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
        InventoryIncrementalJobItemProgress initProgress = RuleAlteredJobAPIFactory.getInstance().getJobItemProgress(shardingContext.getJobName(), shardingContext.getShardingItem());
        RuleAlteredJobContext jobItemContext = new RuleAlteredJobContext(jobConfig, shardingContext.getShardingItem(), initProgress, dataSourceManager);
        int shardingItem = jobItemContext.getShardingItem();
        if (getTasksRunnerMap().containsKey(shardingItem)) {
            log.warn("tasksRunnerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start tasks runner, jobId={}, shardingItem={}", getJobId(), shardingItem);
        InventoryIncrementalTasksRunner tasksRunner = new InventoryIncrementalTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks(),
                jobItemContext.getJobProcessContext().getInventoryDumperExecuteEngine(), jobItemContext.getJobProcessContext().getIncrementalDumperExecuteEngine());
        runInBackground(() -> {
            prepare(jobItemContext);
            tasksRunner.start();
        });
        getTasksRunnerMap().put(shardingItem, tasksRunner);
        PipelineJobProgressPersistService.addJobProgressPersistContext(getJobId(), shardingItem);
    }
    
    private void prepare(final RuleAlteredJobContext jobItemContext) {
        try {
            jobPreparer.prepare(jobItemContext);
        } catch (final PipelineIgnoredException ex) {
            log.info("pipeline ignore exception: {}", ex.getMessage());
            PipelineJobCenter.stop(getJobId());
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("job prepare failed, {}-{}", getJobId(), jobItemContext.getShardingItem(), ex);
            PipelineJobCenter.stop(getJobId());
            jobItemContext.setStatus(JobStatus.PREPARING_FAILURE);
            RuleAlteredJobAPIFactory.getInstance().persistJobItemProgress(jobItemContext);
            throw ex;
        }
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
        log.info("stop tasks runner, jobId={}", getJobId());
        for (PipelineTasksRunner each : getTasksRunnerMap().values()) {
            each.stop();
        }
        getTasksRunnerMap().clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(getJobId());
    }
}
