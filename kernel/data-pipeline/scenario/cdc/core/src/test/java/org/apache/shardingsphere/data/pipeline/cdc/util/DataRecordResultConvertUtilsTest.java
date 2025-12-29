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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataRecordResultConvertUtilsTest {
    
    @ParameterizedTest
    @MethodSource("dataChangeTypeTestCases")
    void assertConvertDataRecordToRecordWithNonInsertTypes(final PipelineSQLOperationType operationType, final Record.DataChangeType expectedDataChangeType) throws InvalidProtocolBufferException {
        DataRecord dataRecord = new DataRecord(operationType, "test_schema", "t_user", new IntegerPrimaryKeyIngestPosition(BigInteger.valueOf(5L), BigInteger.valueOf(10L)), 1);
        dataRecord.addColumn(new NormalColumn("id", 1L, 2L, true, true));
        dataRecord.setCommitTime(123L);
        Record actualRecord = DataRecordResultConvertUtils.convertDataRecordToRecord("logic_db", "test_schema", dataRecord);
        assertThat(actualRecord.getMetaData().getDatabase(), is("logic_db"));
        assertThat(actualRecord.getMetaData().getSchema(), is("test_schema"));
        assertThat(actualRecord.getMetaData().getTable(), is("t_user"));
        assertThat(actualRecord.getTransactionCommitMillis(), is(123L));
        assertThat(actualRecord.getDataChangeType(), is(expectedDataChangeType));
        assertThat(actualRecord.getBefore(0).getValue().unpack(Int64Value.class).getValue(), is(1L));
        assertThat(actualRecord.getAfter(0).getValue().unpack(Int64Value.class).getValue(), is(2L));
    }
    
    private static Stream<Arguments> dataChangeTypeTestCases() {
        return Stream.of(
                Arguments.of(PipelineSQLOperationType.INSERT, Record.DataChangeType.INSERT),
                Arguments.of(PipelineSQLOperationType.UPDATE, Record.DataChangeType.UPDATE),
                Arguments.of(PipelineSQLOperationType.DELETE, Record.DataChangeType.DELETE),
                Arguments.of(PipelineSQLOperationType.SELECT, Record.DataChangeType.UNKNOWN));
    }
}
