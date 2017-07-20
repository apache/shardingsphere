/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.integrate.tbl;

import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPreparedStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPreparedStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPreparedStatementWithGroupByTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPreparedStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithSelectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ShardingTablesOnlyForPreparedStatementWithGroupByTest.class,
    ShardingTablesOnlyForPreparedStatementWithSelectTest.class,
    ShardingTablesOnlyForPreparedStatementWithAggregateTest.class,
    ShardingTablesOnlyForPreparedStatementWithDMLTest.class,
    ShardingTablesOnlyForStatementWithAggregateTest.class,
    ShardingTablesOnlyForStatementWithDMLTest.class,
    ShardingTablesOnlyForStatementWithSelectTest.class
    })
public class AllShardingTablesOnlyTests {
}
