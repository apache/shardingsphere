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

import java.util.List;

/**
 * Auto Acknowledge BitSet channel.
 */
public final class AutoAcknowledgeBitSetChannel extends AbstractBitSetChannel {
    
    @Override
    public void pushRecord(final Record dataRecord, final long index) {
        getManualBitSet().set(index);
        getToBeAckRecords().add(dataRecord);
        setAcknowledgedIndex(index);
    }
    
    @Override
    public List<Record> fetchRecords(final int batchSize, final int timeout) {
        throw new UnsupportedOperationException("Auto ack channel can not fetch records.");
    }
    
    @Override
    public void ack() {
        throw new UnsupportedOperationException("Auto ack channel do not have to ack.");
    }
}
