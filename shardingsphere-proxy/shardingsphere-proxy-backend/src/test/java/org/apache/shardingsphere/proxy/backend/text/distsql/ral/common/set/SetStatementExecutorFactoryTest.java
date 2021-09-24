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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.status.SetStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.variable.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetVariableExecutor;
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
    private BackendConnection backendConnection;
    
    @Test
    public void assertSetVariableExecutor() throws SQLException {
        SetDistSQLStatement sqlStatement = mock(SetVariableStatement.class);
        assertThat(SetStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(SetVariableExecutor.class));
    }
    
    @Test
    public void assertSetStatusExecutor() throws SQLException {
        SetDistSQLStatement sqlStatement = mock(SetStatusStatement.class);
        assertThat(SetStatementExecutorFactory.newInstance(sqlStatement, backendConnection), instanceOf(SetStatusExecutor.class));
    }
}
