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
import org.apache.shardingsphere.scaling.core.config.InventoryDumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.task.DefaultScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Scaling position resumer.
 */
public final class ScalingPositionResumer {
    
    private final ScalingTaskFactory scalingTaskFactory = new DefaultScalingTaskFactory();
    
    /**
     * Resume position from resume from break-point manager.
     *
     * @param scalingJob scaling job
     * @param dataSourceManager dataSource manager
     * @param resumeBreakPointManager resume from break-point manager
     */
    public void resumePosition(final ScalingJob scalingJob, final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        resumeInventoryPosition(scalingJob, dataSourceManager, resumeBreakPointManager);
        resumeIncrementalPosition(scalingJob, resumeBreakPointManager);
    }
    
    private void resumeInventoryPosition(final ScalingJob scalingJob, final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        scalingJob.getInventoryTasks().addAll(getAllInventoryTasks(scalingJob, dataSourceManager, resumeBreakPointManager));
    }
    
    private List<ScalingTask> getAllInventoryTasks(final ScalingJob scalingJob,
                                                                          final DataSourceManager dataSourceManager, final ResumeBreakPointManager resumeBreakPointManager) {
        List<ScalingTask> result = new LinkedList<>();
        for (TaskConfiguration each : scalingJob.getTaskConfigs()) {
            MetaDataManager metaDataManager = new MetaDataManager(dataSourceManager.getDataSource(each.getDumperConfig().getDataSourceConfig()));
            for (Entry<String, PositionManager> entry : getInventoryPositionMap(each.getDumperConfig(), resumeBreakPointManager).entrySet()) {
                result.add(scalingTaskFactory.createInventoryTask(newInventoryDumperConfig(each.getDumperConfig(), metaDataManager, entry), each.getImporterConfig()));
            }
        }
        return result;
    }
    
    private InventoryDumperConfiguration newInventoryDumperConfig(final DumperConfiguration dumperConfig, final MetaDataManager metaDataManager, final Entry<String, PositionManager> entry) {
        String[] splitTable = entry.getKey().split("#");
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(dumperConfig);
        result.setTableName(splitTable[0].split("\\.")[1]);
        result.setPositionManager(entry.getValue());
        if (2 == splitTable.length) {
            result.setShardingItem(Integer.parseInt(splitTable[1]));
        }
        result.setPrimaryKey(metaDataManager.getTableMetaData(result.getTableName()).getPrimaryKeyColumns().get(0));
        return result;
    }
    
    private Map<String, PositionManager> getInventoryPositionMap(final DumperConfiguration dumperConfig, final ResumeBreakPointManager resumeBreakPointManager) {
        Pattern pattern = Pattern.compile(String.format("%s\\.\\w+(#\\d+)?", dumperConfig.getDataSourceName()));
        return resumeBreakPointManager.getInventoryPositionManagerMap().entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Entry::getKey, Map.Entry::getValue, (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }

    private void resumeIncrementalPosition(final ScalingJob scalingJob, final ResumeBreakPointManager resumeBreakPointManager) {
        for (TaskConfiguration each : scalingJob.getTaskConfigs()) {
            each.getDumperConfig().setPositionManager(resumeBreakPointManager.getIncrementalPositionManagerMap().get(each.getDumperConfig().getDataSourceName()));
            scalingJob.getIncrementalTasks().add(scalingTaskFactory.createIncrementalTask(each.getJobConfig().getConcurrency(), each.getDumperConfig(), each.getImporterConfig()));
        }
    }
    
    /**
     * Persist position.
     *
     * @param scalingJob scaling job
     * @param resumeBreakPointManager resume from break-point manager
     */
    public void persistPosition(final ScalingJob scalingJob, final ResumeBreakPointManager resumeBreakPointManager) {
        for (ScalingTask each : scalingJob.getInventoryTasks()) {
            resumeBreakPointManager.getInventoryPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
        for (ScalingTask each : scalingJob.getIncrementalTasks()) {
            resumeBreakPointManager.getIncrementalPositionManagerMap().put(each.getTaskId(), each.getPositionManager());
        }
        resumeBreakPointManager.persistPosition();
    }
}
