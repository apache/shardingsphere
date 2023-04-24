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
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data Record merger.
 */
public final class DataRecordMerger {
    
    /**
     * Merge data record if the data record has the same key.
     *
     * @param dataRecords data records
     * @return merged data records
     */
    public Map<DataRecord.Key, List<DataRecord>> merge(final List<DataRecord> dataRecords) {
        Map<DataRecord.Key, List<DataRecord>> result = new LinkedHashMap<>();
        for (DataRecord each : dataRecords) {
            if (IngestDataChangeType.DELETE.equals(each.getType())) {
                result.computeIfAbsent(each.getOldKey(), key -> new LinkedList<>()).add(each);
                continue;
            }
            result.computeIfAbsent(each.getKey(), key -> new LinkedList<>()).add(each);
        }
        return result;
    }
    
    /**
     * Group by table and type.
     *
     * @param dataRecords data records
     * @return grouped data records
     */
    public List<GroupedDataRecord> group(final List<DataRecord> dataRecords) {
        List<GroupedDataRecord> result = new ArrayList<>(100);
        Map<String, List<DataRecord>> tableGroup = dataRecords.stream().collect(Collectors.groupingBy(DataRecord::getTableName));
        for (Entry<String, List<DataRecord>> entry : tableGroup.entrySet()) {
            Map<String, List<DataRecord>> typeGroup = entry.getValue().stream().collect(Collectors.groupingBy(DataRecord::getType));
            result.add(new GroupedDataRecord(entry.getKey(), typeGroup.get(IngestDataChangeType.INSERT), typeGroup.get(IngestDataChangeType.UPDATE), typeGroup.get(IngestDataChangeType.DELETE)));
        }
        return result;
    }
}
