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
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
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
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockInsertDataRecord(1, 10, 100);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertInsertBeforeUpdate() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 10, 200);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", null, 1, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", null, 10, true, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", null, 200, true, false));
    }
    
    private void assertColumnsMatched(final Column actual, final Column expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getOldValue(), is(expected.getOldValue()));
        assertThat(actual.getValue(), is(expected.getValue()));
        assertThat(actual.isUpdated(), is(expected.isUpdated()));
        assertThat(actual.isUniqueKey(), is(expected.isUniqueKey()));
    }
    
    @Test
    void assertInsertBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 10, 50);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.INSERT));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", null, 2, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", null, 10, true, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", null, 50, true, false));
    }
    
    @Test
    void assertUpdateBeforeUpdate() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 1, 10, 100);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 1, 10, 200);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", 1, 1, false, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", 10, 10, false, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", 50, 200, true, false));
    }
    
    @Test
    void assertUpdateBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 2, 10, 200);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", 1, 2, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", 10, 10, false, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", 50, 200, true, false));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeUpdate() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(2, 10, 200);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", 1, 2, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", 10, 10, false, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", 50, 200, true, false));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeUpdatePrimaryKey() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(2, 3, 10, 50);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.UPDATE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(456L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", 1, 3, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", 10, 10, false, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", 50, 50, false, false));
    }
    
    @Test
    void assertDeleteBeforeUpdate() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockUpdateDataRecord(1, 20, 200);
        assertThrows(UnsupportedSQLOperationException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertInsertBeforeDelete() {
        DataRecord beforeDataRecord = mockInsertDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 10, 50);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertUpdateBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 10, 50);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 10, 50);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(afterDataRecord));
    }
    
    @Test
    void assertUpdatePrimaryKeyBeforeDelete() {
        DataRecord beforeDataRecord = mockUpdateDataRecord(1, 2, 10, 50);
        DataRecord afterDataRecord = mockDeleteDataRecord(2, 10, 50);
        Collection<DataRecord> actual = groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord));
        assertThat(actual.size(), is(1));
        DataRecord dataRecord = actual.iterator().next();
        assertThat(dataRecord.getType(), is(PipelineSQLOperationType.DELETE));
        assertThat(dataRecord.getTableName(), is("order"));
        assertThat(dataRecord.getActualTableName(), is("order_0"));
        assertThat(dataRecord.getCommitTime(), is(789L));
        assertColumnsMatched(dataRecord.getColumn(0), new NormalColumn("id", 1, null, true, true));
        assertColumnsMatched(dataRecord.getColumn(1), new NormalColumn("user_id", 10, null, true, false));
        assertColumnsMatched(dataRecord.getColumn(2), new NormalColumn("total_price", 50, null, true, false));
    }
    
    @Test
    void assertDeleteBeforeDelete() {
        DataRecord beforeDataRecord = mockDeleteDataRecord(1, 1, 1);
        DataRecord afterDataRecord = mockDeleteDataRecord(1, 1, 1);
        assertThrows(PipelineUnexpectedDataRecordOrderException.class, () -> groupEngine.merge(Arrays.asList(beforeDataRecord, afterDataRecord)));
    }
    
    @Test
    void assertGroupOnTableHasNoUniqueKey() {
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.INSERT, "order", new IngestPlaceholderPosition(), 3);
        dataRecord.setActualTableName("order_0");
        List<DataRecord> dataRecords = Collections.singletonList(dataRecord);
        assertThrows(IllegalArgumentException.class, () -> groupEngine.group(dataRecords));
    }
    
    @Test
    void assertGroupOnTableHasUniqueKey() {
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
        result.setActualTableName("order_0");
        result.setCommitTime(123L);
        result.addColumn(new NormalColumn("id", id, true, true));
        result.addColumn(new NormalColumn("user_id", userId, true, false));
        result.addColumn(new NormalColumn("total_price", totalPrice, true, false));
        return result;
    }
    
    private DataRecord mockUpdateDataRecord(final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord("order", id, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final Integer oldId, final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord("order", oldId, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        return mockUpdateDataRecord(tableName, id, id, userId, totalPrice);
    }
    
    private DataRecord mockUpdateDataRecord(final String tableName, final Integer oldId, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(PipelineSQLOperationType.UPDATE, tableName, new IngestPlaceholderPosition(), 3);
        result.setActualTableName("order_0");
        result.setCommitTime(456L);
        result.addColumn(new NormalColumn("id", oldId, id, !Objects.deepEquals(oldId, id), true));
        result.addColumn(new NormalColumn("user_id", userId, userId, false, false));
        result.addColumn(new NormalColumn("total_price", 50, totalPrice, 50 != totalPrice, false));
        return result;
    }
    
    private DataRecord mockDeleteDataRecord(final int id, final int userId, final int totalPrice) {
        return mockDeleteDataRecord("order", id, userId, totalPrice);
    }
    
    private DataRecord mockDeleteDataRecord(final String tableName, final int id, final int userId, final int totalPrice) {
        DataRecord result = new DataRecord(PipelineSQLOperationType.DELETE, tableName, new IngestPlaceholderPosition(), 3);
        result.setActualTableName("order_0");
        result.setCommitTime(789L);
        result.addColumn(new NormalColumn("id", id, null, true, true));
        result.addColumn(new NormalColumn("user_id", userId, null, true, false));
        result.addColumn(new NormalColumn("total_price", totalPrice, null, true, false));
        return result;
    }
}
