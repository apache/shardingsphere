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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPositionFactory;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobInventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;

/**
 * YAML InventoryTask progress swapper.
 */
public final class YamlInventoryTaskProgressSwapper {
    
    /**
     * Swap to YAML.
     *
     * @param inventory inventoryTask progress
     * @return YAML inventoryTask progress
     */
    public YamlInventoryTaskProgress swapToYaml(final JobInventoryTaskProgress inventory) {
        YamlInventoryTaskProgress result = new YamlInventoryTaskProgress();
        if (inventory != null) {
            result.setFinished(getFinished(inventory));
            result.setUnfinished(getUnfinished(inventory));
        }
        return result;
    }
    
    private String[] getFinished(final JobInventoryTaskProgress jobInventoryTaskProgress) {
        return jobInventoryTaskProgress.getInventoryTaskProgressMap().entrySet().stream()
                .filter(entry -> entry.getValue().getPosition() instanceof FinishedPosition)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
    
    private Map<String, String> getUnfinished(final JobInventoryTaskProgress jobInventoryTaskProgress) {
        return jobInventoryTaskProgress.getInventoryTaskProgressMap().entrySet().stream()
                .filter(entry -> !(entry.getValue().getPosition() instanceof FinishedPosition))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPosition().toString()));
    }
    
    /**
     * Swap to object.
     *
     * @param inventory yaml inventoryTask progress
     * @return inventoryTask progress
     */
    public JobInventoryTaskProgress swapToObject(final YamlInventoryTaskProgress inventory) {
        if (null == inventory) {
            return null;
        }
        Map<String, InventoryTaskProgress> inventoryTaskProgressItemMap = new HashMap<>();
        inventoryTaskProgressItemMap.putAll(Arrays.stream(inventory.getFinished())
                .collect(Collectors.toMap(key -> key, value -> new InventoryTaskProgress(new FinishedPosition()))));
        inventoryTaskProgressItemMap.putAll(inventory.getUnfinished().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, getInventoryTaskProgressFunction())));
        return new JobInventoryTaskProgress(inventoryTaskProgressItemMap);
    }
    
    private Function<Map.Entry<String, String>, InventoryTaskProgress> getInventoryTaskProgressFunction() {
        return entry -> new InventoryTaskProgress(
                Strings.isNullOrEmpty(entry.getValue()) ? new PlaceholderPosition() : PrimaryKeyPositionFactory.newInstance(entry.getValue()));
    }
}
