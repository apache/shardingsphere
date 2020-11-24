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

import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;

import java.util.BitSet;
import java.util.List;

/**
 * BitSet channel interface.
 */
public interface BitSetChannel {
    
    /**
     * Push a {@code DataRecord} with index to channel.
     *
     * @param dataRecord data
     * @param index data index
     * @throws InterruptedException if thread interrupted
     */
    void pushRecord(Record dataRecord, long index) throws InterruptedException;
    
    /**
     * Fetch {@code Record} from channel, if the timeout also returns the record.
     *
     * @param batchSize record batch size
     * @param timeout timeout(seconds)
     * @return record
     */
    List<Record> fetchRecords(int batchSize, int timeout);
    
    /**
     * Ack the last batch.
     */
    void ack();
    
    /**
     * Get acknowledged BitSet.
     *
     * @param fromIndex from index
     * @return BitSet
     */
    BitSet getAckBitSet(long fromIndex);
    
    /**
     * Remove earliest acknowledged record.
     *
     * @return record
     */
    Record removeAckRecord();
    
    /**
     * Clear BitSet.
     *
     * @param index BitSet index
     */
    void clear(long index);
    
    /**
     * Close channel.
     */
    void close();
}
