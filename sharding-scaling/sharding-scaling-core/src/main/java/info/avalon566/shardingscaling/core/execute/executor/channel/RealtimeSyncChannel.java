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

package info.avalon566.shardingscaling.core.execute.executor.channel;

import info.avalon566.shardingscaling.core.execute.executor.reader.LogPosition;
import info.avalon566.shardingscaling.core.execute.executor.record.DataRecord;
import info.avalon566.shardingscaling.core.execute.executor.record.FinishedRecord;
import info.avalon566.shardingscaling.core.execute.executor.record.PlaceholderRecord;
import info.avalon566.shardingscaling.core.execute.executor.record.Record;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Realtime data execute channel.
 *
 * @author avalon566
 */
public class RealtimeSyncChannel implements Channel {

    private final int channelNumber;

    /**
     * key = channel id, value = channel.
     */
    private final Map<String, MemoryChannel> channels = new HashMap<>();

    /**
     * key = thread id, value = channel id.
     */
    private final Map<String, String> channelAssignment = new HashMap<>();

    private final List<AckCallback> ackCallbacks;

    private List<Record> toBeAcknowledgeRecords = new LinkedList<>();

    private Map<LogPosition, Record> pendingAcknowledgeRecords = new ConcurrentHashMap<>();

    private final Timer timer = new Timer();

    public RealtimeSyncChannel(final int channelNumber) {
        this(channelNumber, new LinkedList<AckCallback>());
    }

    public RealtimeSyncChannel(final int channelNumber, final List<AckCallback> ackCallbacks) {
        this.channelNumber = channelNumber;
        this.ackCallbacks = ackCallbacks;
        for (int i = 0; i < channelNumber; i++) {
            if (0 < ackCallbacks.size()) {
                channels.put(Integer.toString(i), new MemoryChannel(Collections.singletonList((AckCallback) new SingleChannelAckCallback())));
            } else {
                channels.put(Integer.toString(i), new MemoryChannel());
            }
        }
        scheduleAckRecords();
    }

    @Override
    public final synchronized void pushRecord(final Record record) throws InterruptedException {
        if (FinishedRecord.class.equals(record.getClass())) {
            // broadcast
            for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                entry.getValue().pushRecord(record);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            if (0 < ackCallbacks.size()) {
                toBeAcknowledgeRecords.add(record);
            }
            // hash by table name
            DataRecord dataRecord = (DataRecord) record;
            String index = Integer.toString(dataRecord.getTableName().hashCode() % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else if (PlaceholderRecord.class.equals(record.getClass())) {
            if (0 < ackCallbacks.size()) {
                toBeAcknowledgeRecords.add(record);
                pendingAcknowledgeRecords.put(record.getLogPosition(), record);
            }
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }

    @Override
    public final List<Record> fetchRecords(final int batchSize, final int timeout) {
        return findChannel().fetchRecords(batchSize, timeout);
    }

    @Override
    public final synchronized void ack() {
        findChannel().ack();
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

    private void scheduleAckRecords() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (RealtimeSyncChannel.this) {
                    Iterator<Record> iterator = toBeAcknowledgeRecords.iterator();
                    List<Record> result = new LinkedList<>();
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        if (pendingAcknowledgeRecords.containsKey(record.getLogPosition())) {
                            result.add(record);
                            iterator.remove();
                            pendingAcknowledgeRecords.remove(record.getLogPosition());
                        } else {
                            break;
                        }
                    }
                    if (0 < ackCallbacks.size() && 0 < result.size()) {
                        for (AckCallback each : ackCallbacks) {
                            each.onAck(result);
                        }
                    }
                }
            }
        }, 5000, 1000);
    }

    class SingleChannelAckCallback implements AckCallback {

        @Override
        public void onAck(final List<Record> records) {
            for (Record record : records) {
                pendingAcknowledgeRecords.put(record.getLogPosition(), record);
            }
        }
    }
}
