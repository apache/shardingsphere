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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComQueryExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    private PostgreSQLComQueryExecutor queryExecutor;
    
    @BeforeEach
    void setUp() throws SQLException {
        PostgreSQLComQueryPacket queryPacket = mock(PostgreSQLComQueryPacket.class);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(queryPacket.getSQL()).thenReturn("");
        queryExecutor = new PostgreSQLComQueryExecutor(portalContext, queryPacket, connectionSession);
        setMockFieldIntoExecutor(queryExecutor);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setMockFieldIntoExecutor(final PostgreSQLComQueryExecutor executor) {
        Plugins.getMemberAccessor().set(PostgreSQLComQueryExecutor.class.getDeclaredField("proxyBackendHandler"), executor, proxyBackendHandler);
    }
    
    @Test
    void assertExecuteQueryAndReturnEmptyResult() throws SQLException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(proxyBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(isA(PostgreSQLRowDescriptionPacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
        verify(queryResponseHeader).getQueryHeaders();
    }
    
    @Test
    void assertExecuteQueryAndReturnResult() throws SQLException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(new QueryHeader("schema", "table", "label", "column", 1, "type", 2, 3, true, true, true, true)));
        when(proxyBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(isA(PostgreSQLRowDescriptionPacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
        verify(queryResponseHeader).getQueryHeaders();
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(InsertStatement.class)));
        Collection<DatabasePacket> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(isA(PostgreSQLCommandCompletePacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    void assertNext() throws SQLException {
        when(proxyBackendHandler.next()).thenReturn(true, false);
        assertTrue(queryExecutor.next());
        assertFalse(queryExecutor.next());
    }
    
    @Test
    void assertGetQueryRowPacket() throws SQLException {
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.emptyList()));
        PostgreSQLPacket actual = queryExecutor.getQueryRowPacket();
        assertThat(actual, is(isA(PostgreSQLDataRowPacket.class)));
    }
}
