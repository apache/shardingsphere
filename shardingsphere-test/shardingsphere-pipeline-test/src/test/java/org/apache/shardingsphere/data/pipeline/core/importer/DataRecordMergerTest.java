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

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.GroupedDataRecord;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineUnexpectedDataRecordOrderException;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DataRecordMergerTest {
    
    private final DataRecordMerger dataRecordMerger = new DataRecordMerger();
    
    private DataRecord beforeDataRecord;
    
    private DataRecord afterDataRecord;
    
    private Collection<DataRecord> actual;
    
    @Test(expected = PipelineUnexpectedDataRecordOrderException.class)
    public void assertInsertBeforeInsert() {
        beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        afterDataRecord = mockInsertDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test(expected = PipelineUnexpectedDataRecordOrderException.class)
    public void assertUpdateBeforeInsert() {
        beforeDataRecord = mockUpdateDataRecord(1, 2, 2);
        afterDataRecord = mockInsertDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    public void assertDeleteBeforeInsert() {
        beforeDataRecord = mockDeleteDataRecord(1, 2, 2);
        afterDataRecord = mockInsertDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    public void assertInsertBeforeUpdate() {
        beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    public void assertInsertBeforeUpdatePrimaryKey() {
        beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        afterDataRecord = mockUpdateDataRecord(1, 2, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    public void assertUpdateBeforeUpdate() {
        beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    public void assertUpdateBeforeUpdatePrimaryKey() {
        beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        afterDataRecord = mockUpdateDataRecord(1, 2, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    public void assertUpdatePrimaryKeyBeforeUpdate() {
        beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        afterDataRecord = mockUpdateDataRecord(2, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    public void assertUpdatePrimaryKeyBeforeUpdatePrimaryKey() {
        beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        afterDataRecord = mockUpdateDataRecord(2, 3, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(3));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertDeleteBeforeUpdate() {
        beforeDataRecord = mockDeleteDataRecord(1, 1, 1);
        afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    public void assertInsertBeforeDelete() {
        beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    public void assertUpdateBeforeDelete() {
        beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    public void assertUpdatePrimaryKeyBeforeDelete() {
        beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        afterDataRecord = mockDeleteDataRecord(2, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(IngestDataChangeType.DELETE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(1));
        assertThat(dataRecord.getColumn(2).getValue(), is(1));
    }
    
    @Test(expected = PipelineUnexpectedDataRecordOrderException.class)
    public void assertDeleteBeforeDelete() {
        beforeDataRecord = mockDeleteDataRecord(1, 1, 1);
        afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        actual = dataRecordMerger.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
    }
    
    @Test
    public void assertGroup() {
        List<DataRecord> dataRecords = mockDataRecords();
        List<GroupedDataRecord> groupedDataRecords = dataRecordMerger.group(dataRecords);
        assertThat(groupedDataRecords.size(), is(2));
        assertThat(groupedDataRecords.get(0).getTableName(), is("t1"));
        assertThat(groupedDataRecords.get(1).getTableName(), is("t2"));
        assertThat(groupedDataRecords.get(0).getInsertDataRecords().size(), is(1));
        assertThat(groupedDataRecords.get(0).getUpdateDataRecords().size(), is(1));
        assertThat(groupedDataRecords.get(0).getDeleteDataRecords().size(), is(1));
    }
    
    private List<DataRecord> mockDataRecords() {
        return Arrays.asList(
                mockInsertDataRecord("t1", 1, 1, 1),
                mockUpdateDataRecord("t1", 1, 2, 1),
                mockUpdateDataRecord("t1", 1, 2, 2),
                mockUpdateDataRecord("t1", 2, 1, 1),
                mockUpdateDataRecord("t1", 2, 2, 1),
                mockUpdateDataRecord("t1", 2, 2, 2),
                mockDeleteDataRecord("t1", 3, 1, 1),
                mockInsertDataRecord("t2", 1, 1, 1));
    }
    
    private DataRecord mockInsertDataRecord(final int id, final int userId, final int totalPrice) {
        return mockInsertDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockInsertDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 3);
        result.setType(IngestDataChangeType.INSERT);
        result.setTableName(tableName);
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
        DataRecord result = new DataRecord(new PlaceholderPosition(), 3);
        result.setType(IngestDataChangeType.UPDATE);
        result.setTableName(tableName);
        result.addColumn(new Column("id", oldId, id, null != oldId, true));
        result.addColumn(new Column("user_id", userId, true, false));
        result.addColumn(new Column("total_price", totalPrice, true, false));
        return result;
    }
    
    private DataRecord mockDeleteDataRecord(final int id, final int userId, final int totalPrice) {
        return mockDeleteDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockDeleteDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 3);
        result.setType(IngestDataChangeType.DELETE);
        result.setTableName(tableName);
        result.addColumn(new Column("id", id, true, true));
        result.addColumn(new Column("user_id", userId, true, false));
        result.addColumn(new Column("total_price", totalPrice, true, false));
        return result;
    }
}
