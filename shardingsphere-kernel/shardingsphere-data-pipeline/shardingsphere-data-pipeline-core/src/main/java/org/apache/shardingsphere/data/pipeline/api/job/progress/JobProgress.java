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

package org.apache.shardingsphere.data.pipeline.api.job.progress;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Job progress.
 */
@Getter
@Setter
public final class JobProgress {
    
    private JobStatus status = JobStatus.RUNNING;
    
    private String sourceDatabaseType;
    
    private Map<String, InventoryTaskProgress> inventoryTaskProgressMap;
    
    private Map<String, IncrementalTaskProgress> incrementalTaskProgressMap;
    
    /**
     * Get incremental position.
     *
     * @param dataSourceName data source name
     * @return incremental position
     */
    public Optional<IngestPosition<?>> getIncrementalPosition(final String dataSourceName) {
        IncrementalTaskProgress progress = incrementalTaskProgressMap.get(dataSourceName);
        return Optional.ofNullable(null != progress ? progress.getPosition() : null);
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, IngestPosition<?>> getInventoryPosition(final String tableName) {
        Pattern pattern = Pattern.compile(String.format("%s(#\\d+)?", tableName));
        return inventoryTaskProgressMap.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition()));
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
     * Get incremental latest active time milliseconds.
     *
     * @return latest active time, <code>0</code> is there is no activity
     */
    public long getIncrementalLatestActiveTimeMillis() {
        List<Long> delays = incrementalTaskProgressMap.values().stream()
                .map(each -> each.getIncrementalTaskDelay().getLatestActiveTimeMillis())
                .collect(Collectors.toList());
        return delays.stream().reduce(Long::max).orElse(0L);
    }
}
