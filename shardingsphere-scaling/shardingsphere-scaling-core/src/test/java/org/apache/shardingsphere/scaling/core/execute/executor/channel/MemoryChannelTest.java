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

import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MemoryChannelTest {
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
    }
    
    @Test
    public void assertFetchRecords() throws InterruptedException {
        MemoryChannel memoryChannel = new MemoryChannel(records -> {
        });
        memoryChannel.pushRecord(new DataRecord(new PlaceholderPosition(), 1));
        List<Record> records = memoryChannel.fetchRecords(10, 1);
        assertThat(records.size(), is(1));
    }
    
    @Test
    public void assertAck() throws InterruptedException {
        MemoryChannel memoryChannel = new MemoryChannel(records -> assertThat(records.size(), is(1)));
        memoryChannel.pushRecord(new DataRecord(new PlaceholderPosition(), 1));
        memoryChannel.fetchRecords(1, 1);
        memoryChannel.ack();
    }
}
