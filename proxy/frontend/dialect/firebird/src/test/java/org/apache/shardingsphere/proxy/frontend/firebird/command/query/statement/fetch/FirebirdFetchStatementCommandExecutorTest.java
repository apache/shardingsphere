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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdFetchResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class FirebirdFetchStatementCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int STATEMENT_ID = 1;
    
    @Mock
    private FirebirdFetchStatementPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    private FirebirdFetchStatementCommandExecutor executor;
    
    @BeforeEach
    void setup() {
        FirebirdFetchStatementCache.getInstance().registerConnection(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(packet.getStatementId()).thenReturn(STATEMENT_ID);
    }
    
    @AfterEach
    void tearDown() {
        FirebirdFetchStatementCache.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWhenNoBackendHandler() throws SQLException {
        when(packet.getFetchSize()).thenReturn(Integer.MAX_VALUE);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        assertNoMoreRowsResponse(executor.execute());
    }
    
    @Test
    void assertExecuteWhenNoBackendHandlerAfterSameHandleReprepare() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        FirebirdFetchStatementCache.getInstance().unregisterStatement(CONNECTION_ID, STATEMENT_ID);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        assertNoMoreRowsResponse(executor.execute());
    }
    
    @Test
    void assertGetResponseType() {
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    void assertNextReturnsFalseWhenFetchCountExceeded() throws SQLException {
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        assertFalse(executor.next());
    }
    
    @Test
    void assertNextReturnsTrueWhenFetchCountNotExceeded() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(2);
        when(proxyBackendHandler.next()).thenReturn(true);
        QueryResponseRow responseRow = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.getRowData()).thenReturn(responseRow);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        executor.execute();
        assertTrue(executor.next());
    }
    
    @Test
    void assertExecuteWhenBackendHandlerReturnsRow() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(2);
        QueryResponseRow responseRow = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.next()).thenReturn(true);
        when(proxyBackendHandler.getRowData()).thenReturn(responseRow);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        FirebirdFetchResponsePacket actualPacket = (FirebirdFetchResponsePacket) actualPackets.iterator().next();
        assertThat(actualPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualPacket.getCount(), is(1));
        assertNotNull(actualPacket.getRow());
    }
    
    @Test
    void assertExecuteWhenBackendHandlerReturnsNoMoreRows() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(Integer.MAX_VALUE);
        when(proxyBackendHandler.next()).thenReturn(false);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        FirebirdFetchResponsePacket actualPacket = (FirebirdFetchResponsePacket) actualPackets.iterator().next();
        assertThat(actualPacket.getStatus(), is(ISCConstants.FETCH_NO_MORE_ROWS));
        assertThat(actualPacket.getCount(), is(0));
        assertNull(actualPacket.getRow());
        verify(databaseConnectionManager).unmarkResourceInUse(proxyBackendHandler);
    }
    
    @Test
    void assertGetQueryRowPacketWhenEnd() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(0);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        FirebirdFetchResponsePacket actualPacket = (FirebirdFetchResponsePacket) actualPackets.iterator().next();
        assertThat(actualPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualPacket.getCount(), is(0));
        assertNull(actualPacket.getRow());
    }
    
    private void assertNoMoreRowsResponse(final Collection<DatabasePacket> actualPackets) {
        Iterator<DatabasePacket> packetIterator = actualPackets.iterator();
        FirebirdFetchResponsePacket actualPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPacket.getStatus(), is(ISCConstants.FETCH_NO_MORE_ROWS));
        assertThat(actualPacket.getCount(), is(0));
        assertNull(actualPacket.getRow());
    }
}
