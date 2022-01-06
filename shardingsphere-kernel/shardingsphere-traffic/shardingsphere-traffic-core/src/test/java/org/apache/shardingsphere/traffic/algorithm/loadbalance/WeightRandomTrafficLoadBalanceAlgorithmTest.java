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

package org.apache.shardingsphere.traffic.algorithm.loadbalance;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class WeightRandomTrafficLoadBalanceAlgorithmTest {
    
    private static final int TEST_COUNT = 30;
    
    private final WeightRandomTrafficLoadBalanceAlgorithm weightRandomAlgorithm = new WeightRandomTrafficLoadBalanceAlgorithm();
    
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        Field field = WeightRandomTrafficLoadBalanceAlgorithm.class.getDeclaredField("WEIGHT_MAP");
        field.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) field.get(WeightRandomTrafficLoadBalanceAlgorithm.class)).clear();
    }
    
    @Test
    public void assertGetInstanceId() {
        String instanceId1 = "127.0.0.1@3307";
        String instanceId2 = "127.0.0.1@3308";
        weightRandomAlgorithm.getProps().put(instanceId1, "1");
        weightRandomAlgorithm.getProps().put(instanceId2, "2");
        int count1 = 0;
        int count2 = 0;
        List<String> instanceIds = Arrays.asList(instanceId1, instanceId2);
        for (int index = 0; index < TEST_COUNT; index++) {
            String instanceId = weightRandomAlgorithm.getInstanceId("simple_traffic", instanceIds);
            if (instanceId1.equals(instanceId)) {
                count1++;
            } else {
                count2++;
            }
        }
        int count = count2 + count1;
        int difference = count2 - count1;
        assertThat(count, is(TEST_COUNT));
        assertTrue(difference > 0);
    }
}
