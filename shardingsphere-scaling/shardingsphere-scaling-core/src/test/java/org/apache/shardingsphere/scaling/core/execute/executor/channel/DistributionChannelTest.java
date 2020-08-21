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
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.utils.ThreadUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DistributionChannelTest {
    
    private DistributionChannel distributionChannel;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
    }
    
    @Test
    @SneakyThrows(InterruptedException.class)
    public void assertAckCallbackResultSortable() {
        distributionChannel = new DistributionChannel(2, records -> {
            assertThat(records.size(), is(2));
            assertTrue(((IntPosition) records.get(0).getPosition()).getId() < ((IntPosition) records.get(1).getPosition()).getId());
        });
        distributionChannel.pushRecord(new PlaceholderRecord(new IntPosition(1)));
        distributionChannel.pushRecord(new PlaceholderRecord(new IntPosition(2)));
        fetchRecordsAndSleep(0);
        fetchRecordsAndSleep(1);
        invokeAckRecords0();
    }
    
    private void fetchRecordsAndSleep(final int millis) {
        new Thread(() -> {
            distributionChannel.fetchRecords(1, 0);
            if (millis > 0) {
                ThreadUtil.sleep(millis);
            }
            distributionChannel.ack();
        }).start();
    }
    
    @Test
    @SneakyThrows(InterruptedException.class)
    public void assertBroadcastFinishedRecord() {
        distributionChannel = new DistributionChannel(2, records -> assertThat(records.size(), is(2)));
        distributionChannel.pushRecord(new FinishedRecord(new NopPosition()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void invokeAckRecords0() {
        Method ackRecords0 = DistributionChannel.class.getDeclaredMethod("ackRecords0");
        ackRecords0.setAccessible(true);
        ackRecords0.invoke(distributionChannel);
    }
    
    @After
    public void tearDown() {
        distributionChannel.close();
    }
    
    @AllArgsConstructor
    @Getter
    private static class IntPosition implements Position {
        
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
