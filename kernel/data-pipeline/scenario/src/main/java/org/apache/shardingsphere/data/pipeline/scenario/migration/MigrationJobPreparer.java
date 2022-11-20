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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.prepare.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.lock.GlobalLockDefinition;

import java.sql.SQLException;
import java.util.Collections;

/**
 * Migration job preparer.
 */
@Slf4j
public final class MigrationJobPreparer {
    
    private static final MigrationJobAPI JOB_API = MigrationJobAPIFactory.getInstance();
    
    /**
     * Do prepare work.
     *
     * @param jobItemContext job item context
     * @throws SQLException SQL exception
     */
    public void prepare(final MigrationJobItemContext jobItemContext) throws SQLException {
        PipelineJobPreparerUtils.checkSourceDataSource(jobItemContext.getJobConfig().getSourceDatabaseType(), Collections.singleton(jobItemContext.getSourceDataSource()));
        if (jobItemContext.isStopping()) {
            PipelineJobCenter.stop(jobItemContext.getJobId());
            return;
        }
        prepareAndCheckTargetWithLock(jobItemContext);
        if (jobItemContext.isStopping()) {
            PipelineJobCenter.stop(jobItemContext.getJobId());
            return;
        }
        if (PipelineJobPreparerUtils.isIncrementalSupported(jobItemContext.getJobConfig().getSourceDatabaseType())) {
            initIncrementalTasks(jobItemContext);
            if (jobItemContext.isStopping()) {
                PipelineJobCenter.stop(jobItemContext.getJobId());
                return;
            }
        }
        initInventoryTasks(jobItemContext);
        log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    private void prepareAndCheckTargetWithLock(final MigrationJobItemContext jobItemContext) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String lockName = "prepare-" + jobConfig.getJobId();
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        if (null == JOB_API.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem())) {
            JOB_API.persistJobItemProgress(jobItemContext);
        }
        LockDefinition lockDefinition = new GlobalLockDefinition(lockName);
        long startTimeMillis = System.currentTimeMillis();
        if (lockContext.tryLock(lockDefinition, 180000)) {
            log.info("try lock success, jobId={}, shardingItem={}, cost {} ms", jobConfig.getJobId(), jobItemContext.getShardingItem(), System.currentTimeMillis() - startTimeMillis);
            try {
                InventoryIncrementalJobItemProgress jobItemProgress = JOB_API.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
                JobStatus currentStatus = null != jobItemProgress ? jobItemProgress.getStatus() : null;
                boolean prepareFlag = null == jobItemProgress || JobStatus.PREPARING.equals(currentStatus) || JobStatus.RUNNING.equals(currentStatus)
                        || JobStatus.PREPARING_FAILURE.equals(currentStatus);
                if (prepareFlag) {
                    jobItemContext.setStatus(JobStatus.PREPARING);
                    JOB_API.updateJobItemStatus(jobConfig.getJobId(), jobItemContext.getShardingItem(), JobStatus.PREPARING);
                    prepareAndCheckTarget(jobItemContext);
                    // TODO Loop insert zookeeper performance is not good
                    for (int i = 0; i <= jobItemContext.getJobConfig().getJobShardingCount(); i++) {
                        JOB_API.updateJobItemStatus(jobConfig.getJobId(), i, JobStatus.PREPARE_SUCCESS);
                    }
                }
            } finally {
                log.info("unlock, jobId={}, shardingItem={}, cost {} ms", jobConfig.getJobId(), jobItemContext.getShardingItem(), System.currentTimeMillis() - startTimeMillis);
                lockContext.unlock(lockDefinition);
            }
        }
    }
    
    private void prepareAndCheckTarget(final MigrationJobItemContext jobItemContext) throws SQLException {
        if (jobItemContext.isSourceTargetDatabaseTheSame()) {
            prepareTarget(jobItemContext);
        }
        InventoryIncrementalJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            PipelineDataSourceWrapper targetDataSource = jobItemContext.getDataSourceManager().getDataSource(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            PipelineJobPreparerUtils.checkTargetDataSource(jobItemContext.getJobConfig().getTargetDatabaseType(), jobItemContext.getTaskConfig().getImporterConfig(),
                    Collections.singletonList(targetDataSource));
        }
    }
    
    private void prepareTarget(final MigrationJobItemContext jobItemContext) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String targetDatabaseType = jobConfig.getTargetDatabaseType();
        CreateTableConfiguration createTableConfig = jobItemContext.getTaskConfig().getCreateTableConfig();
        PrepareTargetSchemasParameter prepareTargetSchemasParam = new PrepareTargetSchemasParameter(
                DatabaseTypeFactory.getInstance(targetDatabaseType), createTableConfig, jobItemContext.getDataSourceManager());
        PipelineJobPreparerUtils.prepareTargetSchema(targetDatabaseType, prepareTargetSchemasParam);
        ShardingSphereSQLParserEngine sqlParserEngine = PipelineJobPreparerUtils.getSQLParserEngine(jobConfig.getTargetDatabaseName());
        PipelineJobPreparerUtils.prepareTargetTables(targetDatabaseType, new PrepareTargetTablesParameter(createTableConfig, jobItemContext.getDataSourceManager(), sqlParserEngine));
    }
    
    private void initInventoryTasks(final MigrationJobItemContext jobItemContext) {
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        PipelineColumnMetaData uniqueKeyColumn = jobItemContext.getJobConfig().getUniqueKeyColumn();
        inventoryDumperConfig.setUniqueKey(uniqueKeyColumn.getName());
        inventoryDumperConfig.setUniqueKeyDataType(uniqueKeyColumn.getDataType());
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), inventoryDumperConfig, jobItemContext.getTaskConfig().getImporterConfig());
        jobItemContext.getInventoryTasks().addAll(inventoryTaskSplitter.splitInventoryData(jobItemContext));
    }
    
    private void initIncrementalTasks(final MigrationJobItemContext jobItemContext) {
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        MigrationTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        PipelineDataSourceManager dataSourceManager = jobItemContext.getDataSourceManager();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(initIncremental, taskConfig.getDumperConfig(), dataSourceManager));
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobItemContext.getSourceMetaDataLoader();
        ExecuteEngine incrementalExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalExecuteEngine();
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getImporterConfig().getConcurrency(), taskConfig.getDumperConfig(), taskConfig.getImporterConfig(),
                pipelineChannelCreator, dataSourceManager, sourceMetaDataLoader, incrementalExecuteEngine, jobItemContext);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
    
    /**
     * Do cleanup work.
     *
     * @param jobConfig job configuration
     */
    public void cleanup(final MigrationJobConfiguration jobConfig) {
        try {
            PipelineJobPreparerUtils.destroyPosition(jobConfig.getJobId(), jobConfig.getSource());
        } catch (final SQLException ex) {
            log.warn("job destroying failed", ex);
        }
    }
}
