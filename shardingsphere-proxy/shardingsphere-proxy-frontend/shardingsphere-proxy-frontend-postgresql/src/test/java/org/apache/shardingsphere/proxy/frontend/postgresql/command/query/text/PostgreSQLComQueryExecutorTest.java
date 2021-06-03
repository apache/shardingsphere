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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComQueryExecutorTest {
    
    @Mock
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    private PostgreSQLComQueryExecutor queryExecutor;
    
    @Before
    public void setUp() throws SQLException {
        PostgreSQLComQueryPacket queryPacket = mock(PostgreSQLComQueryPacket.class);
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(queryPacket.getSql()).thenReturn("");
        queryExecutor = new PostgreSQLComQueryExecutor(queryPacket, backendConnection);
        setMockFieldIntoExecutor(queryExecutor);
    }
    
    @SneakyThrows
    private void setMockFieldIntoExecutor(final PostgreSQLComQueryExecutor executor) {
        Field field = PostgreSQLComQueryExecutor.class.getDeclaredField("textProtocolBackendHandler");
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(executor, textProtocolBackendHandler);
    }
    
    @Test
    public void assertExecuteQueryAndReturnEmptyResult() throws SQLException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(textProtocolBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket<?>> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLRowDescriptionPacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
        verify(queryResponseHeader).getQueryHeaders();
    }
    
    @Test
    public void assertExecuteQueryAndReturnResult() throws SQLException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(new QueryHeader("schema", "table", "label", "column", 1, "type", 2, 3, true, true, true, true)));
        when(textProtocolBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket<?>> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLRowDescriptionPacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
        verify(queryResponseHeader).getQueryHeaders();
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        when(textProtocolBackendHandler.execute()).thenReturn(new UpdateResponseHeader(mock(InsertStatement.class)));
        Collection<DatabasePacket<?>> actual = queryExecutor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLCommandCompletePacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    public void assertNext() throws SQLException {
        when(textProtocolBackendHandler.next()).thenReturn(true, false);
        assertTrue(queryExecutor.next());
        assertFalse(queryExecutor.next());
    }
    
    @Test
    public void assertGetQueryRowPacket() throws SQLException {
        PostgreSQLPacket actual = queryExecutor.getQueryRowPacket();
        assertThat(actual, is(instanceOf(PostgreSQLDataRowPacket.class)));
    }
}
