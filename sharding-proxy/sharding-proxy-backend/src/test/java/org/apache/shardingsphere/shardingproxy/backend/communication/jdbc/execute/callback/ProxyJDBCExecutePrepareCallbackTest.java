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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback;

import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxyJDBCExecutePrepareCallbackTest {
    
    @Test
    public void assertGetConnections() throws SQLException {
        BackendConnection backendConnection = mock(BackendConnection.class);
        List<Connection> connections = Collections.emptyList();
        when(backendConnection.getConnections((ConnectionMode) any(), anyString(), anyInt())).thenReturn(connections);
        ProxyJDBCExecutePrepareCallback proxyJDBCExecutePrepareCallback = new ProxyJDBCExecutePrepareCallback(backendConnection, mock(JDBCExecutorWrapper.class), false);
        assertThat(proxyJDBCExecutePrepareCallback.getConnections(null, null, 1), is(connections));
    }
    
    @Test
    public void assertCreateStatementExecuteUnitWhenNotMemoryStrictly() throws SQLException {
        JDBCExecutorWrapper jdbcExecutorWrapper = mock(JDBCExecutorWrapper.class);
        when(jdbcExecutorWrapper.createStatement((Connection) any(), (SQLUnit) any(), anyBoolean())).thenReturn(mock(Statement.class));
        ProxyJDBCExecutePrepareCallback proxyJDBCExecutePrepareCallback = new ProxyJDBCExecutePrepareCallback(mock(BackendConnection.class), jdbcExecutorWrapper, false);
        assertThat(proxyJDBCExecutePrepareCallback.createStatementExecuteUnit(
                null, new ExecutionUnit("ds", new SQLUnit("SELECT 1", Collections.emptyList())), ConnectionMode.CONNECTION_STRICTLY), instanceOf(StatementExecuteUnit.class));
    }
    
    @Test
    public void assertCreateStatementExecuteUnitWhenMemoryStrictly() throws SQLException {
        JDBCExecutorWrapper jdbcExecutorWrapper = mock(JDBCExecutorWrapper.class);
        when(jdbcExecutorWrapper.createStatement((Connection) any(), (SQLUnit) any(), anyBoolean())).thenReturn(mock(Statement.class));
        ProxyJDBCExecutePrepareCallback proxyJDBCExecutePrepareCallback = new ProxyJDBCExecutePrepareCallback(mock(BackendConnection.class), jdbcExecutorWrapper, false);
        assertThat(proxyJDBCExecutePrepareCallback.createStatementExecuteUnit(
                null, new ExecutionUnit("ds", new SQLUnit("SELECT 1", Collections.emptyList())), ConnectionMode.MEMORY_STRICTLY), instanceOf(StatementExecuteUnit.class));
    }
}
