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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineIgnoredException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PositionInitializerFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceCheckerFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.lock.LockScope;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Rule altered job preparer.
 */
@Slf4j
public final class RuleAlteredJobPreparer {
    
    private final InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter();
    
    /**
     * Do prepare work for scaling job.
     *
     * @param jobContext job context
     */
    public void prepare(final RuleAlteredJobContext jobContext) {
        checkSourceDataSource(jobContext);
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
        ShardingSphereLock lock = PipelineContext.getContextManager().getInstanceContext().getLockContext().getLock(LockScope.GLOBAL);
        if (lock.tryLock(lockName, 3000)) {
            try {
                prepareAndCheckTarget(jobContext);
            } finally {
                lock.releaseLock(lockName);
            }
        } else {
            waitUntilLockReleased(lock, lockName);
        }
    }
    
    private void waitUntilLockReleased(final ShardingSphereLock lock, final String lockName) {
        for (int loopCount = 0; loopCount < 30; loopCount++) {
            ThreadUtil.sleep(TimeUnit.SECONDS.toMillis(5));
            if (!lock.isLocked(lockName)) {
                log.info("unlocked, lockName={}", lockName);
                return;
            }
        }
    }
    
    private void prepareAndCheckTarget(final RuleAlteredJobContext jobContext) {
        prepareTarget(jobContext);
        JobProgress initProgress = jobContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            PipelineDataSourceWrapper targetDataSource = jobContext.getDataSourceManager().getDataSource(jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            checkTargetDataSource(jobContext, targetDataSource);
        }
    }
    
    private void prepareTarget(final RuleAlteredJobContext jobContext) {
        RuleAlteredJobConfiguration jobConfig = jobContext.getJobConfig();
        Optional<DataSourcePreparer> dataSourcePreparer = EnvironmentCheckerFactory.getDataSourcePreparer(jobConfig.getTargetDatabaseType());
        if (!dataSourcePreparer.isPresent()) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        TableNameSchemaNameMapping tableNameSchemaNameMapping = jobContext.getTaskConfig().getDumperConfig().getTableNameSchemaNameMapping();
        PrepareTargetSchemasParameter prepareTargetSchemasParameter = new PrepareTargetSchemasParameter(jobContext.getTaskConfig(), jobContext.getDataSourceManager(), tableNameSchemaNameMapping);
        dataSourcePreparer.get().prepareTargetSchemas(prepareTargetSchemasParameter);
        PrepareTargetTablesParameter prepareTargetTablesParameter = new PrepareTargetTablesParameter(jobContext.getTaskConfig(), jobContext.getDataSourceManager(), tableNameSchemaNameMapping);
        dataSourcePreparer.get().prepareTargetTables(prepareTargetTablesParameter);
    }
    
    private void checkSourceDataSource(final RuleAlteredJobContext jobContext) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerFactory.getInstance(jobContext.getJobConfig().getSourceDatabaseType());
        Collection<PipelineDataSourceWrapper> sourceDataSources = Collections.singleton(jobContext.getSourceDataSource());
        dataSourceChecker.checkConnection(sourceDataSources);
        dataSourceChecker.checkPrivilege(sourceDataSources);
        dataSourceChecker.checkVariable(sourceDataSources);
    }
    
    private void checkTargetDataSource(final RuleAlteredJobContext jobContext, final PipelineDataSourceWrapper targetDataSource) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerFactory.getInstance(jobContext.getJobConfig().getTargetDatabaseType());
        Collection<PipelineDataSourceWrapper> targetDataSources = Collections.singletonList(targetDataSource);
        dataSourceChecker.checkConnection(targetDataSources);
        ImporterConfiguration importerConfig = jobContext.getTaskConfig().getImporterConfig();
        dataSourceChecker.checkTargetTable(targetDataSources, importerConfig.getTableNameSchemaNameMapping(), importerConfig.getLogicTableNames());
    }
    
    private void initInventoryTasks(final RuleAlteredJobContext jobContext) {
        List<InventoryTask> allInventoryTasks = inventoryTaskSplitter.splitInventoryData(jobContext);
        jobContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final RuleAlteredJobContext jobContext) throws SQLException {
        PipelineChannelCreator pipelineChannelCreator = jobContext.getRuleAlteredContext().getPipelineChannelCreator();
        ExecuteEngine incrementalDumperExecuteEngine = jobContext.getRuleAlteredContext().getIncrementalDumperExecuteEngine();
        TaskConfiguration taskConfig = jobContext.getTaskConfig();
        PipelineDataSourceManager dataSourceManager = jobContext.getDataSourceManager();
        taskConfig.getDumperConfig().setPosition(getIncrementalPosition(jobContext, taskConfig, dataSourceManager));
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobContext.getSourceMetaDataLoader();
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getJobConfig().getConcurrency(),
                taskConfig.getDumperConfig(), taskConfig.getImporterConfig(), pipelineChannelCreator, dataSourceManager, sourceMetaDataLoader, incrementalDumperExecuteEngine);
        jobContext.getIncrementalTasks().add(incrementalTask);
    }
    
    private IngestPosition<?> getIncrementalPosition(final RuleAlteredJobContext jobContext, final TaskConfiguration taskConfig,
                                                     final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != jobContext.getInitProgress()) {
            Optional<IngestPosition<?>> position = jobContext.getInitProgress().getIncrementalPosition(taskConfig.getDumperConfig().getDataSourceName());
            if (position.isPresent()) {
                return position.get();
            }
        }
        String databaseType = taskConfig.getJobConfig().getSourceDatabaseType();
        DataSource dataSource = dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig());
        return PositionInitializerFactory.getInstance(databaseType).init(dataSource);
    }
    
    /**
     * Do cleanup work for scaling job.
     *
     * @param jobConfig job configuration
     */
    public void cleanup(final RuleAlteredJobConfiguration jobConfig) {
        try {
            cleanup0(jobConfig);
        } catch (final SQLException ex) {
            log.warn("Scaling job destroying failed", ex);
        }
    }
    
    private void cleanup0(final RuleAlteredJobConfiguration jobConfig) throws SQLException {
        DatabaseType databaseType = DatabaseTypeFactory.getInstance(jobConfig.getSourceDatabaseType());
        PositionInitializer positionInitializer = PositionInitializerFactory.getInstance(databaseType.getType());
        ShardingSpherePipelineDataSourceConfiguration sourceDataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration) PipelineDataSourceConfigurationFactory
                .newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
        for (DataSourceProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePropertiesMap(sourceDataSourceConfig.getRootConfig()).values()) {
            try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                positionInitializer.destroy(dataSource);
            }
        }
    }
}
