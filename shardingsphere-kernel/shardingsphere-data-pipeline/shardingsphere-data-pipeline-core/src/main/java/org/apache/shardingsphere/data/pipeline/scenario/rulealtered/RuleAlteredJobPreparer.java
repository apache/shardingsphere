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
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.DefaultPipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.mode.lock.ExclusiveLockDefinition;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Rule altered job preparer.
 */
@Slf4j
public final class RuleAlteredJobPreparer {
    
    /**
     * Do prepare work for scaling job.
     *
     * @param jobContext job context
     */
    public void prepare(final RuleAlteredJobContext jobContext) {
        PipelineJobPreparerUtils.checkSourceDataSource(jobContext.getJobConfig().getSourceDatabaseType(), Collections.singleton(jobContext.getSourceDataSource()));
        if (jobContext.isStopping()) {
            throw new PipelineIgnoredException("Job stopping, jobId=" + jobContext.getJobId());
        }
        prepareAndCheckTargetWithLock(jobContext);
        if (jobContext.isStopping()) {
            throw new PipelineIgnoredException("Job stopping, jobId=" + jobContext.getJobId());
        }
        // TODO check metadata
        try {
            initIncrementalTasks(jobContext);
            if (jobContext.isStopping()) {
                throw new PipelineIgnoredException("Job stopping, jobId=" + jobContext.getJobId());
            }
            initInventoryTasks(jobContext);
            log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                    jobContext.getJobId(), jobContext.getShardingItem(), jobContext.getInventoryTasks(), jobContext.getIncrementalTasks());
        } catch (final SQLException ex) {
            log.error("Scaling job preparing failed, jobId={}", jobContext.getJobId());
            throw new PipelineJobPrepareFailedException("Scaling job preparing failed, jobId=" + jobContext.getJobId(), ex);
        }
    }
    
    private void prepareAndCheckTargetWithLock(final RuleAlteredJobContext jobContext) {
        RuleAlteredJobConfiguration jobConfig = jobContext.getJobConfig();
        // TODO the lock will be replaced
        String lockName = "prepare-" + jobConfig.getJobId();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        LockDefinition lockDefinition = new ExclusiveLockDefinition(lockName);
        if (lockContext.tryLock(lockDefinition, 3000)) {
            log.info("try lock success, jobId={}, shardingItem={}", jobConfig.getJobId(), jobContext.getShardingItem());
            try {
                prepareAndCheckTarget(jobContext);
            } finally {
                lockContext.unlock(lockDefinition);
            }
        } else {
            log.info("wait lock released, jobId={}, shardingItem={}", jobConfig.getJobId(), jobContext.getShardingItem());
            waitUntilLockReleased(lockContext, lockDefinition);
        }
    }
    
    private void waitUntilLockReleased(final LockContext lockContext, final LockDefinition lockDefinition) {
        for (int loopCount = 0; loopCount < 30; loopCount++) {
            log.info("waiting for lock released, lockKey={}, loopCount={}", lockDefinition.getLockKey(), loopCount);
            ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(5));
            if (!lockContext.isLocked(lockDefinition)) {
                log.info("unlocked, lockName={}", lockDefinition.getLockKey());
                return;
            }
        }
    }
    
    private void prepareAndCheckTarget(final RuleAlteredJobContext jobContext) {
        prepareTarget(jobContext);
        JobProgress initProgress = jobContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            PipelineDataSourceWrapper targetDataSource = jobContext.getDataSourceManager().getDataSource(jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            PipelineJobPreparerUtils.checkTargetDataSource(jobContext.getJobConfig().getTargetDatabaseType(), jobContext.getTaskConfig().getImporterConfig(),
                    Collections.singletonList(targetDataSource));
        }
    }
    
    private void prepareTarget(final RuleAlteredJobContext jobContext) {
        RuleAlteredJobConfiguration jobConfig = jobContext.getJobConfig();
        TableNameSchemaNameMapping tableNameSchemaNameMapping = jobContext.getTaskConfig().getDumperConfig().getTableNameSchemaNameMapping();
        String targetDatabaseType = jobConfig.getTargetDatabaseType();
        if (isSourceAndTargetSchemaAvailable(jobConfig)) {
            PrepareTargetSchemasParameter prepareTargetSchemasParameter = new PrepareTargetSchemasParameter(jobConfig.splitLogicTableNames(),
                    DatabaseTypeFactory.getInstance(targetDatabaseType),
                    jobConfig.getDatabaseName(), jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig(), jobContext.getDataSourceManager(), tableNameSchemaNameMapping);
            PipelineJobPreparerUtils.prepareTargetSchema(targetDatabaseType, prepareTargetSchemasParameter);
        }
        PrepareTargetTablesParameter prepareTargetTablesParameter = new PrepareTargetTablesParameter(jobConfig.getDatabaseName(), jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig(),
                jobContext.getDataSourceManager(), jobConfig.getTablesFirstDataNodes(), tableNameSchemaNameMapping);
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
    
    private void initInventoryTasks(final RuleAlteredJobContext jobContext) {
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobContext.getSourceMetaDataLoader(), jobContext.getDataSourceManager(),
                jobContext.getJobProcessContext().getImporterExecuteEngine(), jobContext.getSourceDataSource(), jobContext.getTaskConfig(), jobContext.getInitProgress());
        jobContext.getInventoryTasks().addAll(inventoryTaskSplitter.splitInventoryData(jobContext));
    }
    
    private void initIncrementalTasks(final RuleAlteredJobContext jobContext) throws SQLException {
        PipelineChannelCreator pipelineChannelCreator = jobContext.getJobProcessContext().getPipelineChannelCreator();
        ExecuteEngine incrementalDumperExecuteEngine = jobContext.getJobProcessContext().getIncrementalDumperExecuteEngine();
        TaskConfiguration taskConfig = jobContext.getTaskConfig();
        PipelineDataSourceManager dataSourceManager = jobContext.getDataSourceManager();
        JobItemIncrementalTasksProgress incrementalTasksProgress = jobContext.getInitProgress() == null ? null : jobContext.getInitProgress().getIncremental();
        taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(incrementalTasksProgress, taskConfig.getDumperConfig(), dataSourceManager));
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobContext.getSourceMetaDataLoader();
        DefaultPipelineJobProgressListener jobProgressListener = new DefaultPipelineJobProgressListener(jobContext.getJobId(), jobContext.getShardingItem());
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getImporterConfig().getConcurrency(),
                taskConfig.getDumperConfig(), taskConfig.getImporterConfig(), pipelineChannelCreator, dataSourceManager, sourceMetaDataLoader, incrementalDumperExecuteEngine, jobProgressListener);
        jobContext.getIncrementalTasks().add(incrementalTask);
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
