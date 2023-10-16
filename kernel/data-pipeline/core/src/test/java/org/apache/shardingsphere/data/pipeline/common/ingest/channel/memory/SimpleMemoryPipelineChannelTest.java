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

import org.apache.shardingsphere.data.pipeline.common.ingest.channel.EmptyAckCallback;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleMemoryPipelineChannelTest {
    
    @Test
    void assertFetchRecordsTimeoutCorrectly() {
        SimpleMemoryPipelineChannel simpleMemoryPipelineChannel = new SimpleMemoryPipelineChannel(10, new EmptyAckCallback());
        long startMills = System.currentTimeMillis();
        simpleMemoryPipelineChannel.fetchRecords(1, 1, TimeUnit.MILLISECONDS);
        long delta = System.currentTimeMillis() - startMills;
        assertTrue(delta >= 1 && delta < 50, "Delta is not in [1,50) : " + delta);
        startMills = System.currentTimeMillis();
        simpleMemoryPipelineChannel.fetchRecords(1, 500, TimeUnit.MILLISECONDS);
        delta = System.currentTimeMillis() - startMills;
        assertTrue(delta >= 500 && delta < 650, "Delta is not in [500,650) : " + delta);
    }
}
