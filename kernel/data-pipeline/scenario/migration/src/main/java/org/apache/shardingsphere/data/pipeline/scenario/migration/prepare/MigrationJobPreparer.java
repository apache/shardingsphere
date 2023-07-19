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

package org.apache.shardingsphere.data.pipeline.scenario.migration.prepare;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.common.config.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobCenter;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.preparer.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.preparer.PipelineJobPreparerUtils;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreator;
import org.apache.shardingsphere.infra.lock.GlobalLockNames;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.lock.GlobalLockDefinition;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Migration job preparer.
 */
@Slf4j
public final class MigrationJobPreparer {
    
    private final MigrationJobAPI jobAPI = new MigrationJobAPI();
    
    /**
     * Do prepare work.
     *
     * @param jobItemContext job item context
     * @throws SQLException SQL exception
     */
    public void prepare(final MigrationJobItemContext jobItemContext) throws SQLException {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(jobItemContext.getTaskConfig().getDumperConfig().getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("Migration inventory dumper only support StandardPipelineDataSourceConfiguration"));
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
        prepareIncremental(jobItemContext);
        initInventoryTasks(jobItemContext);
        if (PipelineJobPreparerUtils.isIncrementalSupported(jobItemContext.getJobConfig().getSourceDatabaseType())) {
            initIncrementalTasks(jobItemContext);
            if (jobItemContext.isStopping()) {
                PipelineJobCenter.stop(jobItemContext.getJobId());
                return;
            }
        }
        log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void prepareAndCheckTargetWithLock(final MigrationJobItemContext jobItemContext) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String jobId = jobConfig.getJobId();
        LockContext lockContext = PipelineContextManager.getContext(PipelineJobIdUtils.parseContextKey(jobId)).getContextManager().getInstanceContext().getLockContext();
        if (!jobAPI.getJobItemProgress(jobId, jobItemContext.getShardingItem()).isPresent()) {
            jobAPI.persistJobItemProgress(jobItemContext);
        }
        LockDefinition lockDefinition = new GlobalLockDefinition(String.format(GlobalLockNames.PREPARE.getLockName(), jobConfig.getJobId()));
        long startTimeMillis = System.currentTimeMillis();
        if (lockContext.tryLock(lockDefinition, 600000)) {
            log.info("try lock success, jobId={}, shardingItem={}, cost {} ms", jobId, jobItemContext.getShardingItem(), System.currentTimeMillis() - startTimeMillis);
            try {
                JobOffsetInfo offsetInfo = jobAPI.getJobOffsetInfo(jobId);
                if (!offsetInfo.isTargetSchemaTableCreated()) {
                    jobItemContext.setStatus(JobStatus.PREPARING);
                    jobAPI.updateJobItemStatus(jobId, jobItemContext.getShardingItem(), JobStatus.PREPARING);
                    prepareAndCheckTarget(jobItemContext);
                    jobAPI.persistJobOffsetInfo(jobId, new JobOffsetInfo(true));
                }
            } finally {
                log.info("unlock, jobId={}, shardingItem={}, cost {} ms", jobId, jobItemContext.getShardingItem(), System.currentTimeMillis() - startTimeMillis);
                lockContext.unlock(lockDefinition);
            }
        } else {
            log.warn("try lock failed, jobId={}, shardingItem={}", jobId, jobItemContext.getShardingItem());
        }
    }
    
    private void prepareAndCheckTarget(final MigrationJobItemContext jobItemContext) throws SQLException {
        if (jobItemContext.isSourceTargetDatabaseTheSame()) {
            prepareTarget(jobItemContext);
        }
        InventoryIncrementalJobItemProgress initProgress = jobItemContext.getInitProgress();
        if (null == initProgress) {
            PipelineDataSourceWrapper targetDataSource = jobItemContext.getDataSourceManager().getDataSource(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            PipelineJobPreparerUtils.checkTargetDataSource(
                    jobItemContext.getJobConfig().getTargetDatabaseType(), jobItemContext.getTaskConfig().getImporterConfig(), Collections.singleton(targetDataSource));
        }
    }
    
    private void prepareTarget(final MigrationJobItemContext jobItemContext) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        CreateTableConfiguration createTableConfig = jobItemContext.getTaskConfig().getCreateTableConfig();
        PipelineDataSourceManager dataSourceManager = jobItemContext.getDataSourceManager();
        PrepareTargetSchemasParameter prepareTargetSchemasParam = new PrepareTargetSchemasParameter(jobItemContext.getJobConfig().getTargetDatabaseType(), createTableConfig, dataSourceManager);
        PipelineJobPreparerUtils.prepareTargetSchema(jobItemContext.getJobConfig().getTargetDatabaseType(), prepareTargetSchemasParam);
        ShardingSphereMetaData metaData = PipelineContextManager.getContext(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId())).getContextManager().getMetaDataContexts().getMetaData();
        SQLParserEngine sqlParserEngine = PipelineJobPreparerUtils.getSQLParserEngine(metaData, jobConfig.getTargetDatabaseName());
        PipelineJobPreparerUtils.prepareTargetTables(jobItemContext.getJobConfig().getTargetDatabaseType(), new PrepareTargetTablesParameter(createTableConfig, dataSourceManager, sqlParserEngine));
    }
    
    private void prepareIncremental(final MigrationJobItemContext jobItemContext) {
        MigrationTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            taskConfig.getDumperConfig().setPosition(PipelineJobPreparerUtils.getIncrementalPosition(initIncremental, taskConfig.getDumperConfig(), jobItemContext.getDataSourceManager()));
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
    }
    
    private void initInventoryTasks(final MigrationJobItemContext jobItemContext) {
        InventoryDumperConfiguration inventoryDumperConfig = new InventoryDumperConfiguration(jobItemContext.getTaskConfig().getDumperConfig());
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), inventoryDumperConfig, jobItemContext.getTaskConfig().getImporterConfig());
        jobItemContext.getInventoryTasks().addAll(inventoryTaskSplitter.splitInventoryData(jobItemContext));
    }
    
    private void initIncrementalTasks(final MigrationJobItemContext jobItemContext) {
        MigrationTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        PipelineChannelCreator pipelineChannelCreator = jobItemContext.getJobProcessContext().getPipelineChannelCreator();
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobItemContext.getSourceMetaDataLoader();
        DumperConfiguration dumperConfig = taskConfig.getDumperConfig();
        ImporterConfiguration importerConfig = taskConfig.getImporterConfig();
        ExecuteEngine incrementalExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalExecuteEngine();
        IncrementalTaskProgress taskProgress = PipelineTaskUtils.createIncrementalTaskProgress(dumperConfig.getPosition(), jobItemContext.getInitProgress());
        PipelineChannel channel = PipelineTaskUtils.createIncrementalChannel(importerConfig.getConcurrency(), pipelineChannelCreator, taskProgress);
        Dumper dumper = DatabaseTypedSPILoader.getService(IncrementalDumperCreator.class, dumperConfig.getDataSourceConfig().getDatabaseType())
                .createIncrementalDumper(dumperConfig, dumperConfig.getPosition(), channel, sourceMetaDataLoader);
        Collection<Importer> importers = createImporters(importerConfig, jobItemContext.getSink(), channel, jobItemContext);
        PipelineTask incrementalTask = new IncrementalTask(dumperConfig.getDataSourceName(), incrementalExecuteEngine, dumper, importers, taskProgress);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
    
    private Collection<Importer> createImporters(final ImporterConfiguration importerConfig, final PipelineSink sink, final PipelineChannel channel,
                                                 final PipelineJobProgressListener jobProgressListener) {
        Collection<Importer> result = new LinkedList<>();
        for (int i = 0; i < importerConfig.getConcurrency(); i++) {
            Importer importer = new SingleChannelConsumerImporter(channel, importerConfig.getBatchSize(), 3, TimeUnit.SECONDS, sink, jobProgressListener);
            result.add(importer);
        }
        return result;
    }
    
    /**
     * Do cleanup work.
     *
     * @param jobConfig job configuration
     */
    public void cleanup(final MigrationJobConfiguration jobConfig) {
        for (Entry<String, PipelineDataSourceConfiguration> entry : jobConfig.getSources().entrySet()) {
            try {
                PipelineJobPreparerUtils.destroyPosition(jobConfig.getJobId(), entry.getValue());
            } catch (final SQLException ex) {
                log.warn("job destroying failed, jobId={}, dataSourceName={}", jobConfig.getJobId(), entry.getKey(), ex);
            }
        }
    }
}
