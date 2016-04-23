/**
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

import com.dangdang.ddframe.rdb.integrate.db.DMLShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.SelectAggregateShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.SelectGroupByShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.SelectShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.StatementDMLShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.StatementSelectAggregateShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.db.StatementSelectShardingDataBasesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.DMLShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.SelectAggregateShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.SelectGroupByShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.SelectShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.StatementDMLShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.StatementSelectAggregateShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.StatementSelectShardingBothDataBasesAndTablesTest;
import com.dangdang.ddframe.rdb.integrate.hint.DMLShardingDataBasesOnlyHintTest;
import com.dangdang.ddframe.rdb.integrate.hint.SelectShardingDataBasesOnlyHintTest;
import com.dangdang.ddframe.rdb.integrate.nullable.SelectAggregateShardingNullableTest;
import com.dangdang.ddframe.rdb.integrate.tbl.DMLShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.SelectAggregateShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.SelectGroupByShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.SelectShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.StatementDMLShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.StatementSelectAggregateShardingTablesOnlyTest;
import com.dangdang.ddframe.rdb.integrate.tbl.StatementSelectShardingTablesOnlyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SelectShardingBothDataBasesAndTablesTest.class, 
        SelectAggregateShardingBothDataBasesAndTablesTest.class, 
        SelectGroupByShardingDataBasesOnlyTest.class,
        DMLShardingBothDataBasesAndTablesTest.class,
        StatementSelectShardingBothDataBasesAndTablesTest.class,
        StatementSelectAggregateShardingBothDataBasesAndTablesTest.class,
        StatementDMLShardingBothDataBasesAndTablesTest.class, 
        SelectShardingDataBasesOnlyTest.class, 
        SelectAggregateShardingDataBasesOnlyTest.class, 
        SelectGroupByShardingBothDataBasesAndTablesTest.class,
        DMLShardingDataBasesOnlyTest.class,
        StatementSelectShardingDataBasesOnlyTest.class,
        StatementSelectAggregateShardingDataBasesOnlyTest.class,
        StatementDMLShardingDataBasesOnlyTest.class,
        SelectShardingTablesOnlyTest.class, 
        SelectAggregateShardingTablesOnlyTest.class, 
        SelectGroupByShardingTablesOnlyTest.class, 
        DMLShardingTablesOnlyTest.class,
        StatementSelectShardingTablesOnlyTest.class,
        StatementSelectAggregateShardingTablesOnlyTest.class,
        StatementDMLShardingTablesOnlyTest.class,
        DMLShardingDataBasesOnlyHintTest.class,
        SelectShardingDataBasesOnlyHintTest.class,
        SelectAggregateShardingNullableTest.class
    })
public class AllIntegrateTests {
}
