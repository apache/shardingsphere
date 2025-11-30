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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.provider;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class TransactionReadOnlyValueProviderTest {
    
    @BeforeEach
    void setUp() {
        when(ProxyContext.getInstance()).thenReturn(mock(ProxyContext.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertGetGlobalValue() {
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.GLOBAL, null, MySQLSystemVariable.TX_READ_ONLY), is("0"));
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.GLOBAL, null, MySQLSystemVariable.TRANSACTION_READ_ONLY), is("0"));
    }
    
    @Test
    void assertGetSessionValue() {
        ConnectionSession connectionSession = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, "MySQL"), new DefaultAttributeMap());
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.TX_READ_ONLY), is("0"));
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.TRANSACTION_READ_ONLY), is("0"));
        connectionSession.setReadOnly(true);
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.TX_READ_ONLY), is("1"));
        assertThat(new TransactionReadOnlyValueProvider().get(MySQLSystemVariableScope.SESSION, connectionSession, MySQLSystemVariable.TRANSACTION_READ_ONLY), is("1"));
    }
}
