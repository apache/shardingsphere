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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ClearHintExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.SetReadwriteSplittingHintSourceExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.executor.ShowReadwriteSplittingHintSourceExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ClearReadwriteSplittingHintSourceStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.SetReadwriteSplittingHintSourceStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.hint.ShowReadwriteSplittingHintSourceStatement;
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
    private BackendConnection backendConnection;
    
    @Test
    public void assertSetReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(SetReadwriteSplittingHintSourceStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(SetReadwriteSplittingHintSourceExecutor.class));
    }
    
    @Test
    public void assertShowReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ShowReadwriteSplittingHintSourceStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(ShowReadwriteSplittingHintSourceStatement.class));
    }
    
    @Test
    public void assertClearReadwriteSplittingHintSourceExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ClearReadwriteSplittingHintSourceStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(ClearReadwriteSplittingHintSourceStatement.class));
    }
    
    @Test
    public void assertHintSetDatabaseShardingValueExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ShowReadwriteSplittingHintSourceStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(ShowReadwriteSplittingHintSourceExecutor.class));
    }
    
    @Test
    public void assertHintAddDatabaseShardingValueExecutor() throws SQLException {
        HintDistSQLStatement sqlStatement = mock(ClearHintStatement.class);
        assertThat(HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(ClearHintExecutor.class));
    }
}
