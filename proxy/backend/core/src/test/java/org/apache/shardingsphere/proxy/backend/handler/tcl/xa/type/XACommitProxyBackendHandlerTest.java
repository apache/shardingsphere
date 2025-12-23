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

import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XACommitProxyBackendHandlerTest {
    
    @Test
    void assertExecuteSuccess() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
        ResponseHeader expected = mock(ResponseHeader.class);
        when(databaseProxyConnector.execute()).thenReturn(expected);
        XACommitProxyBackendHandler handler = new XACommitProxyBackendHandler(connectionSession, databaseProxyConnector);
        assertThat(handler.execute(), is(expected));
        verify(connectionContext).clearTransactionContext();
        verify(connectionContext).clearCursorContext();
        verify(databaseProxyConnector).execute();
    }
    
    @Test
    void assertExecuteThrowsException() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
        when(databaseProxyConnector.execute()).thenThrow(SQLException.class);
        XACommitProxyBackendHandler handler = new XACommitProxyBackendHandler(connectionSession, databaseProxyConnector);
        assertThrows(SQLException.class, handler::execute);
        verify(connectionContext).clearTransactionContext();
        verify(connectionContext).clearCursorContext();
        verify(databaseProxyConnector).execute();
    }
}
