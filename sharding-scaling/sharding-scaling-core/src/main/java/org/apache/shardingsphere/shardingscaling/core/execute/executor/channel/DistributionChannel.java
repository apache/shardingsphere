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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.channel;

import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Realtime data execute channel.
 *
 * @author avalon566
 */
public class DistributionChannel implements Channel {

    private final int channelNumber;

    /**
     * key = channel id, value = channel.
     */
    private final Map<String, MemoryChannel> channels = new HashMap<>();

    /**
     * key = thread id, value = channel id.
     */
    private final Map<String, String> channelAssignment = new HashMap<>();

    private final AckCallback ackCallback;
    
    private Map<String, Record> newestAcknowledgeRecords = new ConcurrentHashMap<>();

    private long lastedAckTime;
    
    public DistributionChannel(final int channelNumber, final AckCallback ackCallback) {
        this.channelNumber = channelNumber;
        this.ackCallback = ackCallback;
        for (int i = 0; i < channelNumber; i++) {
            channels.put(Integer.toString(i), new MemoryChannel(null == ackCallback ? null : new SingleChannelAckCallback()));
        }
    }

    @Override
    public final synchronized void pushRecord(final Record record) throws InterruptedException {
        if (FinishedRecord.class.equals(record.getClass())) {
            // broadcast
            for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                entry.getValue().pushRecord(record);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            // hash by table name
            DataRecord dataRecord = (DataRecord) record;
            String index = Integer.toString(Math.abs(dataRecord.getTableName().hashCode()) % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else if (PlaceholderRecord.class.equals(record.getClass())) {
            if (null != ackCallback) {
                newestAcknowledgeRecords.put(PlaceholderRecord.class.getSimpleName(), record);
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
        ack0();
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
    
    private void ack0() {
        long currentTime = System.currentTimeMillis();
        if (lastedAckTime < currentTime - 1000) {
            lastedAckTime = currentTime;
            List<Record> result = new LinkedList<>(newestAcknowledgeRecords.values());
            Collections.sort(result, new Comparator<Record>() {
    
                @Override
                public int compare(final Record o1, final Record o2) {
                    return o1.getLogPosition().compareTo(o2.getLogPosition());
                }
            });
            ackCallback.onAck(result);
        }
    }
    
    class SingleChannelAckCallback implements AckCallback {

        @Override
        public void onAck(final List<Record> records) {
            newestAcknowledgeRecords.put(Long.toString(Thread.currentThread().getId()), records.get(records.size() - 1));
        }
    }
}
