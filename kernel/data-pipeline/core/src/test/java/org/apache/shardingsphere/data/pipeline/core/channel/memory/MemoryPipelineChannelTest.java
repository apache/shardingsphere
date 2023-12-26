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
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryPipelineChannelTest {
    
    @SneakyThrows(InterruptedException.class)
    @Test
    void assertZeroQueueSizeWorks() {
        MemoryPipelineChannel channel = new MemoryPipelineChannel(0, records -> {
            
        });
        List<Record> records = Collections.singletonList(new PlaceholderRecord(new IngestFinishedPosition()));
        Thread thread = new Thread(() -> channel.push(records));
        thread.start();
        assertThat(channel.fetch(1, 500L), is(records));
        thread.join();
    }
    
    @Test
    void assertFetchRecordsTimeoutCorrectly() {
        MemoryPipelineChannel channel = new MemoryPipelineChannel(10, records -> {
            
        });
        long startMillis = System.currentTimeMillis();
        channel.fetch(1, 1L);
        long delta = System.currentTimeMillis() - startMillis;
        assertTrue(delta >= 1 && delta < 50, "Delta is not in [1,50) : " + delta);
        startMillis = System.currentTimeMillis();
        channel.fetch(1, 500L);
        delta = System.currentTimeMillis() - startMillis;
        assertTrue(delta >= 500 && delta < 750, "Delta is not in [500,750) : " + delta);
    }
}
