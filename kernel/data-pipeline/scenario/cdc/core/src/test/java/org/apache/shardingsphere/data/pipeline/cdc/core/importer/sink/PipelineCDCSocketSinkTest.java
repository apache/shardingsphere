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
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineCDCSocketSinkTest {
    
    @Test
    void assertWrite() {
        Channel mockChannel = mock(Channel.class);
        when(mockChannel.isWritable()).thenReturn(false, true);
        when(mockChannel.isActive()).thenReturn(true);
        ShardingSphereDatabase mockDatabase = mock(ShardingSphereDatabase.class);
        when(mockDatabase.getName()).thenReturn("test");
        try (PipelineCDCSocketSink sink = new PipelineCDCSocketSink(mockChannel, mockDatabase, Collections.singletonList("test.t_order"))) {
            PipelineJobUpdateProgress actual = sink.write("ack", Collections.singletonList(new FinishedRecord(new IngestPlaceholderPosition())));
            assertThat(actual.getProcessedRecordsCount(), is(0));
            actual = sink.write("ack", Collections.singletonList(new DataRecord(PipelineSQLOperationType.DELETE, "t_order", new IngestPlaceholderPosition(), 1)));
            assertThat(actual.getProcessedRecordsCount(), is(1));
        }
    }
}
