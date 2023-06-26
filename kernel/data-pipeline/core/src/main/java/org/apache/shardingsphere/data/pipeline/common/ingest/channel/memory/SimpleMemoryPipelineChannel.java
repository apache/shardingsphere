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

package org.apache.shardingsphere.data.pipeline.common.ingest.channel.memory;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Simple memory pipeline channel.
 */
public final class SimpleMemoryPipelineChannel implements PipelineChannel {
    
    private final BlockingQueue<List<Record>> queue;
    
    private final AckCallback ackCallback;
    
    public SimpleMemoryPipelineChannel(final int blockQueueSize, final AckCallback ackCallback) {
        this.queue = new ArrayBlockingQueue<>(blockQueueSize);
        this.ackCallback = ackCallback;
    }
    
    @SneakyThrows(InterruptedException.class)
    @Override
    public void pushRecords(final List<Record> records) {
        queue.put(records);
    }
    
    @SneakyThrows(InterruptedException.class)
    // TODO thread-safe?
    @Override
    public List<Record> fetchRecords(final int batchSize, final long timeout, final TimeUnit timeUnit) {
        List<Record> result = new LinkedList<>();
        long startMillis = System.currentTimeMillis();
        long timeoutMillis = timeUnit.toMillis(timeout);
        int recordsCount = 0;
        do {
            List<Record> records = queue.poll();
            if (null == records || records.isEmpty()) {
                TimeUnit.MILLISECONDS.sleep(Math.min(100, timeoutMillis));
            } else {
                recordsCount += records.size();
                result.addAll(records);
            }
            if (recordsCount >= batchSize) {
                break;
            }
        } while (System.currentTimeMillis() - startMillis < timeoutMillis);
        return result;
    }
    
    @Override
    public List<Record> peekRecords() {
        List<Record> result = queue.peek();
        return null != result ? result : Collections.emptyList();
    }
    
    @Override
    public List<Record> pollRecords() {
        List<Record> result = queue.poll();
        return null != result ? result : Collections.emptyList();
    }
    
    @Override
    public void ack(final List<Record> records) {
        ackCallback.onAck(records);
    }
    
    @Override
    public void close() {
        queue.clear();
    }
}
