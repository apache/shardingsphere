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

package org.apache.shardingsphere.masterslave.algorithm;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RoundRobinMasterSlaveLoadBalanceAlgorithmTest {
    
    private final RoundRobinMasterSlaveLoadBalanceAlgorithm roundRobinMasterSlaveLoadBalanceAlgorithm = new RoundRobinMasterSlaveLoadBalanceAlgorithm();
    
    @Test
    public void assertGetDataSource() {
        String masterDataSourceName = "test_ds_master";
        String slaveDataSourceName1 = "test_ds_slave_1";
        String slaveDataSourceName2 = "test_ds_slave_2";
        List<String> slaveDataSourceNames = Arrays.asList(slaveDataSourceName1, slaveDataSourceName2);
        assertThat(roundRobinMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames), is(slaveDataSourceName1));
        assertThat(roundRobinMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames), is(slaveDataSourceName2));
        assertThat(roundRobinMasterSlaveLoadBalanceAlgorithm.getDataSource("ds", masterDataSourceName, slaveDataSourceNames), is(slaveDataSourceName1));
    }
}
