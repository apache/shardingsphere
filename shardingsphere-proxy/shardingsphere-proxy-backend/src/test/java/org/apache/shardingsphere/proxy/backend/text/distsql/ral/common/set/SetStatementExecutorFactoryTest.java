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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.SetDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetInstanceStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetReadwriteSplittingStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetVariableExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class SetStatementExecutorFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    public void assertSetVariableExecutor() throws SQLException {
        SetDistSQLStatement sqlStatement = mock(SetVariableStatement.class);
        assertThat(SetStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetVariableExecutor.class));
    }
    
    @Test
    public void assertSetReadwriteSplittingStatusExecutor() throws SQLException {
        SetDistSQLStatement sqlStatement = mock(SetReadwriteSplittingStatusStatement.class);
        assertThat(SetStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetReadwriteSplittingStatusExecutor.class));
    }
    
    @Test
    public void assertSetInstanceStatusExecutor() throws SQLException {
        SetDistSQLStatement sqlStatement = mock(SetInstanceStatusStatement.class);
        assertThat(SetStatementExecutorFactory.newInstance(sqlStatement, connectionSession), instanceOf(SetInstanceStatusExecutor.class));
    }
}
