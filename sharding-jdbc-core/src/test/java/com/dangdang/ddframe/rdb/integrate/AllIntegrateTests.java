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

import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDataBasesOnlyForPStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDataBasesOnlyForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDataBasesOnlyForPStatementWithGroupByTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDataBasesOnlyForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDataBasesOnlyForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDataBasesOnlyForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDataBasesOnlyForStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.pstatement.DynamicShardingBothForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.pstatement.DynamicShardingBothForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.statement.DynamicShardingBothForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.statement.DynamicShardingBothForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.statement.DynamicShardingBothForStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement.StaticShardingBothForPStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement.StaticShardingBothForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement.StaticShardingBothForPStatementWithGroupByTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement.StaticShardingBothForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.statement.StaticShardingBothForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.statement.StaticShardingBothForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.statement.StaticShardingBothForStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.hint.ShardingDataBasesOnlyForHintWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.hint.ShardingDataBasesOnlyWithHintForSelectTest;
import com.dangdang.ddframe.rdb.integrate.masterslave.pstatement.ShardingMasterSlaveForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.masterslave.pstatement.ShardingMasterSlaveForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.masterslave.statement.ShardingMasterSlaveForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.masterslave.statement.ShardingMasterSlaveForStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.nullable.ShardingForNullableWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWihGroupByTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithSelectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    StaticShardingBothForPStatementWithAggregateTest.class,
    StaticShardingBothForPStatementWithDMLTest.class,
    StaticShardingBothForPStatementWithGroupByTest.class,
    StaticShardingBothForPStatementWithSelectTest.class,
    StaticShardingBothForStatementWithAggregateTest.class,
    StaticShardingBothForStatementWithDMLTest.class,
    StaticShardingBothForStatementWithSelectTest.class,
    DynamicShardingBothForPStatementWithDMLTest.class,
    DynamicShardingBothForPStatementWithSelectTest.class,
    DynamicShardingBothForStatementWithAggregateTest.class,
    DynamicShardingBothForStatementWithDMLTest.class,
    DynamicShardingBothForStatementWithSelectTest.class, 
    ShardingDataBasesOnlyForPStatementWithGroupByTest.class, 
    ShardingDataBasesOnlyForPStatementWithSelectTest.class, 
    ShardingDataBasesOnlyForPStatementWithAggregateTest.class, 
    ShardingDataBasesOnlyForPStatementWithDMLTest.class, 
    ShardingDataBasesOnlyForStatementWithSelectTest.class, 
    ShardingDataBasesOnlyForStatementWithAggregateTest.class, 
    ShardingDataBasesOnlyForStatementWithDMLTest.class, 
    ShardingTablesOnlyForPStatementWithSelectTest.class, 
    ShardingTablesOnlyForPStatementWithAggregateTest.class, 
    ShardingTablesOnlyForPStatementWihGroupByTest.class, 
    ShardingTablesOnlyForPStatementWithDMLTest.class, 
    ShardingTablesOnlyForStatementWithSelectTest.class, 
    ShardingTablesOnlyForStatementWithAggregateTest.class, 
    ShardingTablesOnlyForStatementWithDMLTest.class, 
    ShardingDataBasesOnlyForHintWithDMLTest.class, 
    ShardingDataBasesOnlyWithHintForSelectTest.class, 
    ShardingForNullableWithAggregateTest.class, 
    ShardingMasterSlaveForPStatementWithDMLTest.class,
    ShardingMasterSlaveForPStatementWithSelectTest.class,
    ShardingMasterSlaveForStatementWithDMLTest.class, 
    ShardingMasterSlaveForStatementWithSelectTest.class
    })
public class AllIntegrateTests {
}
