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

package org.apache.shardingsphere.scaling.core.job.position;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Position group.
 */
@Getter
@Setter
public final class JobPosition {
    
    private final Map<String, Position<?>> incrementalPositionMap = Maps.newHashMap();
    
    private final Map<String, Position<?>> inventoryPositionMap = Maps.newHashMap();
    
    /**
     * Get incremental position.
     *
     * @param dataSourceName data source name
     * @return incremental position
     */
    public Position<?> getIncrementalPosition(final String dataSourceName) {
        return incrementalPositionMap.get(dataSourceName);
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, Position<?>> getInventoryPosition(final String tableName) {
        Pattern pattern = Pattern.compile(String.format("%s(#\\d+)?", tableName));
        return inventoryPositionMap.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).find())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
