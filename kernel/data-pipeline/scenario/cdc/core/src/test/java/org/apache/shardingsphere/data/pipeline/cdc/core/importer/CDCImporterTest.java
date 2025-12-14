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

import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CDCImporterTest {
    
    @Test
    void assertDoWithoutSortingCoversEmptyNonDataAndData() {
        List<List<Record>> fetchResults = Arrays.asList(Collections.emptyList(), asList(mock(Record.class)), asList(createDataRecord(1L), createFinishedRecord()));
        PipelineChannel channel = mockNonSortingChannel(fetchResults);
        PipelineJobProgressListener progressListener = mock(PipelineJobProgressListener.class);
        AtomicReference<CDCImporter> importerHolder = new AtomicReference<>();
        PipelineSink sink = mockSinkWithAck(importerHolder, false);
        CDCImporter importer = new CDCImporter(new LinkedList<>(Collections.singletonList(new CDCChannelProgressPair(channel, progressListener))), 10, 1L, sink, false, null);
        importerHolder.set(importer);
        importer.start();
        importer.stop();
        assertThat(captureAckRecords(channel, 2).size(), is(2));
        assertThat(captureProgressCounts(progressListener, 3), contains(0, 0, 1));
        assertNull(CDCImporterManager.getImporter(importer.getImporterId()));
    }
    
    @Test
    void assertDoWithoutSortingNonDataFinishedRemovesChannel() {
        PipelineChannel channel = mockNonSortingChannel(Collections.singletonList(asList(createFinishedRecord())));
        PipelineJobProgressListener progressListener = mock(PipelineJobProgressListener.class);
        PipelineSink sink = mockSimpleSink();
        CDCImporter importer = new CDCImporter(new LinkedList<>(Collections.singletonList(new CDCChannelProgressPair(channel, progressListener))), 1, 1L, sink, false, null);
        importer.start();
        importer.stop();
        assertThat(captureAckRecords(channel, 1).get(0).get(0), instanceOf(FinishedRecord.class));
        assertThat(captureProgressCounts(progressListener, 2), contains(0, 0));
    }
    
    @Test
    void assertDoWithoutSortingWithRateLimitAndAck() {
        PipelineChannel channel = mockNonSortingChannel(Collections.singletonList(asList(createDataRecord(5L))));
        PipelineJobProgressListener progressListener = mock(PipelineJobProgressListener.class);
        JobRateLimitAlgorithm rateLimitAlgorithm = mock(JobRateLimitAlgorithm.class);
        AtomicReference<CDCImporter> importerHolder = new AtomicReference<>();
        PipelineSink sink = mockSinkWithAck(importerHolder, true);
        CDCImporter importer = new CDCImporter(new LinkedList<>(Collections.singletonList(new CDCChannelProgressPair(channel, progressListener))), 1, 1L, sink, false, rateLimitAlgorithm);
        importerHolder.set(importer);
        importer.start();
        verify(rateLimitAlgorithm).intercept(PipelineSQLOperationType.INSERT, 1);
        assertThat(captureProgressCounts(progressListener, 2), contains(0, 1));
        assertThat(captureAckRecords(channel, 1).size(), is(1));
    }
    
    @Test
    void assertDoWithSortingHandlesEmptyAndSingleTransaction() {
        Queue<List<Record>> transactions = new LinkedList<>(Arrays.asList(Collections.emptyList(), asList(createFinishedRecord(), createDataRecord(2L), createFinishedRecord())));
        PipelineChannel channel = mockSortingChannel(transactions);
        PipelineJobProgressListener progressListener = mock(PipelineJobProgressListener.class);
        AtomicReference<CDCImporter> importerHolder = new AtomicReference<>();
        PipelineSink sink = mockSinkWithAck(importerHolder, false);
        CDCImporter importer = new CDCImporter(new LinkedList<>(Collections.singletonList(new CDCChannelProgressPair(channel, progressListener))), 1, 1L, sink, true, null);
        importerHolder.set(importer);
        importer.start();
        importer.stop();
        List<List<Record>> ackedRecords = captureAckRecords(channel, 1);
        assertThat(ackedRecords.size(), is(1));
        assertThat(ackedRecords.get(0).get(0), instanceOf(FinishedRecord.class));
        assertThat(captureProgressCounts(progressListener, 2), contains(0, 1));
    }
    
    @Test
    void assertDoWithSortingProcessesMultipleCsnBatches() {
        Queue<List<Record>> channelOneTransactions = new LinkedList<>(Arrays.asList(asList(createFinishedRecord(), createDataRecord(4L)), asList(createDataRecord(4L)), Collections.emptyList()));
        PipelineChannel channelOne = mockSortingChannel(channelOneTransactions);
        Queue<List<Record>> channelTwoTransactions = new LinkedList<>(Arrays.asList(asList(createDataRecord(4L), createFinishedRecord()), asList(createFinishedRecord()), Collections.emptyList()));
        PipelineChannel channelTwo = mockSortingChannel(channelTwoTransactions);
        Queue<List<Record>> channelThreeTransactions = new LinkedList<>(Arrays.asList(asList(createDataRecord(6L)), asList(createDataRecord(7L))));
        PipelineChannel channelThree = mockSortingChannel(channelThreeTransactions);
        Queue<List<Record>> channelFourTransactions = new LinkedList<>(Arrays.asList(asList(createFinishedRecord()), Collections.emptyList()));
        PipelineChannel channelFour = mockSortingChannel(channelFourTransactions);
        List<CDCChannelProgressPair> pairs = new LinkedList<>();
        pairs.add(new CDCChannelProgressPair(channelOne, mock(PipelineJobProgressListener.class)));
        pairs.add(new CDCChannelProgressPair(channelTwo, mock(PipelineJobProgressListener.class)));
        pairs.add(new CDCChannelProgressPair(channelThree, mock(PipelineJobProgressListener.class)));
        pairs.add(new CDCChannelProgressPair(channelFour, mock(PipelineJobProgressListener.class)));
        JobRateLimitAlgorithm rateLimitAlgorithm = mock(JobRateLimitAlgorithm.class);
        AtomicReference<CDCImporter> importerHolder = new AtomicReference<>();
        PipelineSink sink = mockSinkWithAck(importerHolder, true);
        CDCImporter importer = new CDCImporter(pairs, 2, 1L, sink, true, rateLimitAlgorithm);
        importerHolder.set(importer);
        importer.start();
        verify(rateLimitAlgorithm).intercept(PipelineSQLOperationType.INSERT, 1);
        assertThat(captureAckRecords(channelTwo, 2).size(), is(2));
        assertThat(captureAckRecords(channelFour, 1).size(), is(1));
        verify(channelThree, never()).ack(any());
        importer.ack("missing_ack");
        assertNull(CDCImporterManager.getImporter(importer.getImporterId()));
    }
    
    private DataRecord createDataRecord(final long csn) {
        DataRecord result = new DataRecord(PipelineSQLOperationType.INSERT, "t_order", new IngestPlaceholderPosition(), 1);
        result.setCsn(csn);
        return result;
    }
    
    private FinishedRecord createFinishedRecord() {
        return new FinishedRecord(new IngestPlaceholderPosition());
    }
    
    private List<Record> asList(final Record... records) {
        return new LinkedList<>(Arrays.asList(records));
    }
    
    private PipelineChannel mockNonSortingChannel(final List<List<Record>> fetchResults) {
        PipelineChannel result = mock(PipelineChannel.class);
        Queue<List<Record>> fetchQueue = new LinkedList<>(fetchResults);
        when(result.fetch(anyInt(), anyLong())).thenAnswer(invocation -> fetchQueue.isEmpty() ? Collections.emptyList() : fetchQueue.poll());
        when(result.peek()).thenReturn(Collections.emptyList());
        when(result.poll()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private PipelineChannel mockSortingChannel(final Queue<List<Record>> transactions) {
        PipelineChannel result = mock(PipelineChannel.class);
        when(result.fetch(anyInt(), anyLong())).thenReturn(Collections.emptyList());
        when(result.peek()).thenAnswer(invocation -> {
            List<Record> records = transactions.peek();
            return null == records ? Collections.emptyList() : records;
        });
        when(result.poll()).thenAnswer(invocation -> {
            List<Record> records = transactions.poll();
            return null == records ? Collections.emptyList() : records;
        });
        return result;
    }
    
    private PipelineSink mockSimpleSink() {
        PipelineSink result = mock(PipelineSink.class);
        when(result.write(anyString(), anyCollection())).thenAnswer(invocation -> {
            Collection<Record> records = invocation.getArgument(1);
            return new PipelineJobUpdateProgress(records.size());
        });
        return result;
    }
    
    private PipelineSink mockSinkWithAck(final AtomicReference<CDCImporter> importerHolder, final boolean stopAfterWrite) {
        PipelineSink result = mock(PipelineSink.class);
        when(result.write(anyString(), anyCollection())).thenAnswer(invocation -> {
            String ackId = invocation.getArgument(0);
            Collection<Record> records = invocation.getArgument(1);
            importerHolder.get().ack(ackId);
            if (stopAfterWrite) {
                importerHolder.get().stop();
            }
            return new PipelineJobUpdateProgress(records.size());
        });
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<List<Record>> captureAckRecords(final PipelineChannel channel, final int expectedTimes) {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(channel, times(expectedTimes)).ack(captor.capture());
        return captor.getAllValues().stream().map(each -> {
            @SuppressWarnings("unchecked")
            List<Record> records = (List<Record>) each;
            return records;
        }).collect(Collectors.toList());
    }
    
    private List<Integer> captureProgressCounts(final PipelineJobProgressListener listener, final int expectedTimes) {
        ArgumentCaptor<PipelineJobUpdateProgress> captor = ArgumentCaptor.forClass(PipelineJobUpdateProgress.class);
        verify(listener, times(expectedTimes)).onProgressUpdated(captor.capture());
        return captor.getAllValues().stream().map(PipelineJobUpdateProgress::getProcessedRecordsCount).collect(Collectors.toList());
    }
}
