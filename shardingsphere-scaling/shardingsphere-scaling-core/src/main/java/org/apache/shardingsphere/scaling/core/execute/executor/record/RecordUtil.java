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

package org.apache.shardingsphere.scaling.core.execute.executor.record;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Record utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecordUtil {
    
    /**
     * Extract primary columns index from data record.
     *
     * @param dataRecord data record
     * @return primary columns index
     */
    public static List<Integer> extractPrimaryColumns(final DataRecord dataRecord) {
        return IntStream.range(0, dataRecord.getColumnCount())
                .filter(each -> dataRecord.getColumn(each).isPrimaryKey())
                .mapToObj(each -> each)
                .collect(Collectors.toList());
    }
    
    /**
     * Extract condition columns(include primary and sharding columns) index from data record.
     *
     * @param dataRecord data record
     * @param shardingColumns sharding columns
     * @return condition columns index
     */
    public static List<Integer> extractConditionColumns(final DataRecord dataRecord, final Set<String> shardingColumns) {
        return IntStream.range(0, dataRecord.getColumnCount())
                .filter(each -> {
                    Column column = dataRecord.getColumn(each);
                    return column.isPrimaryKey() || shardingColumns.contains(column.getName());
                })
                .mapToObj(each -> each)
                .collect(Collectors.toList());
    }
    
    /**
     * Extract updated columns from data record.
     *
     * @param dataRecord data record
     * @return updated columns index
     */
    public static List<Integer> extractUpdatedColumns(final DataRecord dataRecord) {
        return IntStream.range(0, dataRecord.getColumnCount())
                .filter(each -> dataRecord.getColumn(each).isUpdated())
                .mapToObj(each -> each)
                .collect(Collectors.toList());
    }
}
