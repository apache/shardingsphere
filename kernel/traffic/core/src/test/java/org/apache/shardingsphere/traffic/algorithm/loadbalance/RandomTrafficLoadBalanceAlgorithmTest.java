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

import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTrafficLoadBalanceAlgorithmTest {
    
    @Test
    void assertGetInstanceId() {
        RandomTrafficLoadBalanceAlgorithm randomAlgorithm = new RandomTrafficLoadBalanceAlgorithm();
        List<InstanceMetaData> instances = Arrays.asList(new ProxyInstanceMetaData("foo_id", "127.0.0.1@3307", "foo_verison"),
                new ProxyInstanceMetaData("bar_id", "127.0.0.1@3308", "foo_verison"));
        assertTrue(instances.contains(randomAlgorithm.getInstanceId("simple_traffic", instances)));
        assertTrue(instances.contains(randomAlgorithm.getInstanceId("simple_traffic", instances)));
        assertTrue(instances.contains(randomAlgorithm.getInstanceId("simple_traffic", instances)));
    }
}
