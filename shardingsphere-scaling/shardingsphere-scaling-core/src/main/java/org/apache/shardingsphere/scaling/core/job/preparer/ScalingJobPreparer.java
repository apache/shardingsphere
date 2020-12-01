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
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceChecker;
import org.apache.shardingsphere.scaling.core.job.preparer.checker.DataSourceCheckerCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.preparer.resumer.ScalingPositionResumer;
import org.apache.shardingsphere.scaling.core.job.preparer.splitter.InventoryDataTaskSplitter;
import org.apache.shardingsphere.scaling.core.job.task.DefaultScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.schedule.JobStatus;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Scaling job preparer.
 */
@Slf4j
public final class ScalingJobPreparer {
    
    private final ScalingTaskFactory scalingTaskFactory = new DefaultScalingTaskFactory();
    
    private final InventoryDataTaskSplitter inventoryDataTaskSplitter = new InventoryDataTaskSplitter();
    
    private final ScalingPositionResumer scalingPositionResumer = new ScalingPositionResumer();
    
    /**
     * Do prepare work for scaling job.
     *
     * @param scalingJob scaling job
     */
    public void prepare(final ScalingJob scalingJob) {
        String databaseType = scalingJob.getTaskConfigs().get(0).getDumperConfig().getDataSourceConfig().getDatabaseType().getName();
        try (DataSourceManager dataSourceManager = new DataSourceManager(scalingJob.getTaskConfigs())) {
            checkDataSources(databaseType, dataSourceManager);
            ResumeBreakPointManager resumeBreakPointManager = getResumeBreakPointManager(databaseType, scalingJob);
            if (resumeBreakPointManager.isResumable()) {
                scalingPositionResumer.resumePosition(scalingJob, dataSourceManager, resumeBreakPointManager);
            } else {
                initIncrementalTasks(databaseType, scalingJob, dataSourceManager);
                initInventoryDataTasks(databaseType, scalingJob, dataSourceManager);
                scalingPositionResumer.persistPosition(scalingJob, resumeBreakPointManager);
            }
            scalingJob.setDataConsistencyChecker(initDataConsistencyChecker(databaseType, scalingJob));
        } catch (final PrepareFailedException ex) {
            log.error("Preparing scaling job {} failed", scalingJob.getJobId(), ex);
            scalingJob.setStatus(JobStatus.PREPARING_FAILURE.name());
        }
    }
    
    private ResumeBreakPointManager getResumeBreakPointManager(final String databaseType, final ScalingJob scalingJob) {
        return ResumeBreakPointManagerFactory.newInstance(databaseType,
                ScalingTaskUtil.getScalingListenerPath(scalingJob.getJobId(), ScalingConstant.POSITION, scalingJob.getShardingItem()));
    }
    
    private void checkDataSources(final String databaseType, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerCheckerFactory.newInstanceDataSourceChecker(databaseType);
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDataSources().values());
        dataSourceChecker.checkVariable(dataSourceManager.getSourceDataSources().values());
    }
    
    private void initInventoryDataTasks(final String databaseType, final ScalingJob scalingJob, final DataSourceManager dataSourceManager) {
        List<ScalingTask> allInventoryDataTasks = new LinkedList<>();
        for (TaskConfiguration each : scalingJob.getTaskConfigs()) {
            allInventoryDataTasks.addAll(inventoryDataTaskSplitter.splitInventoryData(databaseType, each, dataSourceManager));
        }
        scalingJob.getInventoryTasks().addAll(allInventoryDataTasks);
    }
    
    private void initIncrementalTasks(final String databaseType, final ScalingJob scalingJob, final DataSourceManager dataSourceManager) {
        for (TaskConfiguration each : scalingJob.getTaskConfigs()) {
            DataSourceConfiguration dataSourceConfig = each.getDumperConfig().getDataSourceConfig();
            each.getDumperConfig().setPositionManager(PositionManagerFactory.newInstance(databaseType, dataSourceManager.getDataSource(dataSourceConfig)));
            scalingJob.getIncrementalTasks().add(scalingTaskFactory.createIncrementalTask(each.getJobConfig().getConcurrency(), each.getDumperConfig(), each.getImporterConfig()));
        }
    }
    
    private DataConsistencyChecker initDataConsistencyChecker(final String databaseType, final ScalingJob scalingJob) {
        return DataConsistencyCheckerFactory.newInstance(databaseType, scalingJob);
    }
}
