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

package org.apache.shardingsphere.data.pipeline.core.channel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiplexPipelineChannelTest {
    
    private static final int CHANNEL_NUMBER = 2;
    
    private final Random random = new SecureRandom();
    
    @Test
    void assertAckCallbackResultSortable() {
        Record[] records = mockRecords();
        execute(ackRecords -> {
            AtomicInteger lastId = new AtomicInteger();
            for (Record each : ackRecords) {
                int currentId = ((IntPosition) each.getPosition()).getId();
                assertTrue(currentId > lastId.get());
                lastId.set(currentId);
            }
        }, countDataRecord(records), records);
    }
    
    private Record[] mockRecords() {
        Record[] result = new Record[100];
        for (int i = 1; i <= result.length; i++) {
            result[i - 1] = random.nextBoolean() ? new DataRecord(PipelineSQLOperationType.INSERT, "t1", new IntPosition(i), 0) : new PlaceholderRecord(new IntPosition(i));
        }
        return result;
    }
    
    private int countDataRecord(final Record[] records) {
        return (int) Arrays.stream(records).filter(each -> each instanceof DataRecord).count();
    }
    
    @Test
    void assertBroadcastFinishedRecord() {
        execute(records -> assertThat(records.size(), is(1)), 2, new FinishedRecord(new IngestPlaceholderPosition()));
    }
    
    @SneakyThrows(InterruptedException.class)
    private void execute(final PipelineChannelAckCallback ackCallback, final int recordCount, final Record... records) {
        CountDownLatch countDownLatch = new CountDownLatch(recordCount);
        MultiplexPipelineChannel channel = new MultiplexPipelineChannel(CHANNEL_NUMBER, TypedSPILoader.getService(PipelineChannelCreator.class, "MEMORY"), 10000, ackCallback);
        fetchWithMultiThreads(channel, countDownLatch);
        channel.push(Arrays.asList(records));
        boolean awaitResult = countDownLatch.await(10, TimeUnit.SECONDS);
        assertTrue(awaitResult, "await failed");
    }
    
    private void fetchWithMultiThreads(final MultiplexPipelineChannel memoryChannel, final CountDownLatch countDownLatch) {
        for (int i = 0; i < CHANNEL_NUMBER; i++) {
            new Thread(() -> fetch(memoryChannel, countDownLatch)).start();
        }
    }
    
    private void fetch(final MultiplexPipelineChannel memoryChannel, final CountDownLatch countDownLatch) {
        int maxLoopCount = 10;
        for (int j = 1; j <= maxLoopCount; j++) {
            List<Record> records = memoryChannel.fetch(100, 1000L);
            memoryChannel.ack(records);
            records.forEach(each -> countDownLatch.countDown());
            if (!records.isEmpty() && records.get(records.size() - 1) instanceof FinishedRecord) {
                break;
            }
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class IntPosition implements IngestPosition {
        
        private final int id;
    }
}
