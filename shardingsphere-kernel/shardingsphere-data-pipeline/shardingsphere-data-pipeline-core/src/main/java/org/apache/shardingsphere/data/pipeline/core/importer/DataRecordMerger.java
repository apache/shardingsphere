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

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineUnexpectedDataRecordOrderException;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data Record merger.
 */
public final class DataRecordMerger {
    
    /**
     * Merge data record.
     * <pre>
     * insert + insert -&gt; exception
     * update + insert -&gt; exception
     * delete + insert -&gt; insert
     * insert + update -&gt; insert
     * update + update -&gt; update
     * delete + update -&gt; exception
     * insert + delete -&gt; delete
     * update + delete -&gt; delete
     * delete + delete -&gt; exception
     * </pre>
     *
     * @param dataRecords data records
     * @return merged data records
     */
    public List<DataRecord> merge(final List<DataRecord> dataRecords) {
        Map<DataRecord.Key, DataRecord> result = new HashMap<>();
        dataRecords.forEach(each -> {
            if (IngestDataChangeType.INSERT.equals(each.getType())) {
                mergeInsert(each, result);
            } else if (IngestDataChangeType.UPDATE.equals(each.getType())) {
                mergeUpdate(each, result);
            } else if (IngestDataChangeType.DELETE.equals(each.getType())) {
                mergeDelete(each, result);
            }
        });
        return new ArrayList<>(result.values());
    }
    
    /**
     * Group by table and type.
     *
     * @param dataRecords data records
     * @return grouped data records
     */
    public List<GroupedDataRecord> group(final List<DataRecord> dataRecords) {
        List<DataRecord> mergedDataRecords = merge(dataRecords);
        List<GroupedDataRecord> result = new ArrayList<>(100);
        Map<String, List<DataRecord>> tableGroup = mergedDataRecords.stream().collect(Collectors.groupingBy(DataRecord::getTableName));
        for (Entry<String, List<DataRecord>> entry : tableGroup.entrySet()) {
            Map<String, List<DataRecord>> typeGroup = entry.getValue().stream().collect(Collectors.groupingBy(DataRecord::getType));
            result.add(new GroupedDataRecord(entry.getKey(), typeGroup.get(IngestDataChangeType.INSERT), typeGroup.get(IngestDataChangeType.UPDATE), typeGroup.get(IngestDataChangeType.DELETE)));
        }
        return result;
    }
    
    private void mergeInsert(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = dataRecords.get(dataRecord.getKey());
        if (null != beforeDataRecord && !IngestDataChangeType.DELETE.equals(beforeDataRecord.getType())) {
            throw new PipelineUnexpectedDataRecordOrderException(beforeDataRecord, dataRecord);
        }
        dataRecords.put(dataRecord.getKey(), dataRecord);
    }
    
    private void mergeUpdate(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = checkUpdatedPrimaryKey(dataRecord) ? dataRecords.get(dataRecord.getOldKey()) : dataRecords.get(dataRecord.getKey());
        if (null == beforeDataRecord) {
            dataRecords.put(dataRecord.getKey(), dataRecord);
            return;
        }
        ShardingSpherePreconditions.checkState(!IngestDataChangeType.DELETE.equals(beforeDataRecord.getType()), new UnsupportedSQLOperationException("Not Delete"));
        if (checkUpdatedPrimaryKey(dataRecord)) {
            dataRecords.remove(dataRecord.getOldKey());
        }
        if (IngestDataChangeType.INSERT.equals(beforeDataRecord.getType())) {
            DataRecord mergedDataRecord = mergeColumn(beforeDataRecord, dataRecord);
            mergedDataRecord.setTableName(dataRecord.getTableName());
            mergedDataRecord.setType(IngestDataChangeType.INSERT);
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
            return;
        }
        if (IngestDataChangeType.UPDATE.equals(beforeDataRecord.getType())) {
            DataRecord mergedDataRecord = mergeColumn(beforeDataRecord, dataRecord);
            mergedDataRecord.setTableName(dataRecord.getTableName());
            mergedDataRecord.setType(IngestDataChangeType.UPDATE);
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
        }
    }
    
    private void mergeDelete(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = dataRecords.get(dataRecord.getKey());
        if (null != beforeDataRecord && (IngestDataChangeType.DELETE.equals(beforeDataRecord.getType()))) {
            throw new PipelineUnexpectedDataRecordOrderException(beforeDataRecord, dataRecord);
        }
        if (null != beforeDataRecord && IngestDataChangeType.UPDATE.equals(beforeDataRecord.getType()) && checkUpdatedPrimaryKey(beforeDataRecord)) {
            DataRecord mergedDataRecord = new DataRecord(dataRecord.getPosition(), dataRecord.getColumnCount());
            for (int i = 0; i < dataRecord.getColumnCount(); i++) {
                mergedDataRecord.addColumn(new Column(
                        dataRecord.getColumn(i).getName(),
                        dataRecord.getColumn(i).isUniqueKey() ? beforeDataRecord.getColumn(i).getOldValue() : beforeDataRecord.getColumn(i).getValue(),
                        true,
                        dataRecord.getColumn(i).isUniqueKey()));
            }
            mergedDataRecord.setTableName(dataRecord.getTableName());
            mergedDataRecord.setType(IngestDataChangeType.DELETE);
            dataRecords.remove(beforeDataRecord.getKey());
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
        } else {
            dataRecords.put(dataRecord.getKey(), dataRecord);
        }
    }
    
    private boolean checkUpdatedPrimaryKey(final DataRecord dataRecord) {
        return RecordUtil.extractPrimaryColumns(dataRecord).stream().anyMatch(Column::isUpdated);
    }
    
    private DataRecord mergeColumn(final DataRecord preDataRecord, final DataRecord curDataRecord) {
        DataRecord result = new DataRecord(curDataRecord.getPosition(), curDataRecord.getColumnCount());
        for (int i = 0; i < curDataRecord.getColumnCount(); i++) {
            result.addColumn(new Column(
                    curDataRecord.getColumn(i).getName(),
                    preDataRecord.getColumn(i).isUniqueKey()
                            ? mergePrimaryKeyOldValue(preDataRecord.getColumn(i), curDataRecord.getColumn(i))
                            : null,
                    curDataRecord.getColumn(i).getValue(),
                    preDataRecord.getColumn(i).isUpdated() || curDataRecord.getColumn(i).isUpdated(),
                    curDataRecord.getColumn(i).isUniqueKey()));
        }
        return result;
    }
    
    private Object mergePrimaryKeyOldValue(final Column beforeColumn, final Column column) {
        return beforeColumn.isUpdated() ? beforeColumn.getOldValue() : (column.isUpdated() ? column.getOldValue() : null);
    }
}
