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

package org.apache.shardingsphere.data.pipeline.core.channel;

import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Multiplex pipeline channel.
 *
 * @deprecated It's not used for now since possible ack issue.
 */
@Deprecated
public final class MultiplexPipelineChannel implements PipelineChannel {
    
    private final int channelCount;
    
    private final List<PipelineChannel> channels;
    
    private final Map<String, Integer> channelAssignment = new HashMap<>();
    
    public MultiplexPipelineChannel(final int channelCount, final PipelineChannelCreator channelCreator, final int importerBatchSize, final PipelineChannelAckCallback ackCallback) {
        this.channelCount = channelCount;
        channels = IntStream.range(0, channelCount).mapToObj(each -> channelCreator.newInstance(importerBatchSize, ackCallback)).collect(Collectors.toList());
    }
    
    @Override
    public void push(final List<Record> records) {
        Record firstRecord = records.get(0);
        if (1 == records.size()) {
            push(firstRecord);
            return;
        }
        long insertDataRecordsCount = records.stream().filter(DataRecord.class::isInstance).map(DataRecord.class::cast).filter(each -> PipelineSQLOperationType.INSERT == each.getType()).count();
        if (insertDataRecordsCount == records.size()) {
            channels.get(Math.abs(firstRecord.hashCode() % channelCount)).push(records);
            return;
        }
        for (Record each : records) {
            push(each);
        }
    }
    
    private void push(final Record ingestedRecord) {
        List<Record> records = Collections.singletonList(ingestedRecord);
        if (ingestedRecord instanceof FinishedRecord) {
            for (int i = 0; i < channelCount; i++) {
                channels.get(i).push(records);
            }
        } else if (DataRecord.class.equals(ingestedRecord.getClass())) {
            channels.get(Math.abs(ingestedRecord.hashCode() % channelCount)).push(records);
        } else if (PlaceholderRecord.class.equals(ingestedRecord.getClass())) {
            channels.get(0).push(records);
        } else {
            throw new UnsupportedOperationException("Unsupported record type: " + ingestedRecord.getClass().getName());
        }
    }
    
    @Override
    public List<Record> fetch(final int batchSize, final long timeoutMills) {
        return findChannel().fetch(batchSize, timeoutMills);
    }
    
    @Override
    public List<Record> peek() {
        return findChannel().peek();
    }
    
    @Override
    public List<Record> poll() {
        return findChannel().poll();
    }
    
    @Override
    public void ack(final List<Record> records) {
        findChannel().ack(records);
    }
    
    private PipelineChannel findChannel() {
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
        for (int i = 0; i < channels.size(); i++) {
            if (!channelAssignment.containsValue(i)) {
                channelAssignment.put(threadId, i);
                return;
            }
        }
    }
}
