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

package org.apache.shardingsphere.scaling.core.job.progress;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.job.progress.yaml.JobProgressYamlSwapper;
import org.apache.shardingsphere.scaling.core.job.progress.yaml.YamlJobProgress;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Job progress.
 */
@Getter
@Setter
public final class JobProgress {
    
    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();
    
    private JobStatus status = JobStatus.RUNNING;
    
    private String databaseType;
    
    private Map<String, InventoryTaskProgress> inventoryTaskProgressMap;
    
    private Map<String, IncrementalTaskProgress> incrementalTaskProgressMap;
    
    
    /**
     * Init by string data.
     *
     * @param data string data
     * @return job progress
     */
    public static JobProgress init(final String data) {
        return JOB_PROGRESS_YAML_SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }
    
    /**
     * Get incremental position.
     *
     * @param dataSourceName data source name
     * @return incremental position
     */
    public ScalingPosition<?> getIncrementalPosition(final String dataSourceName) {
        return incrementalTaskProgressMap.get(dataSourceName).getPosition();
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, ScalingPosition<?>> getInventoryPosition(final String tableName) {
        Pattern pattern = Pattern.compile(String.format("%s(#\\d+)?", tableName));
        return inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition()));
    }
    
    @Override
    public String toString() {
        return YamlEngine.marshal(JOB_PROGRESS_YAML_SWAPPER.swapToYaml(this));
    }
    
    /**
     * Get data source.
     *
     * @return data source
     */
    public String getDataSource() {
        return incrementalTaskProgressMap.keySet().stream().findAny().orElse("");
    }
    
    /**
     * Get inventory finished percentage.
     *
     * @return finished percentage
     */
    public int getInventoryFinishedPercentage() {
        long finished = inventoryTaskProgressMap.values().stream()
                .filter(each -> each.getPosition() instanceof FinishedPosition)
                .count();
        return inventoryTaskProgressMap.isEmpty() ? 0 : (int) (finished * 100 / inventoryTaskProgressMap.size());
    }
    
    /**
     * Get incremental delay milliseconds.
     *
     * @return average delay
     */
    public long getIncrementalDelayMilliseconds() {
        List<Long> delays = incrementalTaskProgressMap.values().stream()
                .map(each -> each.getIncrementalTaskDelay().getDelayMilliseconds())
                .collect(Collectors.toList());
        return delays.isEmpty() || delays.contains(-1L) ? -1L : delays.stream().reduce(Long::sum).orElse(0L) / delays.size();
    }
}
