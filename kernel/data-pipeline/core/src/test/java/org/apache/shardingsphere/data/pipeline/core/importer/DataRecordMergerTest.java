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
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

class DataRecordMergerTest {
    
    private final DataRecordMerger dataRecordMerger = new DataRecordMerger();
    
    @Test
    void assertDeleteBeforeInsert() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 2, 2);
        DataRecord afterDataRecord = mockInsertDataRecord(1, 1, 1);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertInsertBeforeUpdate() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertInsertBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 1, 2, 2);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertUpdateBeforeUpdate() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertUpdateBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2, 2);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getBatchUpdateDataRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(2, 3, 2, 2);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getBatchUpdateDataRecords().size(), is(2));
        GroupedDataRecord actualGroupedDataRecord = actual.get(0);
        DataRecord actualFirstDataRecord = actualGroupedDataRecord.getBatchUpdateDataRecords().get(0);
        assertThat(actualFirstDataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(actualFirstDataRecord.getTableName(), is("order"));
        assertThat(actualFirstDataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(actualFirstDataRecord.getColumn(0).getValue(), is(2));
        assertThat(actualFirstDataRecord.getColumn(1).getValue(), is(1));
        assertThat(actualFirstDataRecord.getColumn(2).getValue(), is(1));
        DataRecord actualSecondDataRecord = actualGroupedDataRecord.getBatchUpdateDataRecords().get(1);
        assertThat(actualSecondDataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(actualSecondDataRecord.getTableName(), is("order"));
        assertThat(actualSecondDataRecord.getColumn(0).getOldValue(), is(2));
        assertThat(actualSecondDataRecord.getColumn(0).getValue(), is(3));
        assertThat(actualSecondDataRecord.getColumn(1).getValue(), is(2));
        assertThat(actualSecondDataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertInsertBeforeDelete() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertUpdateBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(2, 1, 1);
        List<GroupedDataRecord> actual = dataRecordMerger.group(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertDataRecordsMatched(actual.iterator().next().getNonBatchRecords(), Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    private void assertDataRecordsMatched(final List<DataRecord> actualRecords, final List<DataRecord> expectedRecords) {
        for (int i = 0; i < actualRecords.size(); i++) {
            assertThat(actualRecords.get(0), sameInstance(expectedRecords.get(0)));
        }
    }
    
    @Test
    void assertGroup() {
        List<DataRecord> dataRecords = Arrays.asList(
                mockInsertDataRecord("t1", 1, 1, 1),
                mockUpdateDataRecord("t1", 1, 2, 1),
                mockUpdateDataRecord("t1", 1, 2, 2),
                mockUpdateDataRecord("t1", 2, 1, 1),
                mockUpdateDataRecord("t1", 2, 2, 1),
                mockUpdateDataRecord("t1", 2, 2, 2),
                mockInsertDataRecord("t1", 10, 10, 10),
                mockDeleteDataRecord("t1", 3, 1, 1),
                mockInsertDataRecord("t2", 1, 1, 1));
        List<GroupedDataRecord> groupedDataRecords = dataRecordMerger.group(dataRecords);
        assertThat(groupedDataRecords.size(), is(2));
        assertThat(groupedDataRecords.get(0).getTableName(), is("t1"));
        assertThat(groupedDataRecords.get(0).getBatchInsertDataRecords().size(), is(1));
        assertThat(groupedDataRecords.get(0).getBatchUpdateDataRecords().size(), is(0));
        assertThat(groupedDataRecords.get(0).getBatchDeleteDataRecords().size(), is(1));
        assertThat(groupedDataRecords.get(0).getNonBatchRecords().size(), is(6));
        assertThat(groupedDataRecords.get(1).getTableName(), is("t2"));
        assertThat(groupedDataRecords.get(1).getBatchInsertDataRecords().size(), is(1));
    }
    
    private DataRecord mockInsertDataRecord(final int id, final int userId, final int totalPrice) {
        return mockInsertDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockInsertDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, tableName, new PlaceholderPosition(), 3);
        result.addColumn(new Column("id", id, true, true));
        result.addColumn(new Column("user_id", userId, true, false));
        result.addColumn(new Column("total_price", totalPrice, true, false));
        return result;
    }
    
    private DataRecord mockUpdateDataRecord(final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord("order", null, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final Integer oldId, final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord("order", oldId, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord(tableName, null, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final String tableName, final Integer oldId, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(IngestDataChangeType.UPDATE, tableName, new PlaceholderPosition(), 3);
        result.addColumn(new Column("id", oldId, id, null != oldId, true));
        result.addColumn(new Column("user_id", userId, true, false));
        result.addColumn(new Column("total_price", totalPrice, true, false));
        return result;
    }
    
    private DataRecord mockDeleteDataRecord(final int id, final int userId, final int totalPrice) {
        return mockDeleteDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockDeleteDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(IngestDataChangeType.DELETE, tableName, new PlaceholderPosition(), 3);
        result.addColumn(new Column("id", id, null, true, true));
        result.addColumn(new Column("user_id", userId, null, true, false));
        result.addColumn(new Column("total_price", totalPrice, null, true, false));
        return result;
    }
}
