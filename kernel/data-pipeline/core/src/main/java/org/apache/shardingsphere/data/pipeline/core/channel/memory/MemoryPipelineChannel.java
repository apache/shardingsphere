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

package org.apache.shardingsphere.data.pipeline.core.channel.memory;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelAckCallback;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Memory pipeline channel.
 */
public final class MemoryPipelineChannel implements PipelineChannel {
    
    private final BlockingQueue<List<Record>> queue;
    
    private final PipelineChannelAckCallback ackCallback;
    
    public MemoryPipelineChannel(final int blockQueueSize, final PipelineChannelAckCallback ackCallback) {
        queue = blockQueueSize < 1 ? new SynchronousQueue<>(true) : new ArrayBlockingQueue<>(blockQueueSize, true);
        this.ackCallback = ackCallback;
    }
    
    @SneakyThrows(InterruptedException.class)
    @Override
    public void push(final List<Record> records) {
        queue.put(records);
    }
    
    @SneakyThrows(InterruptedException.class)
    @Override
    public List<Record> fetch(final int batchSize, final long timeoutMillis) {
        List<Record> result = new LinkedList<>();
        long startMillis = System.currentTimeMillis();
        int recordsCount = 0;
        do {
            List<Record> records = queue.poll(Math.max(0, timeoutMillis - (System.currentTimeMillis() - startMillis)), TimeUnit.MILLISECONDS);
            if (null == records || records.isEmpty()) {
                continue;
            }
            recordsCount += records.size();
            result.addAll(records);
        } while (recordsCount < batchSize && System.currentTimeMillis() - startMillis < timeoutMillis);
        return result;
    }
    
    @Override
    public List<Record> peek() {
        List<Record> result = queue.peek();
        return null == result ? Collections.emptyList() : result;
    }
    
    @Override
    public List<Record> poll() {
        List<Record> result = queue.poll();
        return null == result ? Collections.emptyList() : result;
    }
    
    @Override
    public void ack(final List<Record> records) {
        ackCallback.onAck(records);
    }
}
