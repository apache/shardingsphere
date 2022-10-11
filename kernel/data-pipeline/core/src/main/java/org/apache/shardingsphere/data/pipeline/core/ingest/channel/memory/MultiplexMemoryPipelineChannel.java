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

package org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.EmptyAckCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multiplex memory pipeline channel.
 */
@Slf4j
public final class MultiplexMemoryPipelineChannel implements PipelineChannel {
    
    private static final EmptyAckCallback EMPTY_ACK_CALLBACK = new EmptyAckCallback();
    
    private final int channelNumber;
    
    private final PipelineChannel[] channels;
    
    private final Map<String, Integer> channelAssignment = new HashMap<>();
    
    public MultiplexMemoryPipelineChannel() {
        this(EMPTY_ACK_CALLBACK);
    }
    
    public MultiplexMemoryPipelineChannel(final AckCallback ackCallback) {
        this(10000, ackCallback);
    }
    
    public MultiplexMemoryPipelineChannel(final int blockQueueSize, final AckCallback ackCallback) {
        this(1, blockQueueSize, ackCallback);
    }
    
    public MultiplexMemoryPipelineChannel(final int channelNumber, final int blockQueueSize, final AckCallback ackCallback) {
        this.channelNumber = channelNumber;
        channels = new PipelineChannel[channelNumber];
        for (int i = 0; i < channelNumber; i++) {
            channels[i] = new SimpleMemoryPipelineChannel(blockQueueSize, ackCallback);
        }
    }
    
    @Override
    public void pushRecord(final Record record) {
        if (FinishedRecord.class.equals(record.getClass())) {
            for (int i = 0; i < channelNumber; i++) {
                pushRecord(record, i);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            pushRecord(record, Math.abs(record.hashCode() % channelNumber));
        } else if (PlaceholderRecord.class.equals(record.getClass())) {
            pushRecord(record, 0);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }
    
    private void pushRecord(final Record record, final int channelIndex) {
        PipelineChannel channel = channels[channelIndex];
        channel.pushRecord(record);
    }
    
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeoutSeconds) {
        return findChannel().fetchRecords(batchSize, timeoutSeconds);
    }
    
    @Override
    public void ack(final List<Record> records) {
        findChannel().ack(records);
    }
    
    private PipelineChannel findChannel() {
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
        for (PipelineChannel each : channels) {
            each.close();
        }
    }
}
