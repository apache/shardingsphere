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

package org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class XABeginProxyBackendHandlerTest {
    
    @Test
    void assertExecuteStartsTransaction() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        TransactionConnectionContext transactionContext = new TransactionConnectionContext();
        when(connectionSession.getConnectionContext().getTransactionContext()).thenReturn(transactionContext);
        DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
        ResponseHeader expected = mock(ResponseHeader.class);
        when(databaseProxyConnector.execute()).thenReturn(expected);
        TransactionRule transactionRule = mock(TransactionRule.class, RETURNS_DEEP_STUBS);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.XA);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(transactionRule)));
        XABeginProxyBackendHandler handler = new XABeginProxyBackendHandler(metaData, connectionSession, databaseProxyConnector);
        assertThat(handler.execute(), is(expected));
        assertTrue(transactionContext.isTransactionStarted());
        assertThat(transactionContext.getTransactionType().orElse(""), is(TransactionType.XA.name()));
    }
}
