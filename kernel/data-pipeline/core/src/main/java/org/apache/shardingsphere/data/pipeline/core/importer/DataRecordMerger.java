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

package org.apache.shardingsphere.data.pipeline.core.importer;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord.Key;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Record merger.
 */
public final class DataRecordMerger {
    
    /**
     * Group by table and type.
     *
     * @param dataRecords data records
     * @return grouped data records
     */
    public List<GroupedDataRecord> group(final List<DataRecord> dataRecords) {
        int insertCount = 0;
        Map<Key, Boolean> duplicateKeyMap = new HashMap<>();
        Set<String> tableNames = new LinkedHashSet<>();
        for (DataRecord each : dataRecords) {
            if (IngestDataChangeType.INSERT.equals(each.getType())) {
                insertCount++;
            }
            tableNames.add(each.getTableName());
            Key key = getKeyFromDataRecord(each);
            duplicateKeyMap.put(key, duplicateKeyMap.containsKey(key));
        }
        List<GroupedDataRecord> result = new ArrayList<>(100);
        if (insertCount == dataRecords.size()) {
            Map<String, List<DataRecord>> tableGroup = dataRecords.stream().collect(Collectors.groupingBy(DataRecord::getTableName));
            for (Entry<String, List<DataRecord>> entry : tableGroup.entrySet()) {
                result.add(new GroupedDataRecord(entry.getKey(), entry.getValue(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }
            return result;
        }
        Map<String, List<DataRecord>> nonBatchRecords = new LinkedHashMap<>();
        Map<String, Map<String, List<DataRecord>>> batchDataRecords = new LinkedHashMap<>();
        for (DataRecord each : dataRecords) {
            Key key = getKeyFromDataRecord(each);
            if (duplicateKeyMap.getOrDefault(key, false)) {
                nonBatchRecords.computeIfAbsent(each.getTableName(), ignored -> new LinkedList<>()).add(each);
                continue;
            }
            Map<String, List<DataRecord>> recordMap = batchDataRecords.computeIfAbsent(each.getTableName(), ignored -> new HashMap<>());
            recordMap.computeIfAbsent(each.getType(), ignored -> new LinkedList<>()).add(each);
        }
        for (String each : tableNames) {
            Map<String, List<DataRecord>> batchMap = batchDataRecords.getOrDefault(each, Collections.emptyMap());
            List<DataRecord> nonBatchRecordMap = nonBatchRecords.getOrDefault(each, Collections.emptyList());
            result.add(new GroupedDataRecord(each, batchMap.getOrDefault(IngestDataChangeType.INSERT, Collections.emptyList()),
                    batchMap.getOrDefault(IngestDataChangeType.UPDATE, Collections.emptyList()), batchMap.getOrDefault(IngestDataChangeType.DELETE, Collections.emptyList()), nonBatchRecordMap));
        }
        return result;
    }
    
    private Key getKeyFromDataRecord(final DataRecord dataRecord) {
        return IngestDataChangeType.DELETE.equals(dataRecord.getType()) ? dataRecord.getOldKey() : dataRecord.getKey();
    }
}
