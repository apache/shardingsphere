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

package org.apache.shardingsphere.scaling.core.execute.executor.channel.bitset;

import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Blocking queue BitSet channel.
 */
public final class BlockingQueueChannel extends AbstractBitSetChannel {
    
    private final BlockingQueue<Record> queue = new ArrayBlockingQueue<>(ScalingContext.getInstance().getServerConfig().getBlockQueueSize());
    
    private long fetchedIndex;
    
    @Override
    public void pushRecord(final Record dataRecord, final long index) throws InterruptedException {
        getManualBitSet().set(index);
        queue.put(dataRecord);
    }
    
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeout) {
        List<Record> result = new ArrayList<>(batchSize);
        long start = System.currentTimeMillis();
        while (batchSize > queue.size()) {
            if (timeout * 1000L <= System.currentTimeMillis() - start) {
                break;
            }
            ThreadUtil.sleep(100L);
        }
        queue.drainTo(result, batchSize);
        getToBeAckRecords().addAll(result);
        fetchedIndex = getManualBitSet().getEndIndex(fetchedIndex, result.size());
        return result;
    }
    
    @Override
    public void ack() {
        setAcknowledgedIndex(fetchedIndex);
    }
    
    @Override
    public void close() {
        queue.clear();
        super.close();
    }
}
