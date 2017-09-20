/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.algorithm.common.ShardingStrategyTest;
import io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithmTest;
import io.shardingjdbc.core.api.algorithm.sharding.DatabaseShardingStrategyTest;
import io.shardingjdbc.core.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithmTest;
import io.shardingjdbc.core.api.algorithm.table.TableShardingStrategyTest;
import io.shardingjdbc.core.constant.ShardingPropertiesConstantTest;
import io.shardingjdbc.core.constant.ShardingPropertiesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ShardingDataSourceFactoryTest.class, 
        ShardingPropertiesTest.class, 
        ShardingPropertiesConstantTest.class, 
        ShardingStrategyTest.class, 
        DatabaseShardingStrategyTest.class, 
        TableShardingStrategyTest.class, 
        HintManagerTest.class, 
        MasterSlaveDataSourceFactoryTest.class, 
        RoundRobinMasterSlaveLoadBalanceAlgorithmTest.class,
        RandomMasterSlaveLoadBalanceAlgorithmTest.class
    })
public class AllApiTests {
}
