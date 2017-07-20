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
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ShardingDatabaseOnlyForPreparedStatementWithGroupByTest.class,
    ShardingDatabaseOnlyForPreparedStatementWithSelectTest.class,
    ShardingDatabaseOnlyForPreparedStatementWithAggregateTest.class,
    ShardingDatabaseOnlyForPreparedStatementWithDMLTest.class,
    ShardingDatabaseOnlyForStatementWithAggregateTest.class,
    ShardingDatabaseOnlyForStatementWithDMLTest.class,
    ShardingDatabaseOnlyForStatementWithSelectTest.class
    })
public class AllShardingDatabaseOnlyTests {
}
