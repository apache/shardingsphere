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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer.connector;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;
import org.apache.shardingsphere.data.pipeline.cdc.generator.CDCResponseGenerator;
import org.apache.shardingsphere.data.pipeline.cdc.holder.CDCAckHolder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult;
import org.apache.shardingsphere.data.pipeline.cdc.util.DataRecordResultConvertUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * CDC importer connector.
 */
@Slf4j
public final class CDCImporterConnector implements ImporterConnector {
    
    private static final long DEFAULT_TIMEOUT_MILLISECONDS = 200L;
    
    private final Lock lock = new ReentrantLock();
    
    private final Condition condition = lock.newCondition();
    
    @Setter
    private volatile boolean running = true;
    
    @Getter
    private final String database;
    
    private final Channel channel;
    
    private final int jobShardingCount;
    
    private final Comparator<DataRecord> dataRecordComparator;
    
    private final Map<String, String> tableNameSchemaMap = new HashMap<>();
    
    private final Map<CDCImporter, BlockingQueue<Record>> incrementalRecordMap = new ConcurrentHashMap<>();
    
    private final AtomicInteger runningIncrementalTaskCount = new AtomicInteger(0);
    
    private Thread incrementalImporterTask;
    
    public CDCImporterConnector(final Channel channel, final String database, final int jobShardingCount, final List<String> tableNames, final Comparator<DataRecord> dataRecordComparator) {
        this.channel = channel;
        this.database = database;
        this.jobShardingCount = jobShardingCount;
        tableNames.stream().filter(each -> each.contains(".")).forEach(each -> {
            String[] split = each.split("\\.");
            tableNameSchemaMap.put(split[0], split[1]);
        });
        this.dataRecordComparator = dataRecordComparator;
    }
    
    @Override
    public Object getConnector() {
        return channel;
    }
    
    /**
     * Write data record into channel.
     *
     * @param recordList data records
     * @param cdcImporter cdc importer
     * @param importerType importer type
     */
    public void write(final List<Record> recordList, final CDCImporter cdcImporter, final ImporterType importerType) {
        if (ImporterType.INVENTORY == importerType || null == dataRecordComparator) {
            Map<CDCImporter, Record> importerDataRecordMap = new HashMap<>();
            importerDataRecordMap.put(cdcImporter, recordList.get(recordList.size() - 1));
            writeImmediately(recordList, importerDataRecordMap);
        } else if (ImporterType.INCREMENTAL == importerType) {
            writeIntoQueue(recordList, cdcImporter);
        }
    }
    
    private void writeImmediately(final List<Record> recordList, final Map<CDCImporter, Record> importerDataRecordMap) {
        while (!channel.isWritable() && channel.isActive()) {
            doAwait();
        }
        List<DataRecordResult.Record> records = new LinkedList<>();
        for (Record each : recordList) {
            if (each instanceof DataRecord) {
                DataRecord dataRecord = (DataRecord) each;
                records.add(DataRecordResultConvertUtil.convertDataRecordToRecord(database, tableNameSchemaMap.get(dataRecord.getTableName()), dataRecord));
            }
        }
        String ackId = CDCAckHolder.getInstance().bindAckId(importerDataRecordMap);
        DataRecordResult dataRecordResult = DataRecordResult.newBuilder().addAllRecords(records).setAckId(ackId).build();
        channel.writeAndFlush(CDCResponseGenerator.succeedBuilder("").setDataRecordResult(dataRecordResult).build());
    }
    
    private void doAwait() {
        lock.lock();
        try {
            condition.await(DEFAULT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void writeIntoQueue(final List<Record> dataRecords, final CDCImporter cdcImporter) {
        BlockingQueue<Record> blockingQueue = incrementalRecordMap.computeIfAbsent(cdcImporter, ignored -> new ArrayBlockingQueue<>(500));
        for (Record each : dataRecords) {
            blockingQueue.put(each);
        }
    }
    
    /**
     * Send finished record event.
     *
     * @param batchSize batch size
     */
    public void sendIncrementalStartEvent(final int batchSize) {
        int count = runningIncrementalTaskCount.incrementAndGet();
        if (count < jobShardingCount || null == dataRecordComparator) {
            return;
        }
        log.debug("start CDC incremental importer");
        if (null == incrementalImporterTask) {
            incrementalImporterTask = new Thread(new CDCIncrementalImporterTask(batchSize));
            incrementalImporterTask.start();
        }
    }
    
    @Override
    public String getType() {
        return "CDC";
    }
    
    @RequiredArgsConstructor
    private final class CDCIncrementalImporterTask implements Runnable {
        
        private final int batchSize;
        
        @Override
        public void run() {
            while (running && null != dataRecordComparator) {
                int index = 0;
                List<Record> dataRecords = new LinkedList<>();
                Map<CDCImporter, Record> pipelineChannelPositions = new HashMap<>(incrementalRecordMap.size(), 1);
                while (index < batchSize) {
                    Map<Record, CDCImporter> recordChannelMap = new HashMap<>(incrementalRecordMap.size(), 1);
                    for (Entry<CDCImporter, BlockingQueue<Record>> entry : incrementalRecordMap.entrySet()) {
                        BlockingQueue<Record> blockingQueue = entry.getValue();
                        if (null == blockingQueue.peek()) {
                            continue;
                        }
                        Record record = blockingQueue.poll();
                        if (record instanceof FinishedRecord) {
                            continue;
                        }
                        recordChannelMap.put(record, entry.getKey());
                        pipelineChannelPositions.computeIfAbsent(entry.getKey(), key -> record);
                    }
                    List<DataRecord> filterRecord = recordChannelMap.keySet().stream().filter(each -> each instanceof DataRecord).map(each -> (DataRecord) each).collect(Collectors.toList());
                    if (filterRecord.isEmpty()) {
                        break;
                    }
                    DataRecord minDataRecord = Collections.min(filterRecord, dataRecordComparator);
                    dataRecords.add(minDataRecord);
                    pipelineChannelPositions.put(recordChannelMap.get(minDataRecord), minDataRecord);
                    index++;
                }
                if (dataRecords.isEmpty()) {
                    ThreadUtil.sleep(200, TimeUnit.MILLISECONDS);
                } else {
                    writeImmediately(dataRecords, pipelineChannelPositions);
                }
            }
        }
    }
}
