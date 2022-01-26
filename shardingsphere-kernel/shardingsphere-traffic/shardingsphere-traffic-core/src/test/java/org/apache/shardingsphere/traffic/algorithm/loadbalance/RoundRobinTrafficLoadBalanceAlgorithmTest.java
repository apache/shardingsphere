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

public final class RoundRobinTrafficLoadBalanceAlgorithmTest {
    
    private final RoundRobinTrafficLoadBalanceAlgorithm roundRobinAlgorithm = new RoundRobinTrafficLoadBalanceAlgorithm();
    
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        Field field = RoundRobinTrafficLoadBalanceAlgorithm.class.getDeclaredField("COUNTS");
        field.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) field.get(RoundRobinTrafficLoadBalanceAlgorithm.class)).clear();
    }
    
    @Test
    public void assertGetInstanceId() {
        String instanceId1 = "127.0.0.1@3307";
        String instanceId2 = "127.0.0.1@3308";
        List<String> instanceIds = Arrays.asList(instanceId1, instanceId2);
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instanceIds), is(instanceId1));
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instanceIds), is(instanceId2));
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instanceIds), is(instanceId1));
    }
    
    @Test
    public void assertGetType() {
        assertThat(roundRobinAlgorithm.getType(), is("ROUND_ROBIN"));
    }
    
    @Test
    public void assertIsDefault() {
        assertTrue(roundRobinAlgorithm.isDefault());
    }
}
