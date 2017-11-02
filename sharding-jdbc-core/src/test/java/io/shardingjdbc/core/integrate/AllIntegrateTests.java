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

import io.shardingjdbc.core.integrate.type.ms.MasterSlaveOnly4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.ms.MasterSlaveOnly4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.NullableShardingTableOnly4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.sharding.NullableShardingTableOnly4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseAndTable4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseAndTable4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseOnly4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingDatabaseOnly4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingMasterSlave4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingMasterSlave4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingTableOnly4DMLAndDDLTest;
import io.shardingjdbc.core.integrate.type.sharding.ShardingTableOnly4DQLTest;
import io.shardingjdbc.core.integrate.type.sharding.hint.AllHintDatabaseOnlyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShardingDatabaseOnly4DQLTest.class,
        ShardingDatabaseOnly4DMLAndDDLTest.class,
        ShardingTableOnly4DQLTest.class,
        ShardingTableOnly4DMLAndDDLTest.class,
        ShardingMasterSlave4DQLTest.class,
        ShardingMasterSlave4DMLAndDDLTest.class,
        MasterSlaveOnly4DQLTest.class,
        MasterSlaveOnly4DMLAndDDLTest.class,
        ShardingDatabaseAndTable4DQLTest.class,
        ShardingDatabaseAndTable4DMLAndDDLTest.class,
        NullableShardingTableOnly4DQLTest.class,
        NullableShardingTableOnly4DMLAndDDLTest.class,
        AllHintDatabaseOnlyTests.class
    })
public class AllIntegrateTests {
}
