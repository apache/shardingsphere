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
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.PrepareTargetTablesParameter;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobPrepareFailedException;
import org.apache.shardingsphere.data.pipeline.core.prepare.InventoryTaskSplitter;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTaskFactory;
import org.apache.shardingsphere.data.pipeline.spi.check.datasource.DataSourceChecker;
import org.apache.shardingsphere.data.pipeline.spi.ingest.position.PositionInitializer;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.DataSourcePreparer;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializerFactory;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
        prepareTarget(jobContext.getJobConfig());
        try (DataSourceManager dataSourceManager = new DataSourceManager()) {
            initDataSourceManager(dataSourceManager, jobContext.getTaskConfigs());
            checkDataSource(jobContext, dataSourceManager);
            initIncrementalTasks(jobContext, dataSourceManager);
            initInventoryTasks(jobContext, dataSourceManager);
            log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                    jobContext.getJobId(), jobContext.getShardingItem(), jobContext.getInventoryTasks(), jobContext.getIncrementalTasks());
        } catch (final SQLException ex) {
            log.error("Scaling job preparing failed, jobId={}", jobContext.getJobId());
            throw new PipelineJobPrepareFailedException("Scaling job preparing failed, jobId=" + jobContext.getJobId(), ex);
        }
    }
    
    private void prepareTarget(final JobConfiguration jobConfig) {
        DataSourcePreparer dataSourcePreparer = EnvironmentCheckerFactory.getDataSourcePreparer(jobConfig.getHandleConfig().getTargetDatabaseType());
        if (null == dataSourcePreparer) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        JobDataNodeLine tablesFirstDataNodes = JobDataNodeLine.unmarshal(jobConfig.getHandleConfig().getTablesFirstDataNodes());
        PrepareTargetTablesParameter prepareTargetTablesParameter = new PrepareTargetTablesParameter(tablesFirstDataNodes, jobConfig.getRuleConfig());
        dataSourcePreparer.prepareTargetTables(prepareTargetTablesParameter);
    }
    
    private void initDataSourceManager(final DataSourceManager dataSourceManager, final List<TaskConfiguration> taskConfigs) {
        for (TaskConfiguration taskConfig : taskConfigs) {
            JDBCDataSourceConfiguration dataSourceConfig = taskConfig.getDumperConfig().getDataSourceConfig();
            dataSourceManager.createSourceDataSource(dataSourceConfig);
        }
        dataSourceManager.createTargetDataSource(taskConfigs.iterator().next().getImporterConfig().getDataSourceConfig());
    }
    
    private void checkDataSource(final RuleAlteredJobContext jobContext, final DataSourceManager dataSourceManager) {
        checkSourceDataSources(jobContext, dataSourceManager);
        JobProgress initProgress = jobContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            checkTargetDataSources(jobContext, dataSourceManager);
        }
    }
    
    private void checkSourceDataSources(final RuleAlteredJobContext jobContext, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getSourceDatabaseType());
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDataSources().values());
        dataSourceChecker.checkVariable(dataSourceManager.getSourceDataSources().values());
    }
    
    private void checkTargetDataSources(final RuleAlteredJobContext jobContext, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getTargetDatabaseType());
        dataSourceChecker.checkTargetTable(dataSourceManager.getTargetDataSources().values(), jobContext.getTaskConfigs().iterator().next().getImporterConfig().getShardingColumnsMap().keySet());
    }
    
    private void initInventoryTasks(final RuleAlteredJobContext jobContext, final DataSourceManager dataSourceManager) {
        List<InventoryTask> allInventoryTasks = new LinkedList<>();
        for (TaskConfiguration each : jobContext.getTaskConfigs()) {
            allInventoryTasks.addAll(inventoryTaskSplitter.splitInventoryData(jobContext, each, dataSourceManager));
        }
        jobContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final RuleAlteredJobContext jobContext, final DataSourceManager dataSourceManager) throws SQLException {
        for (TaskConfiguration each : jobContext.getTaskConfigs()) {
            each.getDumperConfig().setPosition(getIncrementalPosition(jobContext, each, dataSourceManager));
            jobContext.getIncrementalTasks().add(PipelineTaskFactory.createIncrementalTask(each.getHandleConfig().getConcurrency(), each.getDumperConfig(), each.getImporterConfig()));
        }
    }
    
    private IngestPosition<?> getIncrementalPosition(final RuleAlteredJobContext jobContext, final TaskConfiguration taskConfig, final DataSourceManager dataSourceManager) throws SQLException {
        if (null != jobContext.getInitProgress()) {
            Optional<IngestPosition<?>> positionOptional = jobContext.getInitProgress().getIncrementalPosition(taskConfig.getDumperConfig().getDataSourceName());
            if (positionOptional.isPresent()) {
                return positionOptional.get();
            }
        }
        return PositionInitializerFactory.newInstance(taskConfig.getHandleConfig().getSourceDatabaseType()).init(dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig()));
    }
    
    /**
     * Do cleanup work for scaling job.
     *
     * @param jobContext job context
     */
    public void cleanup(final RuleAlteredJobContext jobContext) {
        try (DataSourceManager dataSourceManager = new DataSourceManager()) {
            initDataSourceManager(dataSourceManager, jobContext.getTaskConfigs());
            for (TaskConfiguration each : jobContext.getTaskConfigs()) {
                PositionInitializer positionInitializer = PositionInitializerFactory.newInstance(each.getHandleConfig().getSourceDatabaseType());
                positionInitializer.destroy(dataSourceManager.getDataSource(each.getDumperConfig().getDataSourceConfig()));
            }
        } catch (final SQLException ex) {
            log.warn("Scaling job destroying failed", ex);
        }
    }
}
