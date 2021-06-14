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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.bind;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.binary.BinaryCell;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComBindExecutorTest {
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private PostgreSQLComDescribeExecutor describeExecutor;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setup() {
        when(bindPacket.getSql()).thenReturn("");
    }
    
    @Test
    public void assertExecuteEmptyBindPacket() throws SQLException {
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertFalse(executor.next());
    }
    
    @Test
    public void assertExecuteBindPacketWithQuerySQLAndReturnEmptyResult() throws SQLException {
        when(connectionContext.getDescribeExecutor()).thenReturn(Optional.of(describeExecutor));
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(databaseCommunicationEngine.execute()).thenReturn(queryResponseHeader);
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        verify(queryResponseHeader).getQueryHeaders();
    }
    
    @Test
    public void assertExecuteBindPacketWithQuerySQL() throws SQLException {
        when(connectionContext.getDescribeExecutor()).thenReturn(Optional.of(describeExecutor));
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(new QueryHeader("schema", "table", "label", "column", 1, "type", 2, 3, true, true, true, true)));
        when(databaseCommunicationEngine.execute()).thenReturn(queryResponseHeader);
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        Iterator<DatabasePacket<?>> actualPackets = actual.iterator();
        assertThat(actualPackets.next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    public void assertNext() throws SQLException {
        when(databaseCommunicationEngine.next()).thenReturn(true, false);
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        assertTrue(executor.next());
        assertFalse(executor.next());
    }
    
    @Test
    public void assertDataRowNotBinary() throws SQLException {
        QueryResponseRow queryResponseRow = mock(QueryResponseRow.class);
        when(databaseCommunicationEngine.getQueryResponseRow()).thenReturn(queryResponseRow);
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        PostgreSQLPacket actualQueryRowPacket = executor.getQueryRowPacket();
        verify(queryResponseRow).getCells();
        assertThat(actualQueryRowPacket, is(instanceOf(PostgreSQLDataRowPacket.class)));
    }
    
    @Test
    public void assertDataRowIsBinary() throws SQLException {
        when(bindPacket.getResultFormatByColumnIndex(0)).thenReturn(PostgreSQLColumnFormat.BINARY);
        QueryResponseRow queryResponseRow = mock(QueryResponseRow.class);
        when(queryResponseRow.getCells()).thenReturn(Collections.singletonList(new BinaryQueryResponseCell(JDBCType.BIGINT.getVendorTypeNumber(), Long.MAX_VALUE)));
        when(databaseCommunicationEngine.getQueryResponseRow()).thenReturn(queryResponseRow);
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        PostgreSQLPacket actualQueryRowPacket = executor.getQueryRowPacket();
        assertThat(actualQueryRowPacket, is(instanceOf(PostgreSQLDataRowPacket.class)));
        Collection<Object> actualData = ((PostgreSQLDataRowPacket) actualQueryRowPacket).getData();
        assertThat(actualData.iterator().next(), instanceOf(BinaryCell.class));
    }
    
    @Test
    public void assertExecuteBindPacketWithUpdateSQL() throws SQLException {
        when(databaseCommunicationEngine.execute()).thenReturn(new UpdateResponseHeader(mock(InsertStatement.class)));
        PostgreSQLComBindExecutor executor = new PostgreSQLComBindExecutor(connectionContext, bindPacket, backendConnection);
        setMockFieldIntoExecutor(executor);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(instanceOf(PostgreSQLBindCompletePacket.class)));
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @SneakyThrows
    private void setMockFieldIntoExecutor(final PostgreSQLComBindExecutor executor) {
        Field field = PostgreSQLComBindExecutor.class.getDeclaredField("databaseCommunicationEngine");
        field.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(executor, databaseCommunicationEngine);
    }
}
