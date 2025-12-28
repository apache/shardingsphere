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

import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ConnectionSessionTest {
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        connectionSession = new ConnectionSession(mock(), null);
        connectionSession.setGrantee(mock(Grantee.class));
        lenient().when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
    }
    
    @Test
    void assertSetCurrentSchema() {
        connectionSession.setCurrentDatabaseName("currentDatabase");
        assertThat(connectionSession.getUsedDatabaseName(), is("currentDatabase"));
    }
    
    @Test
    void assertSetCurrentSchemaWithSameName() throws ReflectiveOperationException {
        ConnectionContext contextMock = mock(ConnectionContext.class);
        getConnectionContextReference().set(contextMock);
        connectionSession.setCurrentDatabaseName("db");
        verify(contextMock, times(1)).setCurrentDatabaseName("db");
    }
    
    @Test
    void assertSetCurrentSchemaWithNull() throws ReflectiveOperationException {
        connectionSession.setCurrentDatabaseName("db");
        ConnectionContext contextMock = mock(ConnectionContext.class);
        getConnectionContextReference().set(contextMock);
        connectionSession.setCurrentDatabaseName(null);
        assertNull(connectionSession.getCurrentDatabaseName());
        verify(contextMock).setCurrentDatabaseName(null);
    }
    
    @Test
    void assertGetUsedDatabaseNameFromQueryContext() {
        connectionSession.setCurrentDatabaseName("currentDatabase");
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getUsedDatabaseNames()).thenReturn(Collections.singleton("used_db"));
        connectionSession.setQueryContext(queryContext);
        assertThat(connectionSession.getUsedDatabaseName(), is("used_db"));
    }
    
    @Test
    void assertGetUsedDatabaseNameWhenQueryContextEmpty() {
        connectionSession.setCurrentDatabaseName("currentDatabase");
        QueryContext queryContext = mock(QueryContext.class);
        when(queryContext.getUsedDatabaseNames()).thenReturn(Collections.emptySet());
        connectionSession.setQueryContext(queryContext);
        assertThat(connectionSession.getUsedDatabaseName(), is("currentDatabase"));
    }
    
    @Test
    void assertSwitchSchemaWhileBegin() {
        connectionSession.setCurrentDatabaseName("db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        new ProxyBackendTransactionManager(databaseConnectionManager).begin();
        connectionSession.setCurrentDatabaseName("newDB");
        assertThat(connectionSession.getCurrentDatabaseName(), is("newDB"));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(transactionRule)));
        return result;
    }
    
    @Test
    void assertDefaultAutoCommit() {
        assertTrue(connectionSession.isAutoCommit());
    }
    
    @Test
    void assertSetAutoCommit() {
        connectionSession.setAutoCommit(false);
        assertFalse(connectionSession.isAutoCommit());
    }
    
    @Test
    void assertGetIsolationLevel() {
        connectionSession.setIsolationLevel(TransactionIsolationLevel.READ_COMMITTED);
        assertTrue(connectionSession.getIsolationLevel().isPresent());
        assertThat(connectionSession.getIsolationLevel().get(), is(TransactionIsolationLevel.READ_COMMITTED));
    }
    
    @Test
    void assertClearQueryContext() {
        connectionSession.setQueryContext(mock(QueryContext.class));
        assertNotNull(connectionSession.getQueryContext());
        connectionSession.clearQueryContext();
        assertNull(connectionSession.getQueryContext());
    }
    
    @SuppressWarnings("unchecked")
    private AtomicReference<ConnectionContext> getConnectionContextReference() throws ReflectiveOperationException {
        return (AtomicReference<ConnectionContext>) Plugins.getMemberAccessor().get(ConnectionSession.class.getDeclaredField("connectionContext"), connectionSession);
    }
}
