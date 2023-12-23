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

package org.apache.shardingsphere.data.pipeline.core.ingest.record.group;

import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord.Key;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Record group engine.
 */
public final class DataRecordGroupEngine {
    
    /**
     * Group by table and operation type.
     *
     * @param records data records
     * @return grouped data records
     */
    public Collection<GroupedDataRecord> group(final Collection<DataRecord> records) {
        Map<Key, Boolean> duplicateKeys = getDuplicateKeys(records);
        Collection<String> tableNames = new LinkedHashSet<>();
        Map<String, List<DataRecord>> nonBatchRecords = new LinkedHashMap<>();
        Map<String, Map<IngestDataChangeType, List<DataRecord>>> batchDataRecords = new LinkedHashMap<>();
        for (DataRecord each : records) {
            tableNames.add(each.getTableName());
            if (duplicateKeys.getOrDefault(each.getKey(), false)) {
                nonBatchRecords.computeIfAbsent(each.getTableName(), ignored -> new LinkedList<>()).add(each);
            } else {
                batchDataRecords.computeIfAbsent(each.getTableName(), ignored -> new HashMap<>()).computeIfAbsent(each.getType(), ignored -> new LinkedList<>()).add(each);
            }
        }
        return tableNames.stream().map(each -> getGroupedDataRecord(
                each, batchDataRecords.getOrDefault(each, Collections.emptyMap()), nonBatchRecords.getOrDefault(each, Collections.emptyList()))).collect(Collectors.toList());
    }
    
    private Map<Key, Boolean> getDuplicateKeys(final Collection<DataRecord> records) {
        Map<Key, Boolean> result = new HashMap<>();
        for (DataRecord each : records) {
            Key key = each.getKey();
            result.put(key, result.containsKey(key));
        }
        return result;
    }
    
    private GroupedDataRecord getGroupedDataRecord(final String tableName, final Map<IngestDataChangeType, List<DataRecord>> batchRecords, final List<DataRecord> nonBatchRecords) {
        return new GroupedDataRecord(tableName, batchRecords.getOrDefault(IngestDataChangeType.INSERT, Collections.emptyList()),
                batchRecords.getOrDefault(IngestDataChangeType.UPDATE, Collections.emptyList()), batchRecords.getOrDefault(IngestDataChangeType.DELETE, Collections.emptyList()), nonBatchRecords);
    }
}
