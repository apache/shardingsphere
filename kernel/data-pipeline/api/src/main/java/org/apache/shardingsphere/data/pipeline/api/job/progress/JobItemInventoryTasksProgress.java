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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Job item inventory tasks progress.
 */
@RequiredArgsConstructor
@Getter
public final class JobItemInventoryTasksProgress {
    
    private final Map<String, InventoryTaskProgress> inventoryTaskProgressMap;
    
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
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPosition()));
    }
}
