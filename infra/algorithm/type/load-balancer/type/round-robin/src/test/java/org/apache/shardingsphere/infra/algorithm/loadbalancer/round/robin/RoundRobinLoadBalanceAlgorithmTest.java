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

package org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RoundRobinLoadBalanceAlgorithmTest {
    
    @Test
    void assertGetAvailableTargetNameWithDefaultStrategy() {
        LoadBalanceAlgorithm loadBalanceAlgorithm = TypedSPILoader.getService(LoadBalanceAlgorithm.class, "ROUND_ROBIN", new Properties());
        String availableTargetName1 = "test_read_ds_1";
        String availableTargetName2 = "test_read_ds_2";
        List<String> availableTargetNames = Arrays.asList(availableTargetName1, availableTargetName2);
        assertRoundRobinLoadBalance(availableTargetName1, availableTargetName2, loadBalanceAlgorithm, availableTargetNames);
    }
    
    private void assertRoundRobinLoadBalance(final String availableTargetName1, final String availableTargetName2, final LoadBalanceAlgorithm loadBalanceAlgorithm,
                                             final List<String> availableTargetNames) {
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), is(availableTargetName1));
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), is(availableTargetName2));
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), is(availableTargetName1));
        assertThat(loadBalanceAlgorithm.getTargetName("ds", availableTargetNames), is(availableTargetName2));
    }
}
