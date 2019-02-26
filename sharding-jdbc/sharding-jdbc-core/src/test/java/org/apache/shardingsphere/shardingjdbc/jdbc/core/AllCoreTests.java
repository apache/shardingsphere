/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingjdbc.jdbc.core;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnectionTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSourceTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSourceTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaDataTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.DatabaseMetaDataResultSetTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.GeneratedKeysResultSetMetaDataTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.GeneratedKeysResultSetTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ResultSetUtilTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSetTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingPreparedStatementTest;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.statement.ShardingStatementTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ShardingDataSourceTest.class, 
        CachedDatabaseMetaDataTest.class, 
        MasterSlaveDataSourceTest.class, 
        ShardingConnectionTest.class, 
        ShardingStatementTest.class, 
        ShardingPreparedStatementTest.class, 
        ShardingResultSetTest.class,
        ResultSetUtilTest.class,
        GeneratedKeysResultSetTest.class, 
        GeneratedKeysResultSetMetaDataTest.class,
        DatabaseMetaDataResultSetTest.class
    })
public final class AllCoreTests {
}
