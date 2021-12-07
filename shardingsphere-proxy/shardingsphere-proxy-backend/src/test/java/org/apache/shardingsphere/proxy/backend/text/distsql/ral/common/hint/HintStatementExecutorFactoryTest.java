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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.HintDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.hint.ClearHintStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.AddShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.AddShardingHintTableValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearShardingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ShowReadwriteSplittingHintStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ShowShardingHintStatusExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintStatusStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintTableValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ClearShardingHintStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.SetShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.ShowShardingHintStatusStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class HintStatementExecutorFactoryTest {
    
    @Mock
    private JDBCConnectionSession connectionSession;
    
    @Test
    public void assertSetReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(SetReadwriteSplittingHintStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetReadwriteSplittingHintExecutor.class));
    }
    
    @Test
    public void assertShowReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ShowReadwriteSplittingHintStatusStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ShowReadwriteSplittingHintStatusExecutor.class));
    }
    
    @Test
    public void assertClearReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ClearReadwriteSplittingHintStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearReadwriteSplittingHintExecutor.class));
    }
    
    @Test
    public void assertClearHintExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ClearHintStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearHintExecutor.class));
    }
    
    @Test
    public void assertSetShardingHintDatabaseValueExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(SetShardingHintDatabaseValueStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetShardingHintDatabaseValueExecutor.class));
    }
    
    @Test
    public void assertAddShardingHintDatabaseValueExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(AddShardingHintDatabaseValueStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(AddShardingHintDatabaseValueExecutor.class));
    }
    
    @Test
    public void assertAddShardingHintTableValueExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(AddShardingHintTableValueStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(AddShardingHintTableValueExecutor.class));
    }
    
    @Test
    public void assertShowShardingHintStatusExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ShowShardingHintStatusStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ShowShardingHintStatusExecutor.class));
    }
    
    @Test
    public void assertClearShardingValueHintExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ClearShardingHintStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearShardingHintExecutor.class));
    }
}
