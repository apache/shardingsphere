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

package org.apache.shardingsphere.proxy.backend.session;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConnectionSessionTest {
    
    private static ContextManager contextManagerBackup;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    private ConnectionSession connectionSession;
    
    @BeforeClass
    public static void setupProxyContext() {
        contextManagerBackup = ProxyContext.getInstance().getContextManager();
    }
    
    @Before
    public void setup() {
        ProxyContext.getInstance().init(contextManager);
        connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, null);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
    }
    
    @Test
    public void assertSetCurrentSchema() {
        connectionSession.setCurrentSchema("currentSchema");
        assertThat(connectionSession.getSchemaName(), is("currentSchema"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchTransactionTypeWhileBegin() throws SQLException {
        connectionSession.setCurrentSchema("schema");
        JDBCBackendTransactionManager transactionManager = new JDBCBackendTransactionManager(backendConnection);
        transactionManager.begin();
        connectionSession.getTransactionStatus().setTransactionType(TransactionType.XA);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertFailedSwitchSchemaWhileBegin() throws SQLException {
        connectionSession.setCurrentSchema("schema");
        JDBCBackendTransactionManager transactionManager = new JDBCBackendTransactionManager(backendConnection);
        transactionManager.begin();
        connectionSession.setCurrentSchema("newSchema");
    }
    
    @Test
    public void assertDefaultAutocommit() {
        assertTrue(connectionSession.isAutoCommit());
    }
    
    @Test
    public void assertSetAutocommit() {
        connectionSession.setAutoCommit(false);
        assertFalse(connectionSession.isAutoCommit());
    }
    
    @AfterClass
    public static void restoreContextManager() {
        ProxyContext.getInstance().init(contextManagerBackup);
    }
}
