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

package org.apache.shardingsphere.scaling.core.job.progress.yaml;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionInitializerFactory;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *Job progress yaml swapper.
 */
public final class JobProgressYamlSwapper {
    
    /**
     * Swap to yaml.
     *
     * @param jobProgress job progress
     * @return yaml job progress
     */
    public YamlJobProgress swapToYaml(final JobProgress jobProgress) {
        YamlJobProgress result = new YamlJobProgress();
        result.setStatus(jobProgress.getStatus().name());
        result.setDatabaseType(jobProgress.getDatabaseType());
        result.setInventory(getYamlInventory(jobProgress.getInventoryTaskProgressMap()));
        result.setIncremental(getYamlIncremental(jobProgress.getIncrementalTaskProgressMap()));
        return result;
    }
    
    private YamlJobProgress.YamlInventory getYamlInventory(final Map<String, InventoryTaskProgress> inventoryTaskProgressMap) {
        YamlJobProgress.YamlInventory result = new YamlJobProgress.YamlInventory();
        result.setFinished(getFinished(inventoryTaskProgressMap));
        result.setUnfinished(getUnfinished(inventoryTaskProgressMap));
        return result;
    }
    
    private String[] getFinished(final Map<String, InventoryTaskProgress> inventoryTaskProgressMap) {
        return inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> entry.getValue().getPosition() instanceof FinishedPosition)
                .map(Entry::getKey)
                .toArray(String[]::new);
    }
    
    private Map<String, String> getUnfinished(final Map<String, InventoryTaskProgress> inventoryTaskProgressMap) {
        return inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> !(entry.getValue().getPosition() instanceof FinishedPosition))
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition().toString()));
    }
    
    private Map<String, YamlJobProgress.YamlIncremental> getYamlIncremental(final Map<String, IncrementalTaskProgress> incrementalTaskProgressMap) {
        return incrementalTaskProgressMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> {
                    YamlJobProgress.YamlIncremental yamlIncremental = new YamlJobProgress.YamlIncremental();
                    yamlIncremental.setPosition(entry.getValue().getPosition().toString());
                    yamlIncremental.setDelay(entry.getValue().getIncrementalTaskDelay());
                    return yamlIncremental;
                }));
    }
    
    /**
     * Swap to object.
     *
     * @param yamlJobProgress yaml job progress
     * @return job progress
     */
    public JobProgress swapToObject(final YamlJobProgress yamlJobProgress) {
        JobProgress result = new JobProgress();
        result.setStatus(JobStatus.valueOf(yamlJobProgress.getStatus()));
        result.setDatabaseType(yamlJobProgress.getDatabaseType());
        result.setInventoryTaskProgressMap(getInventoryTaskProgressMap(yamlJobProgress.getInventory()));
        result.setIncrementalTaskProgressMap(getIncrementalTaskProgressMap(yamlJobProgress.getDatabaseType(), yamlJobProgress.getIncremental()));
        return result;
    }
    
    private Map<String, InventoryTaskProgress> getInventoryTaskProgressMap(final YamlJobProgress.YamlInventory inventory) {
        Map<String, InventoryTaskProgress> result = Maps.newHashMap();
        result.putAll(Arrays.stream(inventory.getFinished())
                .collect(Collectors.toMap(each -> each, each -> new InventoryTaskProgress(new FinishedPosition()))));
        result.putAll(inventory.getUnfinished().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, getInventoryTaskProgressFunction())));
        return result;
    }
    
    private Function<Entry<String, String>, InventoryTaskProgress> getInventoryTaskProgressFunction() {
        return entry -> new InventoryTaskProgress(Strings.isNullOrEmpty(entry.getValue()) ? new PlaceholderPosition() : PrimaryKeyPosition.init(entry.getValue()));
    }
    
    private Map<String, IncrementalTaskProgress> getIncrementalTaskProgressMap(final String databaseType, final Map<String, YamlJobProgress.YamlIncremental> incremental) {
        return incremental.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, getIncrementalTaskProgressFunction(databaseType)));
    }
    
    private Function<Entry<String, YamlJobProgress.YamlIncremental>, IncrementalTaskProgress> getIncrementalTaskProgressFunction(final String databaseType) {
        return entry -> new IncrementalTaskProgress(PositionInitializerFactory.newInstance(databaseType).init(entry.getValue().getPosition()), entry.getValue().getDelay());
    }
}
