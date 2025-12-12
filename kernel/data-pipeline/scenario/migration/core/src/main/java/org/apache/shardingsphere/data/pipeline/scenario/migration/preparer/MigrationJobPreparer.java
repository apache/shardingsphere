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

package org.apache.shardingsphere.data.pipeline.scenario.migration.preparer;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.IncrementalChannelCreator;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.checker.PipelineDataSourceCheckEngine;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCancelingException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.CreateIncrementalDumperParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.DialectIncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.preparer.PipelineJobPreparer;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.PipelineJobDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.inventory.splitter.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.job.PipelineJobOffsetGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskUtils;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Migration job preparer.
 */
@Slf4j
public final class MigrationJobPreparer implements PipelineJobPreparer<MigrationJobItemContext> {
    
    private final PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(new MigrationJobType().getOption().getYamlJobItemProgressSwapper());
    
    @Override
    public void prepare(final MigrationJobItemContext jobItemContext) throws SQLException {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(
                jobItemContext.getTaskConfig().getDumperContext().getCommonContext().getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("Migration inventory dumper only support StandardPipelineDataSourceConfiguration."));
        DatabaseType sourceDatabaseType = jobItemContext.getJobConfig().getSourceDatabaseType();
        new PipelineDataSourceCheckEngine(sourceDatabaseType).checkSourceDataSources(Collections.singleton(jobItemContext.getSourceDataSource()));
        ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
        prepareAndCheckTargetWithLock(jobItemContext);
        ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
        boolean isIncrementalSupported = DatabaseTypedSPILoader.findService(DialectIncrementalDumperCreator.class, sourceDatabaseType).isPresent();
        if (isIncrementalSupported) {
            prepareIncremental(jobItemContext);
        }
        initInventoryTasks(jobItemContext);
        if (isIncrementalSupported) {
            initIncrementalTasks(jobItemContext);
            ShardingSpherePreconditions.checkState(!jobItemContext.isStopping(), PipelineJobCancelingException::new);
        }
        log.info("Prepare job, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                jobItemContext.getJobId(), jobItemContext.getShardingItem(), jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    private void prepareAndCheckTargetWithLock(final MigrationJobItemContext jobItemContext) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        String jobId = jobConfig.getJobId();
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        ContextManager contextManager = PipelineContextManager.getContext(contextKey);
        if (!jobItemManager.getProgress(jobId, jobItemContext.getShardingItem()).isPresent()) {
            jobItemManager.persistProgress(jobItemContext);
        }
        contextManager.getExclusiveOperatorEngine().operate(new MigrationPrepareOperation(jobId), 600 * 1000L, () -> {
            PipelineJobOffsetGovernanceRepository offsetRepository = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getJobFacade().getOffset();
            JobOffsetInfo offsetInfo = offsetRepository.load(jobId);
            if (!offsetInfo.isTargetSchemaTableCreated()) {
                jobItemContext.setStatus(JobStatus.PREPARING);
                jobItemManager.updateStatus(jobId, jobItemContext.getShardingItem(), JobStatus.PREPARING);
                prepareAndCheckTarget(jobItemContext, contextManager);
                offsetRepository.persist(jobId, new JobOffsetInfo(true));
            }
        });
    }
    
    private void prepareAndCheckTarget(final MigrationJobItemContext jobItemContext, final ContextManager contextManager) throws SQLException {
        if (jobItemContext.isSourceTargetDatabaseTheSame()) {
            prepareTarget(jobItemContext, contextManager);
        }
        if (null == jobItemContext.getInitProgress()) {
            PipelineDataSource targetDataSource = jobItemContext.getDataSourceManager().getDataSource(jobItemContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            new PipelineDataSourceCheckEngine(jobItemContext.getJobConfig().getTargetDatabaseType()).checkTargetDataSources(Collections.singleton(targetDataSource),
                    jobItemContext.getTaskConfig().getImporterConfig());
        }
    }
    
    private void prepareTarget(final MigrationJobItemContext jobItemContext, final ContextManager contextManager) throws SQLException {
        MigrationJobConfiguration jobConfig = jobItemContext.getJobConfig();
        DatabaseType targetDatabaseType = jobItemContext.getJobConfig().getTargetDatabaseType();
        Collection<CreateTableConfiguration> createTableConfigs = jobItemContext.getTaskConfig().getCreateTableConfigurations();
        PipelineDataSourceManager dataSourceManager = jobItemContext.getDataSourceManager();
        PipelineJobDataSourcePreparer preparer = new PipelineJobDataSourcePreparer(targetDatabaseType);
        preparer.prepareTargetSchemas(new PrepareTargetSchemasParameter(targetDatabaseType, createTableConfigs, dataSourceManager));
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        SQLParserEngine sqlParserEngine = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(targetDatabaseType);
        preparer.prepareTargetTables(new PrepareTargetTablesParameter(createTableConfigs, dataSourceManager, sqlParserEngine, jobConfig.getTargetDatabaseName()));
    }
    
    private void prepareIncremental(final MigrationJobItemContext jobItemContext) {
        MigrationTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        JobItemIncrementalTasksProgress initIncremental = null == jobItemContext.getInitProgress() ? null : jobItemContext.getInitProgress().getIncremental();
        try {
            DatabaseType databaseType = taskConfig.getDumperContext().getCommonContext().getDataSourceConfig().getDatabaseType();
            IngestPosition position = new IncrementalTaskPositionManager(databaseType).getPosition(initIncremental, taskConfig.getDumperContext(), jobItemContext.getDataSourceManager());
            taskConfig.getDumperContext().getCommonContext().setPosition(position);
        } catch (final SQLException ex) {
            throw new PrepareJobWithGetBinlogPositionException(jobItemContext.getJobId(), ex);
        }
    }
    
    private void initInventoryTasks(final MigrationJobItemContext jobItemContext) {
        InventoryDumperContext inventoryDumperContext = new InventoryDumperContext(jobItemContext.getTaskConfig().getDumperContext().getCommonContext());
        InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter(jobItemContext.getSourceDataSource(), inventoryDumperContext, jobItemContext.getTaskConfig().getImporterConfig());
        jobItemContext.getInventoryTasks().addAll(inventoryTaskSplitter.split(jobItemContext));
    }
    
    private void initIncrementalTasks(final MigrationJobItemContext jobItemContext) {
        MigrationTaskConfiguration taskConfig = jobItemContext.getTaskConfig();
        IncrementalDumperContext dumperContext = taskConfig.getDumperContext();
        PipelineExecuteEngine incrementalExecuteEngine = jobItemContext.getJobProcessContext().getIncrementalExecuteEngine();
        IncrementalTaskProgress taskProgress = PipelineTaskUtils.createIncrementalTaskProgress(dumperContext.getCommonContext().getPosition(), jobItemContext.getInitProgress());
        PipelineChannel channel = IncrementalChannelCreator.create(jobItemContext.getJobProcessContext().getProcessConfiguration().getStreamChannel(), taskProgress);
        CreateIncrementalDumperParameter param = new CreateIncrementalDumperParameter(
                dumperContext, dumperContext.getCommonContext().getPosition(), channel, jobItemContext.getSourceMetaDataLoader(), jobItemContext.getDataSourceManager());
        Dumper dumper = IncrementalDumperCreator.create(param);
        Collection<Importer> importers = Collections.singletonList(new SingleChannelConsumerImporter(channel, taskConfig.getImporterConfig().getBatchSize(), 1000L,
                jobItemContext.getSink(), jobItemContext));
        PipelineTask incrementalTask = new IncrementalTask(dumperContext.getCommonContext().getDataSourceName(), incrementalExecuteEngine, dumper, importers, taskProgress);
        jobItemContext.getIncrementalTasks().add(incrementalTask);
    }
}
