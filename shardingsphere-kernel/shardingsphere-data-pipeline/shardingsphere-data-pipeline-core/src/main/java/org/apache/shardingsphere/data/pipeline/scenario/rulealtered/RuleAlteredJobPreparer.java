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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PositionInitializerFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

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
    
    static {
        ShardingSphereServiceLoader.register(DataSourceChecker.class);
    }
    
    private final InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter();
    
    /**
     * Do prepare work for scaling job.
     *
     * @param jobContext job context
     */
    public void prepare(final RuleAlteredJobContext jobContext) {
        // TODO Initialize source and target data source after tasks initialization, since dumper and importer constructor might call appendJDBCQueryProperties.
        //  But InventoryTaskSplitter need to check target tables. It need to do some refactoring for appendJDBCQueryProperties vocations.
        checkSourceDataSource(jobContext);
        prepareAndCheckTargetWithLock(jobContext);
        try {
            initIncrementalTasks(jobContext);
            initInventoryTasks(jobContext);
            log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                    jobContext.getJobId(), jobContext.getShardingItem(), jobContext.getInventoryTasks(), jobContext.getIncrementalTasks());
        } catch (final SQLException ex) {
            log.error("Scaling job preparing failed, jobId={}", jobContext.getJobId());
            throw new PipelineJobPrepareFailedException("Scaling job preparing failed, jobId=" + jobContext.getJobId(), ex);
        }
    }
    
    private void prepareAndCheckTargetWithLock(final RuleAlteredJobContext jobContext) {
        JobConfiguration jobConfig = jobContext.getJobConfig();
        // TODO the lock will be replaced
        String lockName = "prepare-" + jobConfig.getHandleConfig().getJobId();
        ShardingSphereLock lock = PipelineContext.getContextManager().getInstanceContext().getLockContext().getOrCreateSchemaLock(lockName);
        if (lock.tryLock(lockName, 1)) {
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
        prepareTarget(jobContext.getJobConfig(), jobContext.getDataSourceManager());
        JobProgress initProgress = jobContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            PipelineDataSourceWrapper targetDataSource = jobContext.getDataSourceManager().getDataSource(jobContext.getTaskConfig().getImporterConfig().getDataSourceConfig());
            checkTargetDataSource(jobContext, targetDataSource);
        }
    }
    
    private void prepareTarget(final JobConfiguration jobConfig, final PipelineDataSourceManager dataSourceManager) {
        DataSourcePreparer dataSourcePreparer = EnvironmentCheckerFactory.getDataSourcePreparer(jobConfig.getHandleConfig().getTargetDatabaseType());
        if (null == dataSourcePreparer) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        JobDataNodeLine tablesFirstDataNodes = JobDataNodeLine.unmarshal(jobConfig.getHandleConfig().getTablesFirstDataNodes());
        PrepareTargetTablesParameter prepareTargetTablesParameter = new PrepareTargetTablesParameter(tablesFirstDataNodes, jobConfig.getPipelineConfig(), dataSourceManager);
        dataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
    }
    
    private void checkSourceDataSource(final RuleAlteredJobContext jobContext) {
        DataSourceChecker dataSourceChecker = TypedSPIRegistry.getRegisteredService(DataSourceChecker.class, jobContext.getJobConfig().getHandleConfig().getSourceDatabaseType());
        Collection<PipelineDataSourceWrapper> sourceDataSources = Collections.singleton(jobContext.getSourceDataSource());
        dataSourceChecker.checkConnection(sourceDataSources);
        dataSourceChecker.checkPrivilege(sourceDataSources);
        dataSourceChecker.checkVariable(sourceDataSources);
    }
    
    private void checkTargetDataSource(final RuleAlteredJobContext jobContext, final PipelineDataSourceWrapper targetDataSource) {
        DataSourceChecker dataSourceChecker = TypedSPIRegistry.getRegisteredService(DataSourceChecker.class, jobContext.getJobConfig().getHandleConfig().getTargetDatabaseType());
        Collection<PipelineDataSourceWrapper> targetDataSources = Collections.singletonList(targetDataSource);
        dataSourceChecker.checkConnection(targetDataSources);
        dataSourceChecker.checkTargetTable(targetDataSources, jobContext.getTaskConfig().getImporterConfig().getShardingColumnsMap().keySet());
    }
    
    private void initInventoryTasks(final RuleAlteredJobContext jobContext) {
        List<InventoryTask> allInventoryTasks = inventoryTaskSplitter.splitInventoryData(jobContext);
        jobContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final RuleAlteredJobContext jobContext) throws SQLException {
        PipelineChannelFactory pipelineChannelFactory = jobContext.getRuleAlteredContext().getPipelineChannelFactory();
        ExecuteEngine incrementalDumperExecuteEngine = jobContext.getRuleAlteredContext().getIncrementalDumperExecuteEngine();
        TaskConfiguration taskConfig = jobContext.getTaskConfig();
        PipelineDataSourceManager dataSourceManager = jobContext.getDataSourceManager();
        taskConfig.getDumperConfig().setPosition(getIncrementalPosition(jobContext, taskConfig, dataSourceManager));
        PipelineTableMetaDataLoader sourceMetaDataLoader = jobContext.getSourceMetaDataLoader();
        IncrementalTask incrementalTask = new IncrementalTask(taskConfig.getHandleConfig().getConcurrency(), taskConfig.getDumperConfig(), taskConfig.getImporterConfig(),
            pipelineChannelFactory, dataSourceManager, sourceMetaDataLoader, incrementalDumperExecuteEngine);
        jobContext.getIncrementalTasks().add(incrementalTask);
    }
    
    private IngestPosition<?> getIncrementalPosition(
            final RuleAlteredJobContext jobContext, final TaskConfiguration taskConfig, final PipelineDataSourceManager dataSourceManager) throws SQLException {
        if (null != jobContext.getInitProgress()) {
            Optional<IngestPosition<?>> positionOptional = jobContext.getInitProgress().getIncrementalPosition(taskConfig.getDumperConfig().getDataSourceName());
            if (positionOptional.isPresent()) {
                return positionOptional.get();
            }
        }
        String databaseType = taskConfig.getHandleConfig().getSourceDatabaseType();
        DataSource dataSource = dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig());
        return PositionInitializerFactory.getPositionInitializer(databaseType).init(dataSource);
    }
    
    /**
     * Do cleanup work for scaling job.
     *
     * @param jobConfig job configuration
     */
    public void cleanup(final JobConfiguration jobConfig) {
        try {
            cleanup0(jobConfig);
        } catch (final SQLException ex) {
            log.warn("Scaling job destroying failed", ex);
        }
    }
    
    private void cleanup0(final JobConfiguration jobConfig) throws SQLException {
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType(jobConfig.getHandleConfig().getSourceDatabaseType());
        PositionInitializer positionInitializer = PositionInitializerFactory.getPositionInitializer(databaseType.getName());
        ShardingSpherePipelineDataSourceConfiguration sourceDataSourceConfig = (ShardingSpherePipelineDataSourceConfiguration)
                PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getPipelineConfig().getSource().getType(), jobConfig.getPipelineConfig().getSource().getParameter());
        for (DataSourceProperties each : new YamlDataSourceConfigurationSwapper().getDataSourcePropertiesMap(sourceDataSourceConfig.getRootConfig()).values()) {
            try (PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(each), databaseType)) {
                positionInitializer.destroy(dataSource);
            }
        }
    }
}
