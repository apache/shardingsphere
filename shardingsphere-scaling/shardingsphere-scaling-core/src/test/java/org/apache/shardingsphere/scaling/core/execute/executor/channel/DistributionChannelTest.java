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
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.utils.ThreadUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DistributionChannelTest {
    
    private DistributionChannel distributionChannel;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
    }
    
    @Test
    public void assertAckCallbackResultSortable() throws InterruptedException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        distributionChannel = new DistributionChannel(2, records -> {
            assertThat(records.size(), is(2));
            assertTrue(((IntPosition) records.get(0).getPosition()).getId() < ((IntPosition) records.get(1).getPosition()).getId());
        });
        distributionChannel.pushRecord(new PlaceholderRecord(new IntPosition(1)));
        distributionChannel.pushRecord(new PlaceholderRecord(new IntPosition(2)));
        fetchRecordsAndSleep(0);
        fetchRecordsAndSleep(1);
        ReflectionUtil.invokeMethod(distributionChannel, "ackRecords0");
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
    public void assertBroadcastFinishedRecord() throws InterruptedException {
        distributionChannel = new DistributionChannel(2, records -> assertThat(records.size(), is(2)));
        distributionChannel.pushRecord(new FinishedRecord(new NopPosition()));
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
