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
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> packetIterator = actualPackets.iterator();
        FirebirdFetchResponsePacket actualPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPacket.getStatus(), is(ISCConstants.FETCH_NO_MORE_ROWS));
        assertThat(actualPacket.getCount(), is(0));
        assertNull(actualPacket.getRow());
    }
    
    @Test
    void assertExecuteWithRowsAndFetchEnd() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(2);
        QueryResponseRow responseRow = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.next()).thenReturn(true, true);
        when(proxyBackendHandler.getRowData()).thenReturn(responseRow, responseRow);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> packetIterator = actualPackets.iterator();
        FirebirdFetchResponsePacket actualFirstPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualPackets.size(), is(3));
        assertThat(actualFirstPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualFirstPacket.getCount(), is(1));
        assertNotNull(actualFirstPacket.getRow());
        FirebirdFetchResponsePacket actualSecondPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualSecondPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualSecondPacket.getCount(), is(1));
        assertNotNull(actualSecondPacket.getRow());
        FirebirdFetchResponsePacket actualEndPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualEndPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualEndPacket.getCount(), is(0));
        assertNull(actualEndPacket.getRow());
    }
    
    @Test
    void assertExecuteStopsWhenRowsExhausted() throws SQLException {
        FirebirdFetchStatementCache.getInstance().registerStatement(CONNECTION_ID, STATEMENT_ID, proxyBackendHandler);
        when(packet.getFetchSize()).thenReturn(2);
        QueryResponseRow responseRow = new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1)));
        when(proxyBackendHandler.next()).thenReturn(true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(responseRow);
        executor = new FirebirdFetchStatementCommandExecutor(packet, connectionSession);
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> packetIterator = actualPackets.iterator();
        FirebirdFetchResponsePacket actualRowPacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualPackets.size(), is(2));
        assertThat(actualRowPacket.getStatus(), is(ISCConstants.FETCH_OK));
        assertThat(actualRowPacket.getCount(), is(1));
        assertNotNull(actualRowPacket.getRow());
        FirebirdFetchResponsePacket actualNoMorePacket = (FirebirdFetchResponsePacket) packetIterator.next();
        assertThat(actualNoMorePacket.getStatus(), is(ISCConstants.FETCH_NO_MORE_ROWS));
        assertThat(actualNoMorePacket.getCount(), is(0));
        assertNull(actualNoMorePacket.getRow());
        verify(databaseConnectionManager).unmarkResourceInUse(proxyBackendHandler);
    }
}
