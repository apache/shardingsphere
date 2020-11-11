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

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DistributionChannelTest {
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
    }
    
    @Test
    public void assertAckCallbackResultSortable() {
        Record[] records = mockRecords();
        AtomicInteger lastId = new AtomicInteger();
        execute(ackRecords -> {
            for (Record record : ackRecords) {
                final int currentId = ((IntPosition) record.getPosition()).getId();
                assertTrue(currentId > lastId.get());
                lastId.set(currentId);
            }
        }, countDataRecord(records), records);
    }
    
    @Test
    public void assertBroadcastFinishedRecord() {
        execute(records -> assertThat(records.size(), is(2)), 2, new FinishedRecord(new NopPosition()));
    }
    
    @SneakyThrows(InterruptedException.class)
    private void execute(final AckCallback ackCallback, final int count, final Record... records) {
        CountDownLatch countDownLatch = new CountDownLatch(count);
        AtomicBoolean acknowledged = new AtomicBoolean();
        DistributionChannel distributionChannel = new DistributionChannel(2, ackRecords -> {
            ackCallback.onAck(ackRecords);
            acknowledged.set(true);
        });
        fetchWithMultiThreading(distributionChannel, countDownLatch);
        for (Record record : records) {
            distributionChannel.pushRecord(record);
        }
        countDownLatch.await();
        distributionChannel.close();
        assertTrue(acknowledged.get());
    }
    
    private void fetchWithMultiThreading(final DistributionChannel distributionChannel, final CountDownLatch countDownLatch) {
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                while (true) {
                    List<Record> records = distributionChannel.fetchRecords(100, 0);
                    distributionChannel.ack();
                    records.forEach(each -> countDownLatch.countDown());
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
    
    @AllArgsConstructor
    @Getter
    private static final class IntPosition implements Position {
        
        private final int id;
        
        @Override
        public int compareTo(final Position position) {
            return id - ((IntPosition) position).id;
        }
        
        @Override
        public JsonElement toJson() {
            return null;
        }
    }
}
