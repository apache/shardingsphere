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

package org.apache.shardingsphere.scaling.core.job.preparer;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.common.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.check.source.DataSourceChecker;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializer;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializerFactory;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.job.preparer.splitter.InventoryTaskSplitter;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Scaling job preparer.
 */
@Slf4j
public final class ScalingJobPreparer {
    
    private final InventoryTaskSplitter inventoryTaskSplitter = new InventoryTaskSplitter();
    
    /**
     * Do prepare work for scaling job.
     *
     * @param jobContext job context
     */
    public void prepare(final JobContext jobContext) {
        prepareTarget(jobContext.getJobConfig());
        try (DataSourceManager dataSourceManager = new DataSourceManager(jobContext.getTaskConfigs())) {
            checkDataSource(jobContext, dataSourceManager);
            initIncrementalTasks(jobContext, dataSourceManager);
            initInventoryTasks(jobContext, dataSourceManager);
            log.info("prepare, jobId={}, shardingItem={}, inventoryTasks={}, incrementalTasks={}",
                    jobContext.getJobId(), jobContext.getShardingItem(), jobContext.getInventoryTasks(), jobContext.getIncrementalTasks());
        } catch (final SQLException ex) {
            log.error("Scaling job preparing failed, jobId={}", jobContext.getJobId());
            throw new PrepareFailedException("Scaling job preparing failed, jobId=" + jobContext.getJobId(), ex);
        }
    }
    
    private void prepareTarget(final JobConfiguration jobConfig) {
        DataSourcePreparer dataSourcePreparer = EnvironmentCheckerFactory.getDataSourcePreparer(jobConfig.getHandleConfig().getDatabaseType());
        if (null == dataSourcePreparer) {
            log.info("dataSourcePreparer null, ignore prepare target");
            return;
        }
        dataSourcePreparer.prepareTargetTables(jobConfig);
    }
    
    private void checkDataSource(final JobContext jobContext, final DataSourceManager dataSourceManager) {
        checkSourceDataSources(jobContext, dataSourceManager);
        JobProgress initProgress = jobContext.getInitProgress();
        if (null == initProgress || initProgress.getStatus() == JobStatus.PREPARING_FAILURE) {
            checkTargetDataSources(jobContext, dataSourceManager);
        }
    }
    
    private void checkSourceDataSources(final JobContext jobContext, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getDatabaseType());
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDataSources().values());
        dataSourceChecker.checkVariable(dataSourceManager.getSourceDataSources().values());
    }
    
    private void checkTargetDataSources(final JobContext jobContext, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = EnvironmentCheckerFactory.newInstance(jobContext.getJobConfig().getHandleConfig().getDatabaseType());
        dataSourceChecker.checkTargetTable(dataSourceManager.getTargetDataSources().values(), jobContext.getTaskConfigs().iterator().next().getImporterConfig().getShardingColumnsMap().keySet());
    }
    
    private void initInventoryTasks(final JobContext jobContext, final DataSourceManager dataSourceManager) {
        List<InventoryTask> allInventoryTasks = new LinkedList<>();
        for (TaskConfiguration each : jobContext.getTaskConfigs()) {
            allInventoryTasks.addAll(inventoryTaskSplitter.splitInventoryData(jobContext, each, dataSourceManager));
        }
        jobContext.getInventoryTasks().addAll(allInventoryTasks);
    }
    
    private void initIncrementalTasks(final JobContext jobContext, final DataSourceManager dataSourceManager) throws SQLException {
        for (TaskConfiguration each : jobContext.getTaskConfigs()) {
            each.getDumperConfig().setPosition(getIncrementalPosition(jobContext, each, dataSourceManager));
            jobContext.getIncrementalTasks().add(ScalingTaskFactory.createIncrementalTask(each.getHandleConfig().getConcurrency(), each.getDumperConfig(), each.getImporterConfig()));
        }
    }
    
    private ScalingPosition<?> getIncrementalPosition(final JobContext jobContext, final TaskConfiguration taskConfig, final DataSourceManager dataSourceManager) throws SQLException {
        if (null != jobContext.getInitProgress()) {
            Optional<ScalingPosition<?>> positionOptional = jobContext.getInitProgress().getIncrementalPosition(taskConfig.getDumperConfig().getDataSourceName());
            if (positionOptional.isPresent()) {
                return positionOptional.get();
            }
        }
        return PositionInitializerFactory.newInstance(taskConfig.getHandleConfig().getDatabaseType()).init(dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig()));
    }
    
    /**
     * Do cleanup work for scaling job.
     *
     * @param jobContext job context
     */
    public void cleanup(final JobContext jobContext) {
        try (DataSourceManager dataSourceManager = new DataSourceManager(jobContext.getTaskConfigs())) {
            for (TaskConfiguration each : jobContext.getTaskConfigs()) {
                PositionInitializer positionInitializer = PositionInitializerFactory.newInstance(each.getHandleConfig().getDatabaseType());
                positionInitializer.destroy(dataSourceManager.getDataSource(each.getDumperConfig().getDataSourceConfig()));
            }
        } catch (final SQLException ex) {
            log.warn("Scaling job destroying failed", ex);
        }
    }
}
