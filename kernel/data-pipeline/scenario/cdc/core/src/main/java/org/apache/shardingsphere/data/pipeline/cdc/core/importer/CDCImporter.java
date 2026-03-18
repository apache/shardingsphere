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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckId;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.cdc.util.RandomStrings;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * CDC importer.
 */
@RequiredArgsConstructor
@Slf4j
public final class CDCImporter extends AbstractPipelineLifecycleRunnable implements Importer {
    
    @Getter
    private final String importerId = RandomStrings.randomAlphanumeric(8);
    
    private final List<CDCChannelProgressPair> channelProgressPairs;
    
    private final int batchSize;
    
    private final long timeoutMillis;
    
    private final PipelineSink sink;
    
    private final boolean needSorting;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final PriorityQueue<CSNRecords> csnRecordsQueue = new PriorityQueue<>(new CSNRecordsComparator());
    
    private final Cache<String, List<Pair<CDCChannelProgressPair, CDCAckPosition>>> ackCache = Caffeine.newBuilder().maximumSize(10000L).expireAfterAccess(5L, TimeUnit.MINUTES).build();
    
    @Override
    protected void runBlocking() {
        CDCImporterManager.putImporter(this);
        for (CDCChannelProgressPair each : channelProgressPairs) {
            each.getJobProgressListener().onProgressUpdated(new PipelineJobUpdateProgress(0));
        }
        while (isRunning()) {
            if (needSorting) {
                doWithSorting();
            } else {
                doWithoutSorting();
            }
            if (channelProgressPairs.isEmpty()) {
                break;
            }
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void doWithSorting() {
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(PipelineSQLOperationType.INSERT, 1);
        }
        List<CSNRecords> csnRecordsList = getCsnRecordsList();
        if (csnRecordsList.isEmpty()) {
            TimeUnit.MILLISECONDS.sleep(timeoutMillis);
            return;
        }
        // TODO Combine small transactions into a large transaction, to improve transformation performance.
        String ackId = CDCAckId.build(importerId).marshal();
        if (1 == csnRecordsList.size()) {
            processCSNRecords(csnRecordsList.get(0), ackId);
        } else {
            processCSNRecordsList(csnRecordsList, ackId);
        }
    }
    
    private List<CSNRecords> getCsnRecordsList() {
        List<CSNRecords> result = new LinkedList<>();
        CSNRecords firstRecords = null;
        for (int i = 0, count = channelProgressPairs.size(); i < count; i++) {
            prepareTransactionRecords();
            CSNRecords csnRecords = csnRecordsQueue.peek();
            if (null == csnRecords) {
                continue;
            }
            if (null == firstRecords) {
                csnRecords = csnRecordsQueue.poll();
                firstRecords = csnRecords;
                result.add(csnRecords);
            } else if (csnRecords.getCsn() == firstRecords.getCsn()) {
                csnRecords = csnRecordsQueue.poll();
                result.add(csnRecords);
            }
        }
        return result;
    }
    
    // TODO openGauss CSN should be incremented for every transaction. Currently, CSN might be duplicated in transactions.
    // TODO Use channels watermark depth to improve performance.
    private void prepareTransactionRecords() {
        if (csnRecordsQueue.isEmpty()) {
            prepareWhenQueueIsEmpty();
        } else {
            prepareWhenQueueIsNotEmpty(csnRecordsQueue.peek().getCsn());
        }
    }
    
    private void prepareWhenQueueIsEmpty() {
        for (CDCChannelProgressPair each : channelProgressPairs) {
            PipelineChannel channel = each.getChannel();
            List<Record> records = channel.poll();
            if (records.isEmpty()) {
                continue;
            }
            if (0 == getDataRecordsCount(records)) {
                channel.ack(records);
                continue;
            }
            csnRecordsQueue.add(new CSNRecords(findFirstDataRecord(records).getCsn(), each, records));
        }
    }
    
    private void prepareWhenQueueIsNotEmpty(final long oldestCSN) {
        for (CDCChannelProgressPair each : channelProgressPairs) {
            PipelineChannel channel = each.getChannel();
            List<Record> records = channel.peek();
            if (records.isEmpty()) {
                continue;
            }
            if (0 == getDataRecordsCount(records)) {
                records = channel.poll();
                channel.ack(records);
                continue;
            }
            long csn = findFirstDataRecord(records).getCsn();
            if (csn <= oldestCSN) {
                records = channel.poll();
                csnRecordsQueue.add(new CSNRecords(csn, each, records));
            }
        }
    }
    
    private int getDataRecordsCount(final List<Record> records) {
        return (int) records.stream().filter(DataRecord.class::isInstance).count();
    }
    
    private DataRecord findFirstDataRecord(final List<Record> records) {
        for (Record each : records) {
            if (each instanceof DataRecord) {
                return (DataRecord) each;
            }
        }
        throw new IllegalStateException("No data record found");
    }
    
    private void processCSNRecords(final CSNRecords csnRecords, final String ackId) {
        List<Record> records = csnRecords.getRecords();
        ackCache.put(ackId, Collections.singletonList(Pair.of(csnRecords.getChannelProgressPair(), new CDCAckPosition(records.get(records.size() - 1), getDataRecordsCount(records)))));
        sink.write(ackId, filterDataRecords(records));
    }
    
    private void processCSNRecordsList(final List<CSNRecords> csnRecordsList, final String ackId) {
        List<Pair<CDCChannelProgressPair, CDCAckPosition>> ackValue = csnRecordsList.stream().map(each -> Pair.of(each.getChannelProgressPair(),
                new CDCAckPosition(each.getRecords().get(each.getRecords().size() - 1), getDataRecordsCount(each.getRecords())))).collect(Collectors.toList());
        ackCache.put(ackId, ackValue);
        Collection<Record> records = new ArrayList<>(ackValue.stream().mapToInt(each -> each.getRight().getDataRecordCount()).sum());
        csnRecordsList.forEach(each -> records.addAll(filterDataRecords(each.getRecords())));
        sink.write(ackId, filterDataRecords(records));
    }
    
    private List<Record> filterDataRecords(final Collection<Record> records) {
        return records.stream().filter(DataRecord.class::isInstance).map(DataRecord.class::cast).collect(Collectors.toList());
    }
    
    private void doWithoutSorting() {
        for (CDCChannelProgressPair each : channelProgressPairs) {
            doWithoutSorting(each);
        }
    }
    
    private void doWithoutSorting(final CDCChannelProgressPair channelProgressPair) {
        PipelineChannel channel = channelProgressPair.getChannel();
        List<Record> records = channel.fetch(batchSize, timeoutMillis);
        if (records.isEmpty()) {
            return;
        }
        Record lastRecord = records.get(records.size() - 1);
        if (records.stream().noneMatch(DataRecord.class::isInstance)) {
            channel.ack(records);
            channelProgressPair.getJobProgressListener().onProgressUpdated(new PipelineJobUpdateProgress(0));
            if (lastRecord instanceof FinishedRecord) {
                channelProgressPairs.remove(channelProgressPair);
            }
            return;
        }
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(PipelineSQLOperationType.INSERT, 1);
        }
        String ackId = CDCAckId.build(importerId).marshal();
        ackCache.put(ackId, Collections.singletonList(Pair.of(channelProgressPair, new CDCAckPosition(records.get(records.size() - 1), getDataRecordsCount(records)))));
        sink.write(ackId, records);
    }
    
    /**
     * Ack.
     *
     * @param ackId ack id
     */
    public void ack(final String ackId) {
        List<Pair<CDCChannelProgressPair, CDCAckPosition>> channelPositionPairList = ackCache.getIfPresent(ackId);
        if (null == channelPositionPairList) {
            log.warn("Could not find cached ack info, ack id: {}", ackId);
            return;
        }
        for (Pair<CDCChannelProgressPair, CDCAckPosition> each : channelPositionPairList) {
            CDCAckPosition ackPosition = each.getRight();
            Record lastRecord = ackPosition.getLastRecord();
            each.getLeft().getChannel().ack(Collections.singletonList(lastRecord));
            if (lastRecord instanceof FinishedRecord) {
                channelProgressPairs.remove(each.getKey());
            }
            each.getLeft().getJobProgressListener().onProgressUpdated(new PipelineJobUpdateProgress(ackPosition.getDataRecordCount()));
        }
        ackCache.invalidate(ackId);
    }
    
    @Override
    protected void doStop() {
        CDCImporterManager.removeImporter(importerId);
    }
}
