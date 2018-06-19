/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.integrate;

import io.shardingsphere.core.integrate.api.AllAPIIntegrateTests;
import io.shardingsphere.core.integrate.type.ms.MasterSlaveOnlyDMLAndDDLTest;
import io.shardingsphere.core.integrate.type.sharding.ShardingDatabaseAndTableDMLAndDDLTest;
import io.shardingsphere.core.integrate.type.sharding.ShardingDatabaseOnlyDMLAndDDLTest;
import io.shardingsphere.core.integrate.type.sharding.ShardingMasterSlaveDMLAndDDLTest;
import io.shardingsphere.core.integrate.type.sharding.ShardingTableOnlyDMLAndDDLTest;
import io.shardingsphere.core.integrate.type.sharding.hint.AllHintDatabaseOnlyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShardingDatabaseOnlyDMLAndDDLTest.class, 
        ShardingTableOnlyDMLAndDDLTest.class, 
        ShardingMasterSlaveDMLAndDDLTest.class, 
        MasterSlaveOnlyDMLAndDDLTest.class, 
        ShardingDatabaseAndTableDMLAndDDLTest.class, 
        AllHintDatabaseOnlyTests.class, 
        AllAPIIntegrateTests.class
    })
public class AllIntegrateTests {
}
