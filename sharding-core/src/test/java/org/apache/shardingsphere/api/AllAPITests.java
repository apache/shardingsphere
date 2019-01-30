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

package org.apache.shardingsphere.api;

import org.apache.shardingsphere.api.algorithm.common.ShardingStrategyTest;
import org.apache.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmTypeTest;
import org.apache.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithmTest;
import org.apache.shardingsphere.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithmTest;
import org.apache.shardingsphere.api.algorithm.sharding.DatabaseShardingStrategyTest;
import org.apache.shardingsphere.api.algorithm.table.TableShardingStrategyTest;
import org.apache.shardingsphere.api.config.EncryptorConfigurationTest;
import org.apache.shardingsphere.api.config.ShardingKeyGeneratorConfigurationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShardingStrategyTest.class, 
        DatabaseShardingStrategyTest.class, 
        TableShardingStrategyTest.class,
        MasterSlaveLoadBalanceAlgorithmTypeTest.class, 
        RoundRobinMasterSlaveLoadBalanceAlgorithmTest.class, 
        RandomMasterSlaveLoadBalanceAlgorithmTest.class,
        ConfigMapContextTest.class, 
        HintManagerTest.class, 
        ShardingKeyGeneratorConfigurationTest.class,
        EncryptorConfigurationTest.class
    })
public final class AllAPITests {
}
