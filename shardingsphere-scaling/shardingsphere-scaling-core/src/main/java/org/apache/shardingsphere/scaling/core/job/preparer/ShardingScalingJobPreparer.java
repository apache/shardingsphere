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
import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceChecker;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceCheckerCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.preparer.resumer.SyncPositionResumer;
import org.apache.shardingsphere.scaling.core.job.preparer.splitter.InventoryDataTaskSplitter;
import org.apache.shardingsphere.scaling.core.job.preparer.utils.JobPrepareUtil;
import org.apache.shardingsphere.scaling.core.job.task.DefaultSyncTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.SyncTaskFactory;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding scaling job preparer.
 */
@Slf4j
public final class ShardingScalingJobPreparer {
    
    private final SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
    
    private final InventoryDataTaskSplitter inventoryDataTaskSplitter = new InventoryDataTaskSplitter();
    
    private final SyncPositionResumer syncPositionResumer = new SyncPositionResumer();
    
    /**
     * Do prepare work for sharding scaling job.
     *
     * @param shardingScalingJob sharding scaling job
     */
    public void prepare(final ShardingScalingJob shardingScalingJob) {
        String databaseType = shardingScalingJob.getSyncConfigurations().get(0).getDumperConfiguration().getDataSourceConfiguration().getDatabaseType().getName();
        try (DataSourceManager dataSourceManager = new DataSourceManager(shardingScalingJob.getSyncConfigurations())) {
            checkDatasources(databaseType, dataSourceManager);
            ResumeBreakPointManager resumeBreakPointManager = getResumeBreakPointManager(databaseType, shardingScalingJob);
            if (resumeBreakPointManager.isResumable()) {
                syncPositionResumer.resumePosition(shardingScalingJob, dataSourceManager, resumeBreakPointManager);
                return;
            }
            initIncrementalDataTasks(databaseType, shardingScalingJob, dataSourceManager);
            initInventoryDataTasks(shardingScalingJob, dataSourceManager);
            syncPositionResumer.persistPosition(shardingScalingJob, resumeBreakPointManager);
        } catch (final PrepareFailedException ex) {
            log.warn("Preparing sharding scaling job {} : {} failed", shardingScalingJob.getJobId(), shardingScalingJob.getJobName(), ex);
            shardingScalingJob.setStatus(SyncTaskControlStatus.PREPARING_FAILURE.name());
        }
    }
    
    private ResumeBreakPointManager getResumeBreakPointManager(final String databaseType, final ShardingScalingJob shardingScalingJob) {
        return ResumeBreakPointManagerFactory.newInstance(databaseType, String.format("/%s/position/%d", shardingScalingJob.getJobName(), shardingScalingJob.getShardingItem()));
    }
    
    private void checkDatasources(final String databaseType, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerCheckerFactory.newInstanceDataSourceChecker(databaseType);
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDatasources().values());
        dataSourceChecker.checkVariable(dataSourceManager.getSourceDatasources().values());
    }
    
    private void initInventoryDataTasks(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager) {
        List<ScalingTask<InventoryPosition>> allInventoryDataTasks = new LinkedList<>();
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            allInventoryDataTasks.addAll(inventoryDataTaskSplitter.splitInventoryData(each, dataSourceManager));
        }
        for (Collection<ScalingTask<InventoryPosition>> each : JobPrepareUtil.groupInventoryDataTasks(shardingScalingJob.getSyncConfigurations().get(0).getConcurrency(), allInventoryDataTasks)) {
            shardingScalingJob.getInventoryDataTasks().add(syncTaskFactory.createInventoryDataSyncTaskGroup(each));
        }
    }
    
    private void initIncrementalDataTasks(final String databaseType, final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager) {
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            DataSourceConfiguration dataSourceConfiguration = each.getDumperConfiguration().getDataSourceConfiguration();
            each.getDumperConfiguration().setPositionManager(instancePositionManager(databaseType, dataSourceManager.getDataSource(dataSourceConfiguration)));
            shardingScalingJob.getIncrementalDataTasks().add(syncTaskFactory.createIncrementalDataSyncTask(each.getConcurrency(), each.getDumperConfiguration(), each.getImporterConfiguration()));
        }
    }
    
    private PositionManager<? extends IncrementalPosition> instancePositionManager(final String databaseType, final DataSource dataSource) {
        PositionManager<? extends IncrementalPosition> result = PositionManagerFactory.newInstance(databaseType, dataSource);
        result.getPosition();
        return result;
    }
}
