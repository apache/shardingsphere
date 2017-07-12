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

package com.dangdang.ddframe.rdb.integrate;

import com.dangdang.ddframe.rdb.integrate.db.AllShardingDatabaseOnlyTests;
import com.dangdang.ddframe.rdb.integrate.dbtbl.AllShardingDatabaseAndTableTests;
import com.dangdang.ddframe.rdb.integrate.hint.AllHintDatabaseOnlyTests;
import com.dangdang.ddframe.rdb.integrate.masterslave.AllShardingMasterAndSlaveTests;
import com.dangdang.ddframe.rdb.integrate.nullable.ShardingForNullableWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.single.AllSingleResultSetTests;
import com.dangdang.ddframe.rdb.integrate.tbl.AllShardingTablesOnlyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllShardingDatabaseAndTableTests.class,
        AllShardingDatabaseOnlyTests.class, 
        AllShardingTablesOnlyTests.class, 
        AllHintDatabaseOnlyTests.class, 
        AllSingleResultSetTests.class,
        AllShardingMasterAndSlaveTests.class,
        ShardingForNullableWithAggregateTest.class
    })
public class AllIntegrateTests {
}
