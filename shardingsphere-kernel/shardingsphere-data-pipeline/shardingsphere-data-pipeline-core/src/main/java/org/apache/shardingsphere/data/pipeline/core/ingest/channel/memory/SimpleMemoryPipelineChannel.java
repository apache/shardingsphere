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

import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.EmptyAckCallback;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Simple memory pipeline channel.
 */
public final class SimpleMemoryPipelineChannel implements PipelineChannel {
    
    private static final EmptyAckCallback EMPTY_ACK_CALLBACK = new EmptyAckCallback();
    
    private final BlockingQueue<Record> queue;
    
    private final AckCallback ackCallback;
    
    public SimpleMemoryPipelineChannel(final int blockQueueSize) {
        this(blockQueueSize, EMPTY_ACK_CALLBACK);
    }
    
    public SimpleMemoryPipelineChannel(final int blockQueueSize, final AckCallback ackCallback) {
        this.queue = new ArrayBlockingQueue<>(blockQueueSize);
        this.ackCallback = ackCallback;
    }
    
    @Override
    public void pushRecord(final Record dataRecord) {
        try {
            queue.put(dataRecord);
        } catch (final InterruptedException ex) {
            throw new RuntimeException("put " + dataRecord + " into queue failed", ex);
        }
    }
    
    // TODO thread-safe?
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeoutSeconds) {
        List<Record> result = new ArrayList<>(batchSize);
        long start = System.currentTimeMillis();
        while (batchSize > queue.size()) {
            if (timeoutSeconds * 1000L <= System.currentTimeMillis() - start) {
                break;
            }
            ThreadUtil.sleep(100L);
        }
        queue.drainTo(result, batchSize);
        return result;
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
