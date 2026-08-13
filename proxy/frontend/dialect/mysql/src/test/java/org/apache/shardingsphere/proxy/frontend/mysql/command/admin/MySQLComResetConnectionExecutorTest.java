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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.sqlmode.MySQLSessionSQLMode;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@ConstructionMockSettings(ProxyBackendTransactionManager.class)
class MySQLComResetConnectionExecutorTest {
    
    @AfterEach
    void resetGlobalSQLMode() {
        MySQLSessionSQLMode.setGlobalValue(MySQLSessionSQLMode.DEFAULT_SQL_MODE);
    }
    
    @Test
    void assertExecute() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getAttributeMap()).thenReturn(new DefaultAttributeMap());
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        int statementId = 1;
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new MySQLServerPreparedStatement("", null, new HintValueContext()));
        Collection<DatabasePacket> actual = new MySQLComResetConnectionExecutor(connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), isA(MySQLOKPacket.class));
        verify(connectionSession).setAutoCommit(true);
        verify(connectionSession).setIsolationLevel(null);
        assertNull(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId));
    }
    
    @Test
    void assertExecuteResetsSQLMode() throws SQLException {
        MySQLSessionSQLMode.setGlobalValue("STRICT_TRANS_TABLES");
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionSQLMode.set("NO_BACKSLASH_ESCAPES", attributeMap);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        RequiredSessionVariableRecorder recorder = new RequiredSessionVariableRecorder();
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(recorder);
        Collection<DatabasePacket> actual = new MySQLComResetConnectionExecutor(connectionSession).execute();
        assertThat(((MySQLOKPacket) actual.iterator().next()).getStatusFlag(), is(0));
        assertFalse(MySQLSessionSQLMode.get(attributeMap).isNoBackslashEscapes());
        assertThat(MySQLSessionSQLMode.get(attributeMap).getValue(), is("STRICT_TRANS_TABLES"));
        assertThat(recorder.toSetSQLs("MySQL"), is(Collections.singletonList("SET sql_mode='STRICT_TRANS_TABLES'")));
        verify(databaseConnectionManager).markSessionVariablesDirty();
    }
}
