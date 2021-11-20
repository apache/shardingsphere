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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.impl.TextQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLPortalTest {
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Mock
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Mock
    private BackendConnection backendConnection;
    
    private PostgreSQLPortal portal;
    
    @Before
    public void setup() throws SQLException {
        PostgreSQLPreparedStatement preparedStatement = mock(PostgreSQLPreparedStatement.class);
        when(preparedStatement.getSql()).thenReturn("");
        when(preparedStatement.getSqlStatement()).thenReturn(new EmptyStatement());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        portal = new PostgreSQLPortal(preparedStatement, Collections.emptyList(), resultFormats, backendConnection);
    }
    
    @Test
    public void assertGetSqlStatement() {
        assertTrue(portal.getSqlStatement() instanceof EmptyStatement);
    }
    
    @Test
    public void assertExecuteAndGetQueryResponseHeader() throws SQLException {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        setTextProtocolBackendHandler(null);
        ResponseHeader expected = mock(QueryResponseHeader.class);
        when(databaseCommunicationEngine.execute()).thenReturn(expected);
        portal.execute();
        assertThat(getResponseHeader(), is(expected));
    }
    
    @Test
    public void assertExecuteAndGetUpdateResponseHeader() throws SQLException {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        setTextProtocolBackendHandler(null);
        ResponseHeader expected = mock(UpdateResponseHeader.class);
        when(databaseCommunicationEngine.execute()).thenReturn(expected);
        portal.execute();
        assertThat(getResponseHeader(), is(expected));
    }
    
    @Test
    public void assertDescribeWithQueryResponseHeader() {
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        setResponseHeader(responseHeader);
        assertTrue(portal.describe() instanceof PostgreSQLRowDescriptionPacket);
    }
    
    @Test
    public void assertDescribeWithUpdateResponseHeader() {
        UpdateResponseHeader responseHeader = mock(UpdateResponseHeader.class);
        setResponseHeader(responseHeader);
        assertTrue(portal.describe() instanceof PostgreSQLNoDataPacket);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDescribeWithUnknownResponseHeader() {
        portal.describe();
    }
    
    @Test
    public void assertGetUpdateCount() {
        UpdateResponseHeader expected = mock(UpdateResponseHeader.class);
        when(expected.getUpdateCount()).thenReturn(100L);
        setResponseHeader(expected);
        assertThat(portal.getUpdateCount(), is(100L));
    }
    
    @Test
    public void assertNext() throws SQLException {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        portal.next();
        verify(databaseCommunicationEngine).next();
    }
    
    @Test
    public void assertNextPacketWithDatabaseCommunicationEngine() throws SQLException {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        QueryResponseRow row = mock(QueryResponseRow.class);
        BinaryQueryResponseCell binaryQueryResponseCell = mock(BinaryQueryResponseCell.class);
        when(binaryQueryResponseCell.getJdbcType()).thenReturn(Types.INTEGER);
        when(row.getCells()).thenReturn(Arrays.asList(mock(TextQueryResponseCell.class), binaryQueryResponseCell));
        when(databaseCommunicationEngine.getQueryResponseRow()).thenReturn(row);
        assertTrue(portal.nextPacket() instanceof PostgreSQLDataRowPacket);
    }
    
    @Test
    public void assertNextPacketWithTextProtocolBackendHandler() throws SQLException {
        setDatabaseCommunicationEngine(null);   
        setTextProtocolBackendHandler(textProtocolBackendHandler);
        assertTrue(portal.nextPacket() instanceof PostgreSQLDataRowPacket);
    }
    
    @Test
    public void assertSuspend() {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        setTextProtocolBackendHandler(null);
        portal.suspend();
        verify(backendConnection).markResourceInUse(databaseCommunicationEngine);
    }
    
    @Test
    public void assertClose() throws SQLException {
        setDatabaseCommunicationEngine(databaseCommunicationEngine);
        setTextProtocolBackendHandler(textProtocolBackendHandler);
        portal.close();
        verify(backendConnection).unmarkResourceInUse(databaseCommunicationEngine);
        verify(textProtocolBackendHandler).close();
    }
    
    @SneakyThrows
    private void setDatabaseCommunicationEngine(final DatabaseCommunicationEngine databaseCommunicationEngine) {
        Field field = PostgreSQLPortal.class.getDeclaredField("databaseCommunicationEngine");
        makeAccessible(field);
        field.set(portal, databaseCommunicationEngine);
    }
    
    @SneakyThrows
    private void setTextProtocolBackendHandler(final TextProtocolBackendHandler textProtocolBackendHandler) {
        Field field = PostgreSQLPortal.class.getDeclaredField("textProtocolBackendHandler");
        makeAccessible(field);
        field.set(portal, textProtocolBackendHandler);
    }
    
    @SneakyThrows
    private void makeAccessible(final Field field) {
        field.setAccessible(true);
        Field modifiersField = getModifiersField();
        modifiersField.setAccessible(true);
        modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);
    }
    
    @SneakyThrows
    private Field getModifiersField() {
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                return each;
            }
        }
        throw new UnsupportedOperationException();
    }
    
    @SneakyThrows
    private ResponseHeader getResponseHeader() {
        Field responseHeaderField = PostgreSQLPortal.class.getDeclaredField("responseHeader");
        responseHeaderField.setAccessible(true);
        return (ResponseHeader) responseHeaderField.get(portal);
    }
    
    @SneakyThrows
    private void setResponseHeader(final ResponseHeader responseHeader) {
        Field responseHeaderField = PostgreSQLPortal.class.getDeclaredField("responseHeader");
        responseHeaderField.setAccessible(true);
        responseHeaderField.set(portal, responseHeader);
    }
}
