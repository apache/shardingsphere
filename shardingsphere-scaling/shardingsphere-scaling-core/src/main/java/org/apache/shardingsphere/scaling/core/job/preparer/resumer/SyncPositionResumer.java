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

import org.apache.shardingsphere.scaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumablePositionManager;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sync position resumer.
 */
public final class SyncPositionResumer {
    
    private final SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
    
    /**
     * Resume position from this position manager.
     *
     * @param shardingScalingJob       sharding scaling job
     * @param dataSourceManager        dataSource manager
     * @param resumablePositionManager which position manager resume from
     */
    public void resumePosition(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumablePositionManager resumablePositionManager) {
        resumeInventoryPosition(shardingScalingJob, dataSourceManager, resumablePositionManager);
        resumeIncrementalPosition(shardingScalingJob, resumablePositionManager);
    }
    
    private void resumeInventoryPosition(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumablePositionManager resumablePositionManager) {
        List<ScalingTask> allInventoryDataTasks = getAllInventoryDataTasks(shardingScalingJob, dataSourceManager, resumablePositionManager);
        for (Collection<ScalingTask> each : JobPrepareUtil.groupInventoryDataTasks(shardingScalingJob.getSyncConfigurations().get(0).getConcurrency(), allInventoryDataTasks)) {
            shardingScalingJob.getInventoryDataTasks().add(syncTaskFactory.createInventoryDataSyncTaskGroup(each));
        }
    }
    
    private List<ScalingTask> getAllInventoryDataTasks(
            final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager, final ResumablePositionManager resumablePositionManager) {
        List<ScalingTask> result = new LinkedList<>();
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(each.getDumperConfiguration().getDataSourceConfiguration()));
            for (Map.Entry<String, PositionManager<PrimaryKeyPosition>> entry : getInventoryPositionMap(each.getDumperConfiguration(), resumablePositionManager).entrySet()) {
                result.add(syncTaskFactory.createInventoryDataSyncTask(newSyncConfiguration(each, metaDataManager, entry)));
            }
        }
        return result;
    }
    
    private SyncConfiguration newSyncConfiguration(
            final SyncConfiguration syncConfiguration, final MetaDataManager metaDataManager, final Map.Entry<String, PositionManager<PrimaryKeyPosition>> entry) {
        String[] splitTable = entry.getKey().split("#");
        RdbmsConfiguration splitDumperConfig = RdbmsConfiguration.clone(syncConfiguration.getDumperConfiguration());
        splitDumperConfig.setTableName(splitTable[0].split("\\.")[1]);
        splitDumperConfig.setPositionManager(entry.getValue());
        if (splitTable.length == 2) {
            splitDumperConfig.setSpiltNum(Integer.parseInt(splitTable[1]));
        }
        splitDumperConfig.setPrimaryKey(metaDataManager.getTableMetaData(splitDumperConfig.getTableName()).getPrimaryKeyColumns().get(0));
        return new SyncConfiguration(syncConfiguration.getConcurrency(), syncConfiguration.getTableNameMap(),
                splitDumperConfig, RdbmsConfiguration.clone(syncConfiguration.getImporterConfiguration()));
    }
    
    private Map<String, PositionManager<PrimaryKeyPosition>> getInventoryPositionMap(
            final RdbmsConfiguration dumperConfiguration, final ResumablePositionManager resumablePositionManager) {
        Pattern pattern = Pattern.compile(String.format("%s\\.\\w+(#\\d+)?", dumperConfiguration.getDataSourceName()));
        return resumablePositionManager.getInventoryPositionManagerMap().entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    private void resumeIncrementalPosition(final ShardingScalingJob shardingScalingJob, final ResumablePositionManager resumablePositionManager) {
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            each.getDumperConfiguration().setPositionManager(resumablePositionManager.getIncrementalPositionManagerMap().get(each.getDumperConfiguration().getDataSourceName()));
            shardingScalingJob.getIncrementalDataTasks().add(syncTaskFactory.createIncrementalDataSyncTask(each));
        }
    }
    
    /**
     * Persist position when init sync job.
     *
     * @param shardingScalingJob       sync job
     * @param resumablePositionManager which position manager resume from
     */
    public void persistPosition(final ShardingScalingJob shardingScalingJob, final ResumablePositionManager resumablePositionManager) {
        persistIncrementalPosition(shardingScalingJob.getIncrementalDataTasks(), resumablePositionManager);
        persistInventoryPosition(shardingScalingJob.getInventoryDataTasks(), resumablePositionManager);
    }
    
    private void persistInventoryPosition(final List<ScalingTask> inventoryDataTasks, final ResumablePositionManager resumablePositionManager) {
        for (ScalingTask each : inventoryDataTasks) {
            if (each instanceof InventoryDataScalingTaskGroup) {
                putInventoryDataScalingTask(((InventoryDataScalingTaskGroup) each).getScalingTasks(), resumablePositionManager);
            }
        }
        resumablePositionManager.persistInventoryPosition();
    }
    
    private void putInventoryDataScalingTask(final Collection<ScalingTask> scalingTasks, final ResumablePositionManager resumablePositionManager) {
        for (ScalingTask each : scalingTasks) {
            resumablePositionManager.getInventoryPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
    }
    
    private void persistIncrementalPosition(final List<ScalingTask> incrementalDataTasks, final ResumablePositionManager resumablePositionManager) {
        for (ScalingTask each : incrementalDataTasks) {
            resumablePositionManager.getIncrementalPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
        resumablePositionManager.persistIncrementalPosition();
    }
}
