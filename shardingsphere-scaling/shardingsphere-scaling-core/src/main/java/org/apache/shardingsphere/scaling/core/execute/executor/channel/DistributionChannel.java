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

package org.apache.shardingsphere.scaling.core.execute.executor.channel;

import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Distribution channel.
 */
public final class DistributionChannel implements Channel {
    
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
    
    private final Queue<Record> toBeAcknowledgeRecords = new ConcurrentLinkedQueue<>();
    
    private final Map<Position, Record> pendingAcknowledgeRecords = new ConcurrentHashMap<>();
    
    private ScheduledExecutorService scheduleAckRecordsExecutor;
    
    public DistributionChannel(final int channelNumber, final AckCallback ackCallback) {
        this.channelNumber = channelNumber;
        this.ackCallback = ackCallback;
        for (int i = 0; i < channelNumber; i++) {
            channels.put(Integer.toString(i), new MemoryChannel(new SingleChannelAckCallback()));
        }
        scheduleAckRecords();
    }
    
    private void scheduleAckRecords() {
        scheduleAckRecordsExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduleAckRecordsExecutor.scheduleAtFixedRate(this::ackRecords0, 5, 1, TimeUnit.SECONDS);
    }
    
    private synchronized void ackRecords0() {
        List<Record> result = new LinkedList<>();
        while (!toBeAcknowledgeRecords.isEmpty()) {
            Record record = toBeAcknowledgeRecords.peek();
            if (pendingAcknowledgeRecords.containsKey(record.getPosition())) {
                result.add(record);
                toBeAcknowledgeRecords.poll();
                pendingAcknowledgeRecords.remove(record.getPosition());
            } else {
                break;
            }
        }
        if (!result.isEmpty()) {
            ackCallback.onAck(result);
        }
    }
    
    @Override
    public void pushRecord(final Record record) throws InterruptedException {
        if (FinishedRecord.class.equals(record.getClass())) {
            // broadcast
            for (Entry<String, MemoryChannel> entry : channels.entrySet()) {
                entry.getValue().pushRecord(record);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            toBeAcknowledgeRecords.add(record);
            // hash by table name
            DataRecord dataRecord = (DataRecord) record;
            String index = Integer.toString(Math.abs(dataRecord.hashCode()) % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else if (PlaceholderRecord.class.equals(record.getClass())) {
            toBeAcknowledgeRecords.add(record);
            pendingAcknowledgeRecords.put(record.getPosition(), record);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }
    
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeout) {
        return findChannel().fetchRecords(batchSize, timeout);
    }
    
    @Override
    public void ack() {
        findChannel().ack();
    }
    
    @Override
    public void close() {
        for (MemoryChannel each : channels.values()) {
            each.close();
        }
        scheduleAckRecordsExecutor.shutdown();
        ackRecords0();
    }
    
    private Channel findChannel() {
        String threadId = Long.toString(Thread.currentThread().getId());
        checkAssignment(threadId);
        return channels.get(channelAssignment.get(threadId));
    }
    
    private void checkAssignment(final String threadId) {
        if (!channelAssignment.containsKey(threadId)) {
            synchronized (this) {
                if (!channelAssignment.containsKey(threadId)) {
                    assignmentChannel(threadId);
                }
            }
        }
    }
    
    private void assignmentChannel(final String threadId) {
        for (Entry<String, MemoryChannel> entry : channels.entrySet()) {
            if (!channelAssignment.containsValue(entry.getKey())) {
                channelAssignment.put(threadId, entry.getKey());
            }
        }
    }
    
    private final class SingleChannelAckCallback implements AckCallback {
        
        @Override
        public void onAck(final List<Record> records) {
            for (Record record : records) {
                pendingAcknowledgeRecords.put(record.getPosition(), record);
            }
        }
    }
}
