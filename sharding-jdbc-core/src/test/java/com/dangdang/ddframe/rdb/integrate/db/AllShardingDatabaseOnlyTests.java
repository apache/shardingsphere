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

package com.dangdang.ddframe.rdb.integrate.db;

import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDatabaseOnlyForPreparedStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDatabaseOnlyForPreparedStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDatabaseOnlyForPreparedStatementWithGroupByTest;
import com.dangdang.ddframe.rdb.integrate.db.pstatement.ShardingDatabaseOnlyForPreparedStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDatabaseOnlyForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDatabaseOnlyForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.db.statement.ShardingDatabaseOnlyForStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWihGroupByTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.pstatement.ShardingTablesOnlyForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithAggregateTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.tbl.statement.ShardingTablesOnlyForStatementWithSelectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ShardingDatabaseOnlyForPreparedStatementWithGroupByTest.class,
        ShardingDatabaseOnlyForPreparedStatementWithSelectTest.class,
        ShardingDatabaseOnlyForPreparedStatementWithAggregateTest.class,
        ShardingDatabaseOnlyForPreparedStatementWithDMLTest.class,
        ShardingDatabaseOnlyForStatementWithSelectTest.class,
        ShardingDatabaseOnlyForStatementWithAggregateTest.class,
        ShardingDatabaseOnlyForStatementWithDMLTest.class,
        ShardingTablesOnlyForPStatementWithSelectTest.class,
        ShardingTablesOnlyForPStatementWithAggregateTest.class,
        ShardingTablesOnlyForPStatementWihGroupByTest.class,
        ShardingTablesOnlyForPStatementWithDMLTest.class,
        ShardingTablesOnlyForStatementWithSelectTest.class,
        ShardingTablesOnlyForStatementWithAggregateTest.class,
        ShardingTablesOnlyForStatementWithDMLTest.class
})
public class AllShardingDatabaseOnlyTests {
}
