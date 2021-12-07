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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import io.netty.util.DefaultAttributeMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCConnectionSession;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TransactionBackendHandlerTest {
    
    private final JDBCConnectionSession connectionSession = new JDBCConnectionSession(TransactionType.LOCAL, new DefaultAttributeMap());
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setTransactionContexts() {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TransactionContexts transactionContexts = mock(TransactionContexts.class, RETURNS_DEEP_STUBS);
        when(contextManager.getTransactionContexts()).thenReturn(transactionContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    @Test
    public void assertExecute() throws SQLException {
        TransactionBackendHandler transactionBackendHandler = new TransactionBackendHandler(mock(TCLStatement.class), TransactionOperationType.BEGIN, connectionSession);
        ResponseHeader actual = transactionBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
}
