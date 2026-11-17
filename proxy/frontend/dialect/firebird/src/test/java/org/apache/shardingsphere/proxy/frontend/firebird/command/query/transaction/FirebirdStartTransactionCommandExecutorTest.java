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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.ConstructionMockSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@ConstructionMockSettings(ProxyBackendTransactionManager.class)
class FirebirdStartTransactionCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    @Mock
    private FirebirdStartTransactionPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private TransactionRule transactionRule;
    
    @BeforeEach
    void setup() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "Firebird"));
        when(connectionSession.getDatabaseConnectionManager().getCloseLock()).thenReturn(new Object());
        when(packet.getIsolationLevel()).thenReturn(TransactionIsolationLevel.SERIALIZABLE);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecute() {
        when(packet.isAutoCommit()).thenReturn(true);
        when(packet.isReadOnly()).thenReturn(true);
        Collection<DatabasePacket> actual = new FirebirdStartTransactionCommandExecutor(packet, connectionSession).execute();
        int actualHandle = ((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle();
        assertThat(actualHandle, is(1));
        ConnectionSession actualSession = FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, actualHandle);
        assertTrue(actualSession.isAutoCommit());
        assertTrue(actualSession.isReadOnly());
        assertThat(actualSession.getIsolationLevel().get(), is(TransactionIsolationLevel.SERIALIZABLE));
    }
    
    @Test
    void assertExecuteWithActiveTransaction() {
        mockLocalTransactionRule();
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        Collection<DatabasePacket> actual = new FirebirdStartTransactionCommandExecutor(packet, connectionSession).execute();
        int actualHandle = ((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle();
        assertThat(actualHandle, is(firstTransactionId + 1));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, firstTransactionId));
        assertThat(FirebirdTransactionIdGenerator.getInstance().getTransactionSession(CONNECTION_ID, actualHandle), not(sameInstance(connectionSession)));
    }
    
    private void mockLocalTransactionRule() {
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(transactionRule).when(ruleMetaData).getSingleRule(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(connectionSession.getDatabaseConnectionManager().getCachedConnections().keySet()).thenReturn(Collections.emptySet());
    }
    
    @Test
    void assertExecuteAfterClosedTransaction() {
        mockLocalTransactionRule();
        when(connectionSession.getDatabaseConnectionManager().closeAllResources()).thenReturn(java.util.Collections.emptyList());
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession);
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(CONNECTION_ID, firstTransactionId);
        Collection<DatabasePacket> actual = new FirebirdStartTransactionCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(firstTransactionId + 1));
    }
}
