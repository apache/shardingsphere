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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;

import java.util.BitSet;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Abstract BitSet channel.
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class AbstractBitSetChannel implements BitSetChannel {
    
    private final Deque<Record> toBeAckRecords = new ConcurrentLinkedDeque<>();
    
    private final ManualBitSet manualBitSet = new ManualBitSet();
    
    private long acknowledgedIndex;
    
    @Override
    public BitSet getAckBitSet(final long fromIndex) {
        return manualBitSet.get(fromIndex, acknowledgedIndex);
    }
    
    @Override
    public Record removeAckRecord() {
        return toBeAckRecords.remove();
    }
    
    @Override
    public void clear(final long index) {
        manualBitSet.clear(index);
    }
    
    @Override
    public void close() {
        toBeAckRecords.clear();
    }
}
