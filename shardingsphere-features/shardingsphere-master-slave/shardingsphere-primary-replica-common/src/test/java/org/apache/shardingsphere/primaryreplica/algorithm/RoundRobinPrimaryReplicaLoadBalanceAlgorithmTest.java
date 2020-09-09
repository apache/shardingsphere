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

package org.apache.shardingsphere.primaryreplica.algorithm;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RoundRobinPrimaryReplicaLoadBalanceAlgorithmTest {
    
    private final RoundRobinPrimaryReplicaLoadBalanceAlgorithm roundRobinPrimaryReplicaLoadBalanceAlgorithm = new RoundRobinPrimaryReplicaLoadBalanceAlgorithm();
    
    @Test
    public void assertGetDataSource() {
        String primaryDataSourceName = "test_ds_primary";
        String replicaDataSourceName1 = "test_ds_replica_1";
        String replicaDataSourceName2 = "test_ds_replica_2";
        List<String> replicaDataSourceNames = Arrays.asList(replicaDataSourceName1, replicaDataSourceName2);
        assertThat(roundRobinPrimaryReplicaLoadBalanceAlgorithm.getDataSource("ds", primaryDataSourceName, replicaDataSourceNames), is(replicaDataSourceName1));
        assertThat(roundRobinPrimaryReplicaLoadBalanceAlgorithm.getDataSource("ds", primaryDataSourceName, replicaDataSourceNames), is(replicaDataSourceName2));
        assertThat(roundRobinPrimaryReplicaLoadBalanceAlgorithm.getDataSource("ds", primaryDataSourceName, replicaDataSourceNames), is(replicaDataSourceName1));
    }
}
