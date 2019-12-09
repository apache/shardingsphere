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

import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Memory channel.
 *
 * @author avalon566
 */
public class MemoryChannel implements Channel {

    private static final int PUSH_TIMEOUT = 1000;

    private final BlockingQueue<Record> queue = new ArrayBlockingQueue<>(10000);

    private final List<AckCallback> ackCallbacks;

    private List<Record> toBeAcknowledgeRecords = new LinkedList<>();

    public MemoryChannel() {
        this(new LinkedList<AckCallback>());
    }

    public MemoryChannel(final AckCallback ackCallback) {
        this(Collections.singletonList(ackCallback));
    }

    public MemoryChannel(final List<AckCallback> ackCallbacks) {
        this.ackCallbacks = ackCallbacks;
    }

    @Override
    public final void pushRecord(final Record dataRecord) throws InterruptedException {
        if (!queue.offer(dataRecord, PUSH_TIMEOUT, TimeUnit.HOURS)) {
            throw new RuntimeException();
        }
    }

    @Override
    public final List<Record> fetchRecords(final int batchSize, final int timeout) {
        List<Record> records = new ArrayList<>(batchSize);
        long start = System.currentTimeMillis();
        while (batchSize > queue.size()) {
            if (timeout * 1000 <= System.currentTimeMillis() - start) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        queue.drainTo(records, batchSize);
        if (0 < ackCallbacks.size()) {
            toBeAcknowledgeRecords.addAll(records);
        }
        return records;
    }

    @Override
    public final void ack() {
        if (0 < ackCallbacks.size() && 0 < toBeAcknowledgeRecords.size()) {
            for (AckCallback each : ackCallbacks) {
                each.onAck(toBeAcknowledgeRecords);
            }
            toBeAcknowledgeRecords.clear();
        }
    }
}
