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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckId;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.common.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
public final class CDCImporter extends AbstractLifecycleExecutor implements Importer {
    
    @Getter
    private final String importerId = RandomStringUtils.randomAlphanumeric(8);
    
    private final List<CDCChannelProgressPair> originalChannelProgressPairs;
    
    private final int batchSize;
    
    private final long timeout;
    
    private final TimeUnit timeUnit;
    
    private final PipelineSink sink;
    
    private final boolean needSorting;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final PriorityQueue<CSNRecords> csnRecordsQueue = new PriorityQueue<>(new CSNRecordsComparator());
    
    private final Cache<String, List<Pair<CDCChannelProgressPair, CDCAckPosition>>> ackCache = Caffeine.newBuilder().maximumSize(10000).expireAfterAccess(5, TimeUnit.MINUTES).build();
    
    @Override
    protected void runBlocking() {
        CDCImporterManager.putImporter(this);
        List<CDCChannelProgressPair> channelProgressPairs = new ArrayList<>(originalChannelProgressPairs);
        while (isRunning()) {
            if (needSorting) {
                doWithSorting(channelProgressPairs);
            } else {
                doWithoutSorting(channelProgressPairs);
            }
            if (channelProgressPairs.isEmpty()) {
                break;
            }
        }
    }
    
    private void doWithoutSorting(final List<CDCChannelProgressPair> channelProgressPairs) {
        Iterator<CDCChannelProgressPair> channelProgressPairsIterator = channelProgressPairs.iterator();
        while (channelProgressPairsIterator.hasNext()) {
            CDCChannelProgressPair channelProgressPair = channelProgressPairsIterator.next();
            PipelineChannel channel = channelProgressPair.getChannel();
            List<Record> records = channel.fetchRecords(batchSize, timeout, timeUnit).stream().filter(each -> !(each instanceof PlaceholderRecord)).collect(Collectors.toList());
            if (records.isEmpty()) {
                continue;
            }
            if (null != rateLimitAlgorithm) {
                rateLimitAlgorithm.intercept(JobOperationType.INSERT, 1);
            }
            String ackId = CDCAckId.build(importerId).marshal();
            ackCache.put(ackId, Collections.singletonList(Pair.of(channelProgressPair, new CDCAckPosition(records.get(records.size() - 1), getDataRecordsCount(records)))));
            sink.write(ackId, records);
            Record lastRecord = records.get(records.size() - 1);
            if (lastRecord instanceof FinishedRecord) {
                channelProgressPairsIterator.remove();
            }
            if (lastRecord instanceof FinishedRecord && records.stream().noneMatch(DataRecord.class::isInstance)) {
                channel.ack(records);
                channelProgressPair.getJobProgressListener().onProgressUpdated(new PipelineJobProgressUpdatedParameter(0));
            }
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void doWithSorting(final List<CDCChannelProgressPair> channelProgressPairs) {
        if (null != rateLimitAlgorithm) {
            rateLimitAlgorithm.intercept(JobOperationType.INSERT, 1);
        }
        CSNRecords firstCsnRecords = null;
        List<CSNRecords> csnRecordsList = new LinkedList<>();
        for (int i = 0, count = channelProgressPairs.size(); i < count; i++) {
            prepareTransactionRecords(channelProgressPairs);
            CSNRecords csnRecords = csnRecordsQueue.peek();
            if (null == csnRecords) {
                continue;
            }
            if (null == firstCsnRecords) {
                csnRecords = csnRecordsQueue.poll();
                firstCsnRecords = csnRecords;
                csnRecordsList.add(csnRecords);
            } else if (csnRecords.getCsn() == firstCsnRecords.getCsn()) {
                csnRecords = csnRecordsQueue.poll();
                csnRecordsList.add(csnRecords);
            }
        }
        if (csnRecordsList.isEmpty()) {
            timeUnit.sleep(timeout);
            return;
        }
        // TODO Combine small transactions into a large transaction, to improve transformation performance.
        String ackId = CDCAckId.build(importerId).marshal();
        if (1 == csnRecordsList.size()) {
            CSNRecords csnRecords = csnRecordsList.get(0);
            List<Record> records = csnRecords.getRecords();
            ackCache.put(ackId, Collections.singletonList(Pair.of(csnRecords.getChannelProgressPair(), new CDCAckPosition(records.get(records.size() - 1), getDataRecordsCount(records)))));
            sink.write(ackId, filterDataRecords(records));
            return;
        }
        List<Pair<CDCChannelProgressPair, CDCAckPosition>> ackValue = csnRecordsList.stream().map(each -> Pair.of(each.getChannelProgressPair(),
                new CDCAckPosition(each.getRecords().get(each.getRecords().size() - 1), getDataRecordsCount(each.getRecords())))).collect(Collectors.toList());
        ackCache.put(ackId, ackValue);
        List<Record> records = new ArrayList<>(ackValue.stream().mapToInt(each -> each.getRight().getDataRecordCount()).sum());
        csnRecordsList.forEach(each -> records.addAll(filterDataRecords(each.getRecords())));
        sink.write(ackId, filterDataRecords(records));
    }
    
    private int getDataRecordsCount(final List<Record> records) {
        return (int) records.stream().filter(DataRecord.class::isInstance).count();
    }
    
    private List<Record> filterDataRecords(final List<Record> records) {
        return records.stream().filter(DataRecord.class::isInstance).map(DataRecord.class::cast).collect(Collectors.toList());
    }
    
    // TODO openGauss CSN should be incremented for every transaction. Currently, CSN might be duplicated in transactions.
    // TODO Use channels watermark depth to improve performance.
    private void prepareTransactionRecords(final List<CDCChannelProgressPair> channelProgressPairs) {
        if (csnRecordsQueue.isEmpty()) {
            prepareWhenQueueIsEmpty(channelProgressPairs);
        } else {
            prepareWhenQueueIsNotEmpty(channelProgressPairs, csnRecordsQueue.peek().getCsn());
        }
    }
    
    private void prepareWhenQueueIsEmpty(final List<CDCChannelProgressPair> channelProgressPairs) {
        for (CDCChannelProgressPair each : channelProgressPairs) {
            PipelineChannel channel = each.getChannel();
            List<Record> records = channel.pollRecords();
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
    
    private DataRecord findFirstDataRecord(final List<Record> records) {
        for (Record each : records) {
            if (each instanceof DataRecord) {
                return (DataRecord) each;
            }
        }
        throw new IllegalStateException("No data record found");
    }
    
    private void prepareWhenQueueIsNotEmpty(final List<CDCChannelProgressPair> channelProgressPairs, final long oldestCSN) {
        for (CDCChannelProgressPair each : channelProgressPairs) {
            PipelineChannel channel = each.getChannel();
            List<Record> records = channel.peekRecords();
            if (records.isEmpty()) {
                continue;
            }
            if (0 == getDataRecordsCount(records)) {
                records = channel.pollRecords();
                channel.ack(records);
                continue;
            }
            long csn = findFirstDataRecord(records).getCsn();
            if (csn <= oldestCSN) {
                records = channel.pollRecords();
                csnRecordsQueue.add(new CSNRecords(csn, each, records));
            }
        }
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
            each.getLeft().getChannel().ack(Collections.singletonList(ackPosition.getLastRecord()));
            each.getLeft().getJobProgressListener().onProgressUpdated(new PipelineJobProgressUpdatedParameter(ackPosition.getDataRecordCount()));
        }
    }
    
    @Override
    protected void doStop() {
        CDCImporterManager.removeImporter(importerId);
    }
}
