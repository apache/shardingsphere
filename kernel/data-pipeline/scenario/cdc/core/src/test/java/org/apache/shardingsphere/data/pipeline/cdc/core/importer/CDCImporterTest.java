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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import org.apache.shardingsphere.data.pipeline.core.channel.memory.MemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.type.QPSJobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTaskAckCallback;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class CDCImporterTest {
    
    @Test
    void assertWithoutSortStart() throws ExecutionException, InterruptedException {
        AtomicReference<IngestPosition> position = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        MemoryPipelineChannel channel = new MemoryPipelineChannel(1, new InventoryTaskAckCallback(position));
        CDCChannelProgressPair mockPair = new CDCChannelProgressPair(channel, param -> count.addAndGet(param.getProcessedRecordsCount()));
        AtomicReference<String> ackPosition = new AtomicReference<>();
        PipelineSink mockSink = buildSink(ackPosition);
        List<CDCChannelProgressPair> originalChannelProgressPairs = new LinkedList<>();
        originalChannelProgressPairs.add(mockPair);
        CDCImporter importer = new CDCImporter(originalChannelProgressPairs, 1, 300L, mockSink, false, null);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<?> future = executorService.submit(importer);
        channel.push(Arrays.asList(new DataRecord(PipelineSQLOperationType.DELETE, "foo_tbl", new IngestPlaceholderPosition(), 1), new FinishedRecord(new IngestPlaceholderPosition())));
        Awaitility.await().pollInterval(10L, TimeUnit.MILLISECONDS).atMost(1L, TimeUnit.SECONDS).until(() -> null != ackPosition.get());
        importer.ack(ackPosition.get());
        future.get();
        assertThat(count.get(), is(1));
    }
    
    @Test
    void assertWithSortStart() throws ExecutionException, InterruptedException {
        AtomicReference<IngestPosition> position = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        MemoryPipelineChannel firstChannel = new MemoryPipelineChannel(10, new InventoryTaskAckCallback(position));
        MemoryPipelineChannel secondChannel = new MemoryPipelineChannel(10, new InventoryTaskAckCallback(position));
        CDCChannelProgressPair firstPair = new CDCChannelProgressPair(firstChannel, param -> count.addAndGet(param.getProcessedRecordsCount()));
        CDCChannelProgressPair secondPair = new CDCChannelProgressPair(secondChannel, param -> count.addAndGet(param.getProcessedRecordsCount()));
        AtomicReference<String> ackPosition = new AtomicReference<>();
        PipelineSink mockSink = buildSink(ackPosition);
        List<CDCChannelProgressPair> originalChannelProgressPairs = new LinkedList<>(Arrays.asList(firstPair, secondPair));
        CDCImporter importer = new CDCImporter(originalChannelProgressPairs, 1, 300L, mockSink, true, new QPSJobRateLimitAlgorithm());
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        final Future<?> future = executorService.submit(importer);
        DataRecord dataRecord = new DataRecord(PipelineSQLOperationType.DELETE, "foo_tbl", new IngestPlaceholderPosition(), 1);
        dataRecord.setCsn(1L);
        firstChannel.push(Collections.singletonList(dataRecord));
        firstChannel.push(Collections.singletonList(new PlaceholderRecord(new IngestPlaceholderPosition())));
        secondChannel.push(Collections.singletonList(dataRecord));
        Awaitility.await().pollInterval(10L, TimeUnit.MILLISECONDS).atMost(1L, TimeUnit.SECONDS).until(() -> null != ackPosition.get());
        importer.ack(ackPosition.get());
        importer.stop();
        future.get();
        assertThat(count.get(), is(2));
    }
    
    private PipelineSink buildSink(final AtomicReference<String> ackPosition) {
        return new PipelineSink() {
            
            @Override
            public void close() {
            }
            
            @Override
            public PipelineJobProgressUpdatedParameter write(final String ackId, final Collection<Record> records) {
                ackPosition.set(ackId);
                return new PipelineJobProgressUpdatedParameter(records.size());
            }
        };
    }
}
