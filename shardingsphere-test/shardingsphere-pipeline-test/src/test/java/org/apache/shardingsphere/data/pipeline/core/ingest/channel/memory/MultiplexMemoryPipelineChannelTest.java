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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.AckCallback;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MultiplexMemoryPipelineChannelTest {
    
    private static final int CHANNEL_NUMBER = 2;
    
    @Test
    public void assertAckCallbackResultSortable() {
        Record[] records = mockRecords();
        execute(ackRecords -> {
            AtomicInteger lastId = new AtomicInteger();
            for (Record record : ackRecords) {
                int currentId = ((IntPosition) record.getPosition()).getId();
                assertTrue(currentId > lastId.get());
                lastId.set(currentId);
            }
        }, countDataRecord(records), records);
    }
    
    @Test
    public void assertBroadcastFinishedRecord() {
        execute(records -> assertThat(records.size(), is(1)), 2, new FinishedRecord(new PlaceholderPosition()));
    }
    
    @SneakyThrows(InterruptedException.class)
    private void execute(final AckCallback ackCallback, final int recordCount, final Record... records) {
        CountDownLatch countDownLatch = new CountDownLatch(recordCount);
        MultiplexMemoryPipelineChannel memoryChannel = new MultiplexMemoryPipelineChannel(CHANNEL_NUMBER, 10000, ackCallback);
        fetchWithMultiThreading(memoryChannel, countDownLatch);
        for (Record record : records) {
            memoryChannel.pushRecord(record);
        }
        boolean awaitResult = countDownLatch.await(10, TimeUnit.SECONDS);
        assertTrue("await failed", awaitResult);
        memoryChannel.close();
    }
    
    private void fetchWithMultiThreading(final MultiplexMemoryPipelineChannel memoryChannel, final CountDownLatch countDownLatch) {
        for (int i = 0; i < CHANNEL_NUMBER; i++) {
            new Thread(() -> {
                int maxLoopCount = 10;
                for (int j = 1; j <= maxLoopCount; j++) {
                    List<Record> records = memoryChannel.fetchRecords(100, 1);
                    memoryChannel.ack(records);
                    records.forEach(each -> countDownLatch.countDown());
                    if (!records.isEmpty() && records.get(records.size() - 1) instanceof FinishedRecord) {
                        break;
                    }
                }
            }).start();
        }
    }
    
    private Record[] mockRecords() {
        Record[] result = new Record[100];
        Random random = new Random();
        for (int i = 1; i <= result.length; i++) {
            if (random.nextBoolean()) {
                result[i - 1] = new DataRecord(new IntPosition(i), 0);
            } else {
                result[i - 1] = new PlaceholderRecord(new IntPosition(i));
            }
        }
        return result;
    }
    
    private int countDataRecord(final Record[] records) {
        return (int) Arrays.stream(records).filter(each -> each instanceof DataRecord).count();
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class IntPosition implements IngestPosition<IntPosition> {
        
        private final int id;
        
        @Override
        public int compareTo(final IntPosition position) {
            return id - position.id;
        }
    }
}
