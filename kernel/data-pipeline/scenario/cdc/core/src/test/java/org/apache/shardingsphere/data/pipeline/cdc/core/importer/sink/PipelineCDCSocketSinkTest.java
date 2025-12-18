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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink;

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
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void assertClose() {
        Channel channel = mock(Channel.class);
        new PipelineCDCSocketSink(channel, mock(), Collections.singletonList("logic_schema.t_order")).close();
        ArgumentCaptor<CDCResponse> responseCaptor = ArgumentCaptor.forClass(CDCResponse.class);
        verify(channel).writeAndFlush(responseCaptor.capture());
        CDCResponse actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getStatus(), is(CDCResponse.Status.FAILED));
        assertThat(actualResponse.getErrorCode(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actualResponse.getErrorMessage(), is("The socket channel is closed."));
    }
}
