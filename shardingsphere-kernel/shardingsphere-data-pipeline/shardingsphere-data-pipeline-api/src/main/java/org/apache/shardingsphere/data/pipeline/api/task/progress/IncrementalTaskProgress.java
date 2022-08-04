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

package org.apache.shardingsphere.data.pipeline.api.task.progress;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;

/**
 * Incremental task progress.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class IncrementalTaskProgress implements TaskProgress {
    
    private final Map<String, IncrementalTaskProgressItem> incrementalTaskProgressItemMap;
    
    /**
     * Get incremental position.
     *
     * @return incremental position
     */
    public Optional<IngestPosition<?>> getIncrementalPosition(final String dataSourceName) {
        Optional<IncrementalTaskProgressItem> incrementalTaskProgressItem = incrementalTaskProgressItemMap.entrySet().stream()
                .filter(entry -> dataSourceName.equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .findAny();
        return incrementalTaskProgressItem.map(IncrementalTaskProgressItem::getPosition);
    }
    
    /**
     * Get incremental latest active time milliseconds.
     *
     * @return latest active time, <code>0</code> is there is no activity
     */
    public long getIncrementalLatestActiveTimeMillis() {
        List<Long> delays = incrementalTaskProgressItemMap.values().stream()
                .map(each -> each.getIncrementalTaskDelay().getLatestActiveTimeMillis())
                .collect(Collectors.toList());
        return delays.stream().reduce(Long::max).orElse(0L);
    }
}
