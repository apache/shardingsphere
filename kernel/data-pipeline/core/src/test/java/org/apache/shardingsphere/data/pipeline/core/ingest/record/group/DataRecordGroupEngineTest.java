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
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataRecordGroupEngineTest {
    
    private final DataRecordGroupEngine groupEngine = new DataRecordGroupEngine();
    
    @Test
    void assertInsertBeforeInsert() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockInsertDataRecord(1, 1, 1);
        assertThrows(PipelineUnexpectedDataRecordOrderException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertUpdateBeforeInsert() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 2);
        DataRecord afterDataRecord = mockInsertDataRecord(1, 1, 1);
        assertThrows(PipelineUnexpectedDataRecordOrderException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertDeleteBeforeInsert() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 2, 2);
        DataRecord afterDataRecord = mockInsertDataRecord(1, 1, 1);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertInsertBeforeUpdate() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertInsertBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertUpdateBeforeUpdate() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertUpdateBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeUpdate() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(2, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(2));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(2, 3, 2, 2);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getColumn(0).getOldValue(), is(1));
        assertThat(dataRecord.getColumn(0).getValue(), is(3));
        assertThat(dataRecord.getColumn(1).getValue(), is(2));
        assertThat(dataRecord.getColumn(2).getValue(), is(2));
    }
    
    @Test
    void assertDeleteBeforeUpdate() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 2);
        assertThrows(UnsupportedSQLOperationException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertInsertBeforeDelete() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertUpdateBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(2, 1, 1);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.DELETE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertNull(dataRecord.getColumn(0).getOldValue());
        assertThat(dataRecord.getColumn(0).getValue(), is(1));
        assertThat(dataRecord.getColumn(1).getValue(), is(1));
        assertThat(dataRecord.getColumn(2).getValue(), is(1));
    }
    
    @Test
    void assertDeleteBeforeDelete() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        assertThrows(PipelineUnexpectedDataRecordOrderException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertGroup() {
        List<DataRecord> dataRecords = mockDataRecords();
        List<GroupedDataRecord> groupedDataRecords = groupEngine.group(dataRecords);
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
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, tableName, new IngestPlaceholderPosition(), 3);
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
        DataRecord result = new DataRecord(PipelineSQLOperationType.UPDATE, tableName, new IngestPlaceholderPosition(), 3);
        result.addColumn(new Column("id", oldId, id, null != oldId, true));
        result.addColumn(new Column("user_id", userId, true, false));
        result.addColumn(new Column("total_price", totalPrice, true, false));
        return result;
    }
    
    private DataRecord mockDeleteDataRecord(final int id, final int userId, final int totalPrice) {
        return mockDeleteDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockDeleteDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(PipelineSQLOperationType.DELETE, tableName, new IngestPlaceholderPosition(), 3);
        result.addColumn(new Column("id", id, null, true, true));
        result.addColumn(new Column("user_id", userId, null, true, false));
        result.addColumn(new Column("total_price", totalPrice, null, true, false));
        return result;
    }
}
