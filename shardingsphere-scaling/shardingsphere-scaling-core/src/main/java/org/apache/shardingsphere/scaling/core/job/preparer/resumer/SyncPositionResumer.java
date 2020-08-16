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

package org.apache.shardingsphere.scaling.core.job.preparer.resumer;

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.preparer.utils.JobPrepareUtil;
import org.apache.shardingsphere.scaling.core.job.task.DefaultSyncTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.SyncTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryDataScalingTaskGroup;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Synchronize position resumer.
 */
public final class SyncPositionResumer {
    
    private final SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
    
    /**
     * Resume position from resume from break-point manager.
     *
     * @param shardingScalingJob sharding scaling job
     * @param dataSourceManager dataSource manager
     * @param resumeBreakPointManager resume from break-point manager
     */
    public void resumePosition(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        resumeInventoryPosition(shardingScalingJob, dataSourceManager, resumeBreakPointManager);
        resumeIncrementalPosition(shardingScalingJob, resumeBreakPointManager);
    }
    
    private void resumeInventoryPosition(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        List<ScalingTask<InventoryPosition>> allInventoryDataTasks = getAllInventoryDataTasks(shardingScalingJob, dataSourceManager, resumeBreakPointManager);
        for (Collection<ScalingTask<InventoryPosition>> each : JobPrepareUtil.groupInventoryDataTasks(shardingScalingJob.getSyncConfigurations().get(0).getConcurrency(), allInventoryDataTasks)) {
            shardingScalingJob.getInventoryDataTasks().add(syncTaskFactory.createInventoryDataSyncTaskGroup(each));
        }
    }
    
    private List<ScalingTask<InventoryPosition>> getAllInventoryDataTasks(
            final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        List<ScalingTask<InventoryPosition>> result = new LinkedList<>();
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(each.getDumperConfiguration().getDataSourceConfiguration()));
            for (Entry<String, PositionManager<InventoryPosition>> entry : getInventoryPositionMap(each.getDumperConfiguration(), resumeBreakPointManager).entrySet()) {
                result.add(syncTaskFactory.createInventoryDataSyncTask(newSyncConfiguration(each, metaDataManager, entry)));
            }
        }
        return result;
    }
    
    private SyncConfiguration newSyncConfiguration(final SyncConfiguration syncConfiguration, final MetaDataManager metaDataManager, final Entry<String, PositionManager<InventoryPosition>> entry) {
        String[] splitTable = entry.getKey().split("#");
        DumperConfiguration splitDumperConfig = DumperConfiguration.clone(syncConfiguration.getDumperConfiguration());
        splitDumperConfig.setTableName(splitTable[0].split("\\.")[1]);
        splitDumperConfig.setPositionManager(entry.getValue());
        if (2 == splitTable.length) {
            splitDumperConfig.setSpiltNum(Integer.parseInt(splitTable[1]));
        }
        splitDumperConfig.setPrimaryKey(metaDataManager.getTableMetaData(splitDumperConfig.getTableName()).getPrimaryKeyColumns().get(0));
        return new SyncConfiguration(syncConfiguration.getConcurrency(), syncConfiguration.getTableNameMap(),
                splitDumperConfig, syncConfiguration.getImporterConfiguration());
    }
    
    private Map<String, PositionManager<InventoryPosition>> getInventoryPositionMap(
            final DumperConfiguration dumperConfiguration, final ResumeBreakPointManager resumeBreakPointManager) {
        Pattern pattern = Pattern.compile(String.format("%s\\.\\w+(#\\d+)?", dumperConfiguration.getDataSourceName()));
        return resumeBreakPointManager.getInventoryPositionManagerMap().entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Entry::getKey, Map.Entry::getValue));
    }
    
    private void resumeIncrementalPosition(final ShardingScalingJob shardingScalingJob, final ResumeBreakPointManager resumeBreakPointManager) {
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            each.getDumperConfiguration().setPositionManager(resumeBreakPointManager.getIncrementalPositionManagerMap().get(each.getDumperConfiguration().getDataSourceName()));
            shardingScalingJob.getIncrementalDataTasks().add(syncTaskFactory.createIncrementalDataSyncTask(each));
        }
    }
    
    /**
     * Persist position.
     *
     * @param shardingScalingJob sharding scaling job
     * @param resumeBreakPointManager resume from break-point manager
     */
    public void persistPosition(final ShardingScalingJob shardingScalingJob, final ResumeBreakPointManager resumeBreakPointManager) {
        persistIncrementalPosition(shardingScalingJob.getIncrementalDataTasks(), resumeBreakPointManager);
        persistInventoryPosition(shardingScalingJob.getInventoryDataTasks(), resumeBreakPointManager);
    }
    
    private void persistInventoryPosition(final List<ScalingTask<InventoryPosition>> inventoryDataTasks, final ResumeBreakPointManager resumeBreakPointManager) {
        for (ScalingTask<InventoryPosition> each : inventoryDataTasks) {
            if (each instanceof InventoryDataScalingTaskGroup) {
                putInventoryDataScalingTask(((InventoryDataScalingTaskGroup) each).getScalingTasks(), resumeBreakPointManager);
            }
        }
        resumeBreakPointManager.persistInventoryPosition();
    }
    
    private void putInventoryDataScalingTask(final Collection<ScalingTask<InventoryPosition>> scalingTasks, final ResumeBreakPointManager resumeBreakPointManager) {
        for (ScalingTask<InventoryPosition> each : scalingTasks) {
            resumeBreakPointManager.getInventoryPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
    }
    
    private void persistIncrementalPosition(final List<ScalingTask<IncrementalPosition>> incrementalDataTasks, final ResumeBreakPointManager resumeBreakPointManager) {
        for (ScalingTask<IncrementalPosition> each : incrementalDataTasks) {
            resumeBreakPointManager.getIncrementalPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
        resumeBreakPointManager.persistIncrementalPosition();
    }
}
