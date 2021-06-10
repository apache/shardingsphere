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

package org.apache.shardingsphere.readwritesplitting.algorithm;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public final class RandomReplicaLoadBalanceAlgorithmTest {
    
    private final RandomReplicaLoadBalanceAlgorithm randomReplicaLoadBalanceAlgorithm = new RandomReplicaLoadBalanceAlgorithm();
    
    @Test
    public void assertGetDataSource() {
        String writeDataSourceName = "test_write_ds";
        String readDataSourceName1 = "test_replica_ds_1";
        String readDataSourceName2 = "test_replica_ds_2";
        List<String> readDataSourceNames = Arrays.asList(readDataSourceName1, readDataSourceName2);
        assertTrue(readDataSourceNames.contains(randomReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
        assertTrue(readDataSourceNames.contains(randomReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
        assertTrue(readDataSourceNames.contains(randomReplicaLoadBalanceAlgorithm.getDataSource("ds", writeDataSourceName, readDataSourceNames)));
    }
}
