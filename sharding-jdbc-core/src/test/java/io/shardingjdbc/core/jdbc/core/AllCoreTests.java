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

package io.shardingjdbc.core.jdbc.core;

import io.shardingjdbc.core.jdbc.core.connection.ShardingConnectionTest;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSourceTest;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSourceTest;
import io.shardingjdbc.core.jdbc.core.resultset.GeneratedKeysResultSetMetaDataTest;
import io.shardingjdbc.core.jdbc.core.resultset.GeneratedKeysResultSetTest;
import io.shardingjdbc.core.jdbc.core.resultset.ShardingResultSetTest;
import io.shardingjdbc.core.jdbc.core.statement.ShardingPreparedStatementTest;
import io.shardingjdbc.core.jdbc.core.statement.ShardingStatementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ShardingDataSourceTest.class, 
        MasterSlaveDataSourceTest.class, 
        ShardingConnectionTest.class, 
        ShardingStatementTest.class, 
        ShardingPreparedStatementTest.class, 
        ShardingResultSetTest.class, 
        GeneratedKeysResultSetTest.class, 
        GeneratedKeysResultSetMetaDataTest.class 
    })
public class AllCoreTests {
}
