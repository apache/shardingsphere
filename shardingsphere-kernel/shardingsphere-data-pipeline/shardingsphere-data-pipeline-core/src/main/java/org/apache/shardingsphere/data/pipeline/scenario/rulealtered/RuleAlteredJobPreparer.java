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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.DefaultPipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.mode.lock.ExclusiveLockDefinition;

import java.sql.SQLException;
import java.util.Collections;

/**
 * Rule altered job preparer.
 */
@Slf4j
public final class RuleAlteredJobPreparer {
    
    private static final RuleAlteredJobAPI JOB_API = RuleAlteredJobAPIFactory.getInstance();
    
    /**
     * Do prepare work for scaling job.
     *
     * @param jobItemContext job item context
     */
    public void prepare(final RuleAlteredJobContext jobItemContext) {
        PipelineJobPreparerUtils.checkSourceDataSource(jobItemContext.getJobConfig().getSourceDatabaseType(), Collections.singleton(jobItemContext.getSourceDataSource()));
        if (jobItemContext.isStopping()) {
            throw new PipelineIgnoredException("Job stopping, jobId=" + jobItemContext.getJobId());
        }
        prepareAndCheckTargetWithLock(jobItemContext);
        if (jobItemContext.isStopping()) {
            throw new PipelineIgnoredException("Job stopping, jobId=" + jobItemContext.getJobId());
        }
        // TODO check metadata
        try {
            initIncrementalTasks(jobItemContext);
            if (jobItemContext.isStopping()) {
                throw new PipelineIgnoredException("Job stopping, jobId=" + jobItemContext.getJobId());
            }
            initInventoryTasks(jobItemContext);
            log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                    jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
        } catch (final SQLException ex) {
            log.error("Scaling job preparing failed, jobId={}", jobItemContext.getJobId());
            throw new PipelineJobPrepareFailedException("Scaling job preparing failed, jobId=" + jobItemContext.getJobId(), ex);
        }
    }
    
    private void prepareAndCheckTargetWithLock(final RuleAlteredJobContext jobItemContext) {
        RuleAlteredJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String lockName = "prepare-" + jobConfig.getJobId();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(lockName);
        JOB_API.persistJobItemProgress(jobItemContext);
        if (lockContext.tryLock(lockDefinition, 180000)) {
            log.info("try lock success, jobId={}, shardingItem={}", jobConfig.getJobId(), jobItemContext.getShardingItem());
            try {
                InventoryIncrementalJobItemProgress jobItemProgress = JOB_API.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
                boolean prepareFlag = null == jobItemProgress || JobStatus.PREPARING.equals(jobItemProgress.getStatus()) || JobStatus.RUNNING.equals(jobItemProgress.getStatus())
                        || JobStatus.PREPARING_FAILURE.equals(jobItemProgress.getStatus());
                if (prepareFlag) {
                    log.info("execute prepare, jobId={}, shardingItem={}", jobConfig.getJobId(), jobItemContext.getShardingItem());
                    jobItemContext.setStatus(JobStatus.PREPARING);
                    JOB_API.updateJobItemStatus(jobConfig.getJobId(), jobItemContext.getShardingItem(), JobStatus.PREPARING);
                    prepareAndCheckTarget(jobItemContext);
                    // TODO Loop insert zookeeper performance is not good
                    for (int i = 0; i <= jobItemContext.getJobConfig().getJobShardingCount(); i++) {
                        JOB_API.updateJobItemStatus(jobConfig.getJobId(), i, JobStatus.PREPARE_SUCCESS);
                    }
                }
            } finally {
                log.info("unlock, jobId={}, shardingItem={}", jobConfig.getJobId(), jobItemContext.getShardingItem());
                lockContext.unlock(lockDefinition);
            }
        }
    }
    
    private void prepareAndCheckTarget(final RuleAlteredJobContext jobItemContext) {
        prepareTarget(jobItemContext);
        InventoryIncrementalJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            PipelineDataSourceWrapper targetDataSource = jobItemContext.getDataSourceManager().getDataSource(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            PipelineJobPreparerUtils.checkTargetDataSource(jobItemContext.getJobConfig().getTargetDatabaseType(), jobItemContext.getTaskConfig().getImporterConfig(),
                    Collections.singletonList(targetDataSource));
        }
    }
    
    private void prepareTarget(final RuleAlteredJobContext jobItemContext) {
        RuleAlteredJobConfiguration jobConfig = jobItemContext.getJobConfig();
        TableNameSchemaNameMapping tableNameSchemaNameMapping = jobItemContext.getTaskConfig().getDumperConfig().getTableNameSchemaNameMapping();
        String targetDatabaseType = jobConfig.getTargetDatabaseType();
        if (isSourceAndTargetSchemaAvailable(jobConfig)) {
            PrepareTargetSchemasParameter prepareTargetSchemasParameter = new PrepareTargetSchemasParameter(jobConfig.splitLogicTableNames(),
                    DatabaseTypeFactory.getInstance(targetDatabaseType), jobConfig.getDatabaseName(),
                    jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig(), jobItemContext.getDataSourceManager(), tableNameSchemaNameMapping);
            PipelineJobPreparerUtils.prepareTargetSchema(targetDatabaseType, prepareTargetSchemasParameter);
        }
        PrepareTargetTablesParameter prepareTargetTablesParameter = new PrepareTargetTablesParameter(jobConfig.getDatabaseName(),
                jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig(),
                jobItemContext.getDataSourceManager(), jobConfig.getTablesFirstDataNodes(), tableNameSchemaNameMapping);
        PipelineJobPreparerUtils.prepareTargetTables(targetDatabaseType, prepareTargetTablesParameter);
    }
    
    private boolean isSourceAndTargetSchemaAvailable(final RuleAlteredJobConfiguration jobConfig) {
        DatabaseType sourceDatabaseType = DatabaseTypeFactory.getInstance(jobConfig.getSourceDatabaseType());
        DatabaseType targetDatabaseType = DatabaseTypeFactory.getInstance(jobConfig.getTargetDatabaseType());
        if (!sourceDatabaseType.isSchemaAvailable() || !targetDatabaseType.isSchemaAvailable()) {
            log.info("prepareTargetSchemas, one of source or target database type schema is not available, ignore");
            return false;
        }
        return true;
    }
    
    private void initInventoryTasks(final RuleAlteredJobContext jobItemContext) {
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceMetaDataLoader(), jobItemContext.getDataSourceManager(),
                jobItemContext.getJobProcessContext().getImporterExecuteEngine(), jobItemContext.getSourceDataSource(), jobItemContext.getTaskConfig(), jobItemContext.getInitProgress());
        jobItemContext.getInventoryTasks().addAll(inventoryTaskSplitter.splitInventoryData(jobItemContext));
    }
    
    private void initIncrementalTasks(final RuleAlteredJobContext jobItemContext) throws SQLException {
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        ExecuteEngine incrementalDumperExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalDumperExecuteEngine();
        TaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        PipelineDataSourceManager dataSourceManager = jobItemContext.getDataSourceManager();
        JobItemIncrementalTasksProgress initIncremental = jobItemContext.getInitProgress() == null ? null : jobItemContext.getInitProgress().getIncremental();
        taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(initIncremental, taskConfig.getDumperConfig(), dataSourceManager));
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobItemContext.getSourceMetaDataLoader();
        DefaultPipelineJobProgressListener jobProgressListener = new DefaultPipelineJobProgressListener(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getImporterConfig().getConcurrency(),
                taskConfig.getDumperConfig(), taskConfig.getImporterConfig(), pipelineChannelCreator, dataSourceManager, sourceMetaDataLoader, incrementalDumperExecuteEngine, jobProgressListener);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
    
    /**
     * Do cleanup work for scaling job.
     *
     * @param jobConfig job configuration
     */
    public void cleanup(final RuleAlteredJobConfiguration jobConfig) {
        try {
            PipelineJobPreparerUtils.destroyPosition(jobConfig.getSource());
        } catch (final SQLException ex) {
            log.warn("Scaling job destroying failed", ex);
        }
    }
}
