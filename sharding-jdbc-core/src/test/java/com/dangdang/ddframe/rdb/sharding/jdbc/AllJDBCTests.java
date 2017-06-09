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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.ConnectionAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.DataSourceAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.invocation.JdbcMethodInvocationTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.PreparedStatementAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.ResultSetAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.ResultSetGetterAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.invocation.SetParameterMethodInvocationTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.StatementAdapterTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnectionTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.MasterSlaveDataSourceTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSourceTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.resultset.GeneratedKeysResultSetMetaDataTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.resultset.GeneratedKeysResultSetTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingPreparedStatementTableOnlyTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingPreparedStatementTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingStatementTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedGeneratedKeysResultSetTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedOperationConnectionTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedOperationDataSourceTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedOperationPreparedStatementTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedOperationResultSetTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedOperationStatementTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.UnsupportedUpdateOperationResultSetTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        UnsupportedOperationDataSourceTest.class, 
        UnsupportedOperationConnectionTest.class, 
        UnsupportedOperationStatementTest.class, 
        UnsupportedOperationPreparedStatementTest.class, 
        UnsupportedOperationResultSetTest.class, 
        UnsupportedUpdateOperationResultSetTest.class, 
        UnsupportedGeneratedKeysResultSetTest.class, 
        DataSourceAdapterTest.class, 
        ConnectionAdapterTest.class, 
        StatementAdapterTest.class, 
        PreparedStatementAdapterTest.class, 
        ResultSetAdapterTest.class, 
        ResultSetGetterAdapterTest.class,
        ShardingDataSourceTest.class, 
        MasterSlaveDataSourceTest.class, 
        ShardingConnectionTest.class, 
        ShardingStatementTest.class, 
        ShardingPreparedStatementTest.class, 
        ShardingPreparedStatementTableOnlyTest.class, 
        GeneratedKeysResultSetTest.class, 
        GeneratedKeysResultSetMetaDataTest.class, 
        JdbcMethodInvocationTest.class, 
        SetParameterMethodInvocationTest.class
    })
public class AllJDBCTests {
}
