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

package org.apache.shardingsphere.data.pipeline.cdc.sink.socket;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PipelineCDCSocketSinkTest {
    
    @Test
    void assertWriteWhenRecordsEmpty() {
        try (PipelineCDCSocketSink sink = new PipelineCDCSocketSink(mock(), mock(), Arrays.asList("logic.t_order", "t_without_schema"))) {
            assertThat(sink.write("ack", Collections.emptyList()).getProcessedRecordsCount(), is(0));
        }
    }
    
    @Test
    void assertWriteWhenChannelWritable() {
        Channel channel = mock(Channel.class);
        when(channel.isWritable()).thenReturn(false, true);
        when(channel.isActive()).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("logic_db");
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.UPDATE, "t_order", new IngestPlaceholderPosition(), 1);
        dataRecord.addColumn(new NormalColumn("order_id", 1, 2, true, true));
        FinishedRecord finishedRecord = new FinishedRecord(new IngestPlaceholderPosition());
        try (PipelineCDCSocketSink sink = new PipelineCDCSocketSink(channel, database, Collections.singletonList("logic_schema.t_order"))) {
            PipelineJobUpdateProgress actual = sink.write("ack", Arrays.asList(finishedRecord, dataRecord));
            assertThat(actual.getProcessedRecordsCount(), is(1));
            ArgumentCaptor<CDCResponse> responseCaptor = ArgumentCaptor.forClass(CDCResponse.class);
            verify(channel).writeAndFlush(responseCaptor.capture());
            CDCResponse actualResponse = responseCaptor.getValue();
            DataRecordResult recordResult = actualResponse.getDataRecordResult();
            assertThat(recordResult.getAckId(), is("ack"));
            assertThat(recordResult.getRecordList().size(), is(1));
            DataRecordResult.Record actualRecord = recordResult.getRecord(0);
            assertThat(actualRecord.getMetaData().getDatabase(), is("logic_db"));
            assertThat(actualRecord.getMetaData().getSchema(), is("logic_schema"));
            assertThat(actualRecord.getMetaData().getTable(), is("t_order"));
        }
    }
    
    @Test
    void assertWriteWhenChannelInactive() {
        Channel channel = mock(Channel.class);
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.INSERT, "t_order", new IngestPlaceholderPosition(), 0);
        try (PipelineCDCSocketSink sink = new PipelineCDCSocketSink(channel, mock(), Collections.singletonList("logic_schema.t_order"))) {
            PipelineJobUpdateProgress actual = sink.write("ack", Collections.singletonList(dataRecord));
            assertThat(actual.getProcessedRecordsCount(), is(0));
            verify(channel, never()).writeAndFlush(any());
        }
    }
    
    @Test
    void assertWriteWhenOnlyFinishedRecords() {
        Channel channel = mock(Channel.class);
        when(channel.isWritable()).thenReturn(true);
        when(channel.isActive()).thenReturn(true);
        PipelineCDCSocketSink sink = new PipelineCDCSocketSink(channel, mock(), Collections.emptyList());
        PipelineJobUpdateProgress actual = sink.write("foo_ack", Collections.singletonList(new FinishedRecord(new IngestPlaceholderPosition())));
        assertThat(actual.getProcessedRecordsCount(), is(0));
        ArgumentCaptor<CDCResponse> responseCaptor = ArgumentCaptor.forClass(CDCResponse.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        CDCResponse actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.SUCCEED));
        assertThat(actualResponse.getResponseCase(), is(CDCResponse.ResponseCase.DATA_RECORD_RESULT));
        assertThat(actualResponse.getDataRecordResult().getAckId(), is("foo_ack"));
        assertTrue(actualResponse.getDataRecordResult().getRecordList().isEmpty());
    }
    
    @Test
    void assertWriteWhenChannelBecomesInactive() {
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true, false);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.INSERT, "foo_tbl", new IngestPlaceholderPosition(), 0);
        PipelineCDCSocketSink sink = new PipelineCDCSocketSink(channel, database, Collections.singletonList("foo_schema.foo_tbl"));
        assertThat(sink.write("foo_ack", Collections.singletonList(dataRecord)).getProcessedRecordsCount(), is(0));
        verify(channel, never()).writeAndFlush(any());
        verifyNoInteractions(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("failureMessageCases")
    void assertOnFailure(final String name, final String message, final String expectedMessage) {
        Channel channel = mock(Channel.class);
        PipelineCDCSocketSink sink = new PipelineCDCSocketSink(channel, mock(), Collections.emptyList());
        sink.onFailure(new IllegalStateException(message));
        ArgumentCaptor<CDCResponse> responseCaptor = ArgumentCaptor.forClass(CDCResponse.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        CDCResponse expectedResponse = CDCResponse.newBuilder().setStatus(CDCResponse.Status.FAILED).setRequestId("").setErrorCode("").setErrorMessage(expectedMessage).build();
        assertThat(responseCaptor.getValue(), is(expectedResponse));
        verify(channel, never()).close();
    }
    
    private static Stream<Arguments> failureMessageCases() {
        return Stream.of(
                Arguments.of("failure with message", "foo_failure", "foo_failure"),
                Arguments.of("failure without message", null, ""),
                Arguments.of("failure with empty message", "", ""));
    }
    
    @Test
    void assertClose() {
        Channel channel = mock(Channel.class);
        new PipelineCDCSocketSink(channel, mock(), Collections.singletonList("logic_schema.t_order")).close();
        ArgumentCaptor<CDCResponse> responseCaptor = ArgumentCaptor.forClass(CDCResponse.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        CDCResponse actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.FAILED));
        assertThat(actualResponse.getErrorCode(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actualResponse.getErrorMessage(), is("The socket channel is closed."));
        verify(channel, never()).close();
    }
}
