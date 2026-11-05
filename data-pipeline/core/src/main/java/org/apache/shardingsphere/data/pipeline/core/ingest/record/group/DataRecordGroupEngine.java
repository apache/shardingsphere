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

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.data.PipelineUnexpectedDataRecordOrderException;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Data Record group engine.
 */
public final class DataRecordGroupEngine {
    
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
            if (PipelineSQLOperationType.INSERT == each.getType()) {
                mergeInsert(each, result);
            } else if (PipelineSQLOperationType.UPDATE == each.getType()) {
                mergeUpdate(each, result);
            } else if (PipelineSQLOperationType.DELETE == each.getType()) {
                mergeDelete(each, result);
            }
        });
        return new ArrayList<>(result.values());
    }
    
    /**
     * Group by table and type.
     *
     * @param dataRecords data records, related table must have primary key or unique key
     * @return grouped data records
     * @throws IllegalArgumentException if related table has no primary key or unique key
     */
    public List<GroupedDataRecord> group(final List<DataRecord> dataRecords) {
        DataRecord firstDataRecord = dataRecords.get(0);
        ShardingSpherePreconditions.checkState(!firstDataRecord.getUniqueKeyValue().isEmpty(),
                () -> new IllegalArgumentException(firstDataRecord.getTableName() + " must have primary key or unique key"));
        List<GroupedDataRecord> result = new ArrayList<>(100);
        Map<String, List<DataRecord>> tableGroup = merge(dataRecords).stream().collect(Collectors.groupingBy(DataRecord::getTableName));
        for (Entry<String, List<DataRecord>> entry : tableGroup.entrySet()) {
            Map<PipelineSQLOperationType, List<DataRecord>> typeGroup = entry.getValue().stream().collect(Collectors.groupingBy(DataRecord::getType));
            result.add(new GroupedDataRecord(entry.getKey(), typeGroup.getOrDefault(PipelineSQLOperationType.INSERT, Collections.emptyList()),
                    typeGroup.getOrDefault(PipelineSQLOperationType.UPDATE, Collections.emptyList()), typeGroup.getOrDefault(PipelineSQLOperationType.DELETE, Collections.emptyList())));
        }
        return result;
    }
    
    private void mergeInsert(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = dataRecords.get(dataRecord.getKey());
        ShardingSpherePreconditions.checkState(null == beforeDataRecord || PipelineSQLOperationType.DELETE == beforeDataRecord.getType(),
                () -> new PipelineUnexpectedDataRecordOrderException(beforeDataRecord, dataRecord));
        dataRecords.put(dataRecord.getKey(), dataRecord);
    }
    
    private void mergeUpdate(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = dataRecords.get(dataRecord.getOldKey());
        if (null == beforeDataRecord) {
            dataRecords.put(dataRecord.getKey(), dataRecord);
            return;
        }
        ShardingSpherePreconditions.checkState(PipelineSQLOperationType.DELETE != beforeDataRecord.getType(), () -> new UnsupportedSQLOperationException("Not Delete"));
        if (isUniqueKeyUpdated(dataRecord)) {
            dataRecords.remove(dataRecord.getOldKey());
        }
        if (PipelineSQLOperationType.INSERT == beforeDataRecord.getType()) {
            DataRecord mergedDataRecord = mergeUpdateColumn(PipelineSQLOperationType.INSERT, dataRecord.getTableName(), beforeDataRecord, dataRecord);
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
            return;
        }
        if (PipelineSQLOperationType.UPDATE == beforeDataRecord.getType()) {
            DataRecord mergedDataRecord = mergeUpdateColumn(PipelineSQLOperationType.UPDATE, dataRecord.getTableName(), beforeDataRecord, dataRecord);
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
        }
    }
    
    private void mergeDelete(final DataRecord dataRecord, final Map<DataRecord.Key, DataRecord> dataRecords) {
        DataRecord beforeDataRecord = dataRecords.get(dataRecord.getOldKey());
        ShardingSpherePreconditions.checkState(null == beforeDataRecord || PipelineSQLOperationType.DELETE != beforeDataRecord.getType(),
                () -> new PipelineUnexpectedDataRecordOrderException(beforeDataRecord, dataRecord));
        if (null != beforeDataRecord && PipelineSQLOperationType.UPDATE == beforeDataRecord.getType() && isUniqueKeyUpdated(beforeDataRecord)) {
            DataRecord mergedDataRecord = new DataRecord(PipelineSQLOperationType.DELETE, dataRecord.getTableName(), dataRecord.getPosition(), dataRecord.getColumnCount());
            mergeBaseFields(dataRecord, mergedDataRecord);
            for (int i = 0; i < dataRecord.getColumnCount(); i++) {
                mergedDataRecord.addColumn(new NormalColumn(dataRecord.getColumn(i).getName(),
                        dataRecord.getColumn(i).isUniqueKey() ? beforeDataRecord.getColumn(i).getOldValue() : beforeDataRecord.getColumn(i).getValue(),
                        null, true, dataRecord.getColumn(i).isUniqueKey()));
            }
            dataRecords.remove(beforeDataRecord.getKey());
            dataRecords.put(mergedDataRecord.getKey(), mergedDataRecord);
        } else {
            dataRecords.put(dataRecord.getOldKey(), dataRecord);
        }
    }
    
    private void mergeBaseFields(final DataRecord sourceRecord, final DataRecord targetRecord) {
        targetRecord.setActualTableName(sourceRecord.getActualTableName());
        targetRecord.setCsn(sourceRecord.getCsn());
        targetRecord.setCommitTime(sourceRecord.getCommitTime());
    }
    
    private boolean isUniqueKeyUpdated(final DataRecord dataRecord) {
        // TODO Compatible with multiple unique indexes
        for (Column each : dataRecord.getColumns()) {
            if (each.isUniqueKey() && each.isUpdated()) {
                return true;
            }
        }
        return false;
    }
    
    private DataRecord mergeUpdateColumn(final PipelineSQLOperationType type, final String tableName, final DataRecord preDataRecord, final DataRecord curDataRecord) {
        DataRecord result = new DataRecord(type, tableName, curDataRecord.getPosition(), curDataRecord.getColumnCount());
        mergeBaseFields(curDataRecord, result);
        for (int i = 0; i < curDataRecord.getColumnCount(); i++) {
            result.addColumn(new NormalColumn(
                    curDataRecord.getColumn(i).getName(),
                    preDataRecord.getColumn(i).getOldValue(),
                    curDataRecord.getColumn(i).getValue(),
                    preDataRecord.getColumn(i).isUpdated() || curDataRecord.getColumn(i).isUpdated(),
                    curDataRecord.getColumn(i).isUniqueKey()));
        }
        return result;
    }
}
