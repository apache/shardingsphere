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

package org.apache.shardingsphere.proxy.backend.util;

import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionManager;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class TransactionUtilsTest {
    
    @Mock
    private TransactionManager transactionManager;
    
    @Test
    void assertGetTransactionIsolationLevelByEnum() {
        assertThat(TransactionUtils.getTransactionIsolationLevel(TransactionIsolationLevel.READ_UNCOMMITTED), is(Connection.TRANSACTION_READ_UNCOMMITTED));
        assertThat(TransactionUtils.getTransactionIsolationLevel(TransactionIsolationLevel.READ_COMMITTED), is(Connection.TRANSACTION_READ_COMMITTED));
        assertThat(TransactionUtils.getTransactionIsolationLevel(TransactionIsolationLevel.REPEATABLE_READ), is(Connection.TRANSACTION_REPEATABLE_READ));
        assertThat(TransactionUtils.getTransactionIsolationLevel(TransactionIsolationLevel.SERIALIZABLE), is(Connection.TRANSACTION_SERIALIZABLE));
        assertThat(TransactionUtils.getTransactionIsolationLevel(TransactionIsolationLevel.NONE), is(Connection.TRANSACTION_NONE));
    }
    
    @Test
    void assertGetTransactionIsolationLevelByInt() {
        assertThat(TransactionUtils.getTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED), is(TransactionIsolationLevel.READ_UNCOMMITTED));
        assertThat(TransactionUtils.getTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED), is(TransactionIsolationLevel.READ_COMMITTED));
        assertThat(TransactionUtils.getTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ), is(TransactionIsolationLevel.REPEATABLE_READ));
        assertThat(TransactionUtils.getTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE), is(TransactionIsolationLevel.SERIALIZABLE));
        assertThat(TransactionUtils.getTransactionIsolationLevel(-1), is(TransactionIsolationLevel.NONE));
    }
    
    @Test
    void assertGetTransactionTypeFromContext() {
        TransactionConnectionContext transactionContext = new TransactionConnectionContext();
        transactionContext.beginTransaction(TransactionType.XA.name(), transactionManager);
        assertThat(TransactionUtils.getTransactionType(transactionContext), is(TransactionType.XA));
    }
    
    @Test
    void assertGetTransactionTypeFromDefaultRule() {
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.BASE);
        RuleMetaData globalRuleMetaData = mock(RuleMetaData.class);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(transactionRule);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThat(TransactionUtils.getTransactionType(new TransactionConnectionContext()), is(TransactionType.BASE));
    }
}
