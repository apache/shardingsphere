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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.bitset.AutoAcknowledgeBitSetChannel;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.bitset.BitSetChannel;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.bitset.BlockingQueueBitSetChannel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Distribution channel.
 */
@Slf4j
public final class DistributionChannel implements Channel {
    
    private final int channelNumber;
    
    private final BitSetChannel[] channels;
    
    private final BitSetChannel autoAckChannel = new AutoAcknowledgeBitSetChannel();
    
    private final Map<String, Integer> channelAssignment = new HashMap<>();
    
    private final AckCallback ackCallback;
    
    private final AtomicLong indexAutoIncreaseGenerator = new AtomicLong();
    
    private final Queue<Integer> toBeAckBitSetIndexes = new ConcurrentLinkedQueue<>();
    
    private long lastAckIndex;
    
    private ScheduledExecutorService scheduleAckRecordsExecutor;
    
    public DistributionChannel(final int channelNumber, final AckCallback ackCallback) {
        this.channelNumber = channelNumber;
        this.ackCallback = ackCallback;
        channels = new BitSetChannel[channelNumber];
        for (int i = 0; i < channelNumber; i++) {
            channels[i] = new BlockingQueueBitSetChannel();
        }
        scheduleAckRecords();
    }
    
    private void scheduleAckRecords() {
        scheduleAckRecordsExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduleAckRecordsExecutor.scheduleWithFixedDelay(this::ackRecords0, 5, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void pushRecord(final Record record) throws InterruptedException {
        if (FinishedRecord.class.equals(record.getClass())) {
            for (int i = 0; i < channels.length; i++) {
                pushRecord(record, i);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            pushRecord(record, Math.abs(record.hashCode() % channelNumber));
        } else if (PlaceholderRecord.class.equals(record.getClass())) {
            pushRecord(record, -1);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }
    
    private void pushRecord(final Record record, final int index) throws InterruptedException {
        toBeAckBitSetIndexes.add(index);
        getBitSetChannel(index).pushRecord(record, indexAutoIncreaseGenerator.getAndIncrement());
    }
    
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeout) {
        return findChannel().fetchRecords(batchSize, timeout);
    }
    
    @Override
    public void ack() {
        findChannel().ack();
    }
    
    private synchronized void ackRecords0() {
        try {
            int count = shouldAckCount();
            if (0 == count) {
                return;
            }
            ackCallback.onAck(fetchAckRecords(count));
            lastAckIndex += count;
            for (BitSetChannel channel : channels) {
                channel.clear(lastAckIndex);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("distribution channel auto ack failed.", ex);
        }
    }
    
    private int shouldAckCount() {
        BitSet bitSet = autoAckChannel.getAckBitSet(lastAckIndex);
        for (BitSetChannel channel : channels) {
            bitSet.or(channel.getAckBitSet(lastAckIndex));
        }
        return bitSet.nextClearBit(0);
    }
    
    private List<Record> fetchAckRecords(final int count) {
        List<Record> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(getBitSetChannel(toBeAckBitSetIndexes.remove()).removeAckRecord());
        }
        return result;
    }
    
    private BitSetChannel getBitSetChannel(final Integer index) {
        return index == -1 ? autoAckChannel : channels[index];
    }
    
    private BitSetChannel findChannel() {
        String threadId = Long.toString(Thread.currentThread().getId());
        checkAssignment(threadId);
        return channels[channelAssignment.get(threadId)];
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
        for (int i = 0; i < channels.length; i++) {
            if (!channelAssignment.containsValue(i)) {
                channelAssignment.put(threadId, i);
                return;
            }
        }
    }
    
    @Override
    public void close() {
        scheduleAckRecordsExecutor.shutdown();
        ackRecords0();
        for (BitSetChannel each : channels) {
            each.close();
        }
        toBeAckBitSetIndexes.clear();
    }
}
