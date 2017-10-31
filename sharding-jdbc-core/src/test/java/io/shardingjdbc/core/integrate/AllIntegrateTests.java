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

package io.shardingjdbc.core.integrate;

import io.shardingjdbc.core.integrate.type.ms.MasterSlaveOnlyTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.ms.MasterSlaveOnlyTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.NullableShardingTableOnlyTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.sharding.NullableShardingTableOnlyTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseAndTableTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseAndTableTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseOnlyTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseOnlyTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingMasterSlaveTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingMasterSlaveTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingTableOnlyTest4DMLAndDDL;
import io.shardingjdbc.core.integrate.type.sharding.ShardingTableOnlyTest4DQL;
import io.shardingjdbc.core.integrate.type.sharding.hint.AllHintDatabaseOnlyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShardingDatabaseOnlyTest4DQL.class,
        ShardingDatabaseOnlyTest4DMLAndDDL.class,
        ShardingTableOnlyTest4DQL.class,
        ShardingTableOnlyTest4DMLAndDDL.class,
        ShardingMasterSlaveTest4DQL.class,
        ShardingMasterSlaveTest4DMLAndDDL.class,
        MasterSlaveOnlyTest4DQL.class,
        MasterSlaveOnlyTest4DMLAndDDL.class,
        ShardingDatabaseAndTableTest4DQL.class,
        ShardingDatabaseAndTableTest4DMLAndDDL.class,
        NullableShardingTableOnlyTest4DQL.class,
        NullableShardingTableOnlyTest4DMLAndDDL.class,
        AllHintDatabaseOnlyTests.class
    })
public class AllIntegrateTests {
}
