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

package info.avalon566.shardingscaling.core.sync.channel;

import info.avalon566.shardingscaling.core.sync.reader.LogPosition;
import info.avalon566.shardingscaling.core.sync.record.DataRecord;
import info.avalon566.shardingscaling.core.sync.record.FinishedRecord;
import info.avalon566.shardingscaling.core.sync.record.Record;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * One provider to multi consumer channel model.
 *
 * @author avalon566
 */
public class DispatcherChannel implements Channel {

    private final int channelNumber;

    /**
     * key = channel id, value = channel.
     */
    private final Map<String, MemoryChannel> channels = new HashMap<>();

    /**
     * key = thread id, value = channel id.
     */
    private final Map<String, String> channelAssignment = new HashMap<>();

    private final List<LogPositionWrapper> pendingLogPosition = new ArrayList<>();

    private final Map<String, List<Record>> consumePendingLogPosition = new HashMap<>();

    private LogPosition currentLogPosition;

    public DispatcherChannel(final int channelNumber) {
        this.channelNumber = channelNumber;
        for (int i = 0; i < channelNumber; i++) {
            channels.put(Integer.toString(i), new MemoryChannel());
        }
    }

    @Override
    public final synchronized void pushRecord(final Record record) throws InterruptedException {
        pendingLogPosition.add(new LogPositionWrapper(record.getLogPosition()));
        if (FinishedRecord.class.equals(record.getClass())) {
            // 广播事件
            for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                entry.getValue().pushRecord(record);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            // 表名哈希
            DataRecord dataRecord = (DataRecord) record;
            String index = Integer.toString(dataRecord.getTableName().hashCode() % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }

    @Override
    public final List<Record> fetchRecords(final int batchSize, final int timeout) {
        List<Record> records = findChannel().fetchRecords(batchSize, timeout);
        consumePendingLogPosition.put(Long.toString(Thread.currentThread().getId()), records);
        return records;
    }

    @Override
    public final synchronized void ack() {
        String threadId = Long.toString(Thread.currentThread().getId());
        List<Record> records = consumePendingLogPosition.get(threadId);
        if (null == records || 0 == records.size()) {
            return;
        }
        for (Record record : records) {
            int index = Collections.binarySearch(pendingLogPosition, new LogPositionWrapper(record.getLogPosition()));
            pendingLogPosition.get(index).setAck(true);
        }
        Iterator<LogPositionWrapper> it = pendingLogPosition.iterator();
        while (it.hasNext()) {
            LogPositionWrapper entry = it.next();
            if (entry.isAck()) {
                it.remove();
                currentLogPosition = entry.getLogPosition();
            } else {
                break;
            }
        }
    }

    private Channel findChannel() {
        String threadId = Long.toString(Thread.currentThread().getId());
        checkAssignment(threadId);
        return channels.get(channelAssignment.get(threadId));
    }

    private void checkAssignment(final String threadId) {
        if (!channelAssignment.containsKey(threadId)) {
            synchronized (this) {
                for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                    if (!channelAssignment.containsValue(entry.getKey())) {
                        channelAssignment.put(threadId, entry.getKey());
                    }
                }
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    class LogPositionWrapper implements Comparable<LogPositionWrapper> {

        private final LogPosition logPosition;

        private boolean ack;

        @Override
        public int compareTo(final LogPositionWrapper logPositionWrapper) {
            if (logPositionWrapper == null) {
                return 1;
            }
            return logPosition.compareTo(logPositionWrapper.getLogPosition());
        }
    }
}
