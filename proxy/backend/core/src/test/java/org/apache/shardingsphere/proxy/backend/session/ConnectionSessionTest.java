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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.exception.SwitchTypeInTransactionException;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class ConnectionSessionTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    private ConnectionSession connectionSession;
    
    @BeforeEach
    public void setup() {
        connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, null);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
    }
    
    @Test
    public void assertSetCurrentSchema() {
        connectionSession.setCurrentDatabase("currentDatabase");
        assertThat(connectionSession.getDatabaseName(), is("currentDatabase"));
    }
    
    @Test
    public void assertFailedSwitchTransactionTypeWhileBegin() {
        connectionSession.setCurrentDatabase("db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        new BackendTransactionManager(backendConnection).begin();
        assertThrows(SwitchTypeInTransactionException.class, () -> connectionSession.getTransactionStatus().setTransactionType(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchSchemaWhileBegin() {
        connectionSession.setCurrentDatabase("db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        new BackendTransactionManager(backendConnection).begin();
        connectionSession.setCurrentDatabase("newDB");
        assertThat(connectionSession.getDefaultDatabaseName(), is("newDB"));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(mock(TransactionRule.class))));
        return result;
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
    
    @Test
    public void assertClearQueryContext() {
        connectionSession.setQueryContext(mock(QueryContext.class));
        assertNotNull(connectionSession.getQueryContext());
        connectionSession.clearQueryContext();
        assertNull(connectionSession.getQueryContext());
    }
}
