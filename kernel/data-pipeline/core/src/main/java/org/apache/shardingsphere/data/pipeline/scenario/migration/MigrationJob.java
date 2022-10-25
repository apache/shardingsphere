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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryIncrementalTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.sql.SQLException;

/**
 * Migration job.
 */
@RequiredArgsConstructor
@Slf4j
public final class MigrationJob extends AbstractPipelineJob implements SimpleJob {
    
    private final MigrationJobAPI jobAPI = MigrationJobAPIFactory.getInstance();
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    private final PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance();
    
    // Shared by all sharding items
    private final MigrationJobPreparer jobPreparer = new MigrationJobPreparer();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        log.info("Execute job {}-{}", shardingContext.getJobName(), shardingItem);
        if (isStopping()) {
            log.info("stopping true, ignore");
            return;
        }
        setJobId(shardingContext.getJobName());
        MigrationJobConfiguration jobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        InventoryIncrementalJobItemProgress initProgress = jobAPI.getJobItemProgress(shardingContext.getJobName(), shardingItem);
        MigrationProcessContext jobProcessContext = jobAPI.buildPipelineProcessContext(jobConfig);
        MigrationTaskConfiguration taskConfig = jobAPI.buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        MigrationJobItemContext jobItemContext = new MigrationJobItemContext(jobConfig, shardingItem, initProgress, jobProcessContext, taskConfig, dataSourceManager);
        if (getTasksRunnerMap().containsKey(shardingItem)) {
            log.warn("tasksRunnerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start tasks runner, jobId={}, shardingItem={}", getJobId(), shardingItem);
        jobAPI.cleanJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        InventoryIncrementalTasksRunner tasksRunner = new InventoryIncrementalTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
        runInBackground(() -> {
            prepare(jobItemContext);
            tasksRunner.start();
        });
        getTasksRunnerMap().put(shardingItem, tasksRunner);
        PipelineJobProgressPersistService.addJobProgressPersistContext(getJobId(), shardingItem);
        pipelineDistributedBarrier.persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierEnablePath(getJobId()), shardingItem);
    }
    
    private void prepare(final MigrationJobItemContext jobItemContext) {
        try {
            long startTimeMillis = System.currentTimeMillis();
            jobPreparer.prepare(jobItemContext);
            log.info("prepare cost {} ms", System.currentTimeMillis() - startTimeMillis);
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("job prepare failed, {}-{}", getJobId(), jobItemContext.getShardingItem(), ex);
            PipelineJobCenter.stop(jobItemContext.getJobId(), true);
            jobItemContext.setStatus(JobStatus.PREPARING_FAILURE);
            jobAPI.persistJobItemProgress(jobItemContext);
            jobAPI.persistJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem(), ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Stop job.
     */
    public void stop() {
        setStopping(true);
        dataSourceManager.close();
        if (null != getJobBootstrap()) {
            getJobBootstrap().shutdown();
        }
        String jobId = getJobId();
        if (null == jobId) {
            log.info("stop, jobId is null, ignore");
            return;
        }
        log.info("stop tasks runner, jobId={}", jobId);
        for (PipelineTasksRunner each : getTasksRunnerMap().values()) {
            each.stop();
        }
        getTasksRunnerMap().clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(jobId);
    }
}
