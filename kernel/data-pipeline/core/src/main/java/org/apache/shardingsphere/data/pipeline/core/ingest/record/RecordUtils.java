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

package org.apache.shardingsphere.data.pipeline.core.ingest.record;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Record utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecordUtils {
    
    /**
     * Extract condition columns(include primary and sharding columns) from data record.
     *
     * @param dataRecord data record
     * @param shardingColumns sharding columns
     * @return condition columns
     */
    public static List<Column> extractConditionColumns(final DataRecord dataRecord, final Collection<String> shardingColumns) {
        List<Column> result = new ArrayList<>(dataRecord.getColumns().size());
        for (Column each : dataRecord.getColumns()) {
            if (each.isUniqueKey() || shardingColumns.contains(each.getName())) {
                result.add(each);
            }
        }
        Optional<Column> uniqueKeyColumn = dataRecord.getColumns().stream().filter(Column::isUniqueKey).findFirst();
        if (uniqueKeyColumn.isPresent()) {
            return result;
        }
        return dataRecord.getColumns();
    }
}
