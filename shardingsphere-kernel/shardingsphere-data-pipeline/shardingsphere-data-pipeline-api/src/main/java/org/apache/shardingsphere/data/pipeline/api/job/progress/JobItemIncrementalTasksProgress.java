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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;

/**
 * Job item incremental tasks progress.
 */
@RequiredArgsConstructor
@Getter
public final class JobItemIncrementalTasksProgress {
    
    private final Map<String, IncrementalTaskProgress> incrementalTaskProgressMap;
    
    /**
     * Get incremental position.
     * 
     * @param dataSourceName data source name
     * @return incremental position
     */
    public Optional<IngestPosition<?>> getIncrementalPosition(final String dataSourceName) {
        Optional<IncrementalTaskProgress> incrementalTaskProgress = incrementalTaskProgressMap.entrySet().stream()
                .filter(entry -> dataSourceName.equals(entry.getKey())).map(Map.Entry::getValue).findAny();
        return incrementalTaskProgress.map(IncrementalTaskProgress::getPosition);
    }
    
    /**
     * Get data source name.
     *
     * @return data source
     */
    public String getDataSourceName() {
        return incrementalTaskProgressMap.keySet().stream().findAny().orElse("");
    }
    
    /**
     * Get incremental latest active time milliseconds.
     *
     * @return latest active time, <code>0</code> means there is no activity
     */
    public long getIncrementalLatestActiveTimeMillis() {
        List<Long> delays = incrementalTaskProgressMap.values().stream()
                .map(each -> each.getIncrementalTaskDelay().getLatestActiveTimeMillis())
                .collect(Collectors.toList());
        return delays.stream().reduce(Long::max).orElse(0L);
    }
}
