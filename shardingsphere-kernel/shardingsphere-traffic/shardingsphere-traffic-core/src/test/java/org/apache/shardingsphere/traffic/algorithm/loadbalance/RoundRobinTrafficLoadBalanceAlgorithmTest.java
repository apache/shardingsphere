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
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class RoundRobinTrafficLoadBalanceAlgorithmTest {
    
    @Test
    public void assertGetInstanceId() {
        InstanceMetaData instance1 = new ProxyInstanceMetaData("127.0.0.1@3307", "127.0.0.1@3307");
        InstanceMetaData instance2 = new ProxyInstanceMetaData("127.0.0.1@3308", "127.0.0.1@3308");
        List<InstanceMetaData> instances = Arrays.asList(instance1, instance2);
        RoundRobinTrafficLoadBalanceAlgorithm roundRobinAlgorithm = new RoundRobinTrafficLoadBalanceAlgorithm();
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instances), is(instance1));
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instances), is(instance2));
        assertThat(roundRobinAlgorithm.getInstanceId("simple_traffic", instances), is(instance1));
    }
}
