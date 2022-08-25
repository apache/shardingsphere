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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint;

import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.hint.ClearHintStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.AddShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.AddShardingHintTableValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ClearShardingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.SetReadwriteSplittingHintExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.SetShardingHintDatabaseValueExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ShowReadwriteSplittingHintStatusExecutor;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.executor.ShowShardingHintStatusExecutor;
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
public final class HintRALStatementExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    public void assertSetReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(SetReadwriteSplittingHintStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetReadwriteSplittingHintExecutor.class));
    }
    
    @Test
    public void assertShowReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(ShowReadwriteSplittingHintStatusStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ShowReadwriteSplittingHintStatusExecutor.class));
    }
    
    @Test
    public void assertClearReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(ClearReadwriteSplittingHintStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearReadwriteSplittingHintExecutor.class));
    }
    
    @Test
    public void assertClearHintExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(ClearHintStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearHintExecutor.class));
    }
    
    @Test
    public void assertSetShardingHintDatabaseValueExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(SetShardingHintDatabaseValueStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetShardingHintDatabaseValueExecutor.class));
    }
    
    @Test
    public void assertAddShardingHintDatabaseValueExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(AddShardingHintDatabaseValueStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(AddShardingHintDatabaseValueExecutor.class));
    }
    
    @Test
    public void assertAddShardingHintTableValueExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(AddShardingHintTableValueStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(AddShardingHintTableValueExecutor.class));
    }
    
    @Test
    public void assertShowShardingHintStatusExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(ShowShardingHintStatusStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ShowShardingHintStatusExecutor.class));
    }
    
    @Test
    public void assertClearShardingValueHintExecutor() throws SQLException {
        HintRALStatement sqlStatement = mock(ClearShardingHintStatement.class);
        assertThat(HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(ClearShardingHintExecutor.class));
    }
}
