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

import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertTrue;

public final class AutoAcknowledgeChannelTest {
    
    private AutoAcknowledgeChannel channel;
    
    @Before
    public void setUp() {
        channel = new AutoAcknowledgeChannel();
    }
    
    @Test
    public void assertPushRecord() {
        channel.pushRecord(new DataRecord(new PlaceholderPosition(), 1), 0);
        BitSet bitSet = channel.getAckBitSet(0);
        assertTrue(bitSet.get(0));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertFetchRecordsFailure() {
        channel.fetchRecords(1, 1);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertAckFailure() {
        channel.ack();
    }
}
