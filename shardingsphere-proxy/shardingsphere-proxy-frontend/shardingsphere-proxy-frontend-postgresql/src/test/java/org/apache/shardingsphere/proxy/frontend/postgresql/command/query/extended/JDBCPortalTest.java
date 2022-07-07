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
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JDBCPortalTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager mockContextManager;
    
    @Mock
    private JDBCDatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Mock
    private TextProtocolBackendHandler textProtocolBackendHandler;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    private JDBCPortal portal;
    
    @Before
    public void setup() throws SQLException {
        ProxyContext.init(mockContextManager);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        prepareJDBCPortal();
    }
    
    private void prepareJDBCPortal() throws SQLException {
        PostgreSQLPreparedStatement preparedStatement = mock(PostgreSQLPreparedStatement.class);
        when(preparedStatement.getSql()).thenReturn("");
        when(preparedStatement.getSqlStatement()).thenReturn(new EmptyStatement());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        portal = new JDBCPortal("", preparedStatement, Collections.emptyList(), resultFormats, backendConnection);
    }
    
    @Test
    public void assertGetName() {
        assertThat(portal.getName(), is(""));
    }
    
    @Test
    public void assertExecuteSelectStatementWithDatabaseCommunicationEngineAndReturnAllRows() throws SQLException {
        setField(portal, "databaseCommunicationEngine", databaseCommunicationEngine);
        setField(portal, "textProtocolBackendHandler", null);
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngine.next()).thenReturn(true, true, false);
        when(databaseCommunicationEngine.getQueryResponseRow()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        portal.bind();
        assertThat(portal.describe(), instanceOf(PostgreSQLRowDescriptionPacket.class));
        setField(portal, "sqlStatement", mock(SelectStatement.class));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(3));
        Iterator<PostgreSQLPacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertExecuteSelectStatementWithDatabaseCommunicationEngineAndPortalSuspended() throws SQLException {
        setField(portal, "databaseCommunicationEngine", databaseCommunicationEngine);
        setField(portal, "textProtocolBackendHandler", null);
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngine.next()).thenReturn(true, true);
        when(databaseCommunicationEngine.getQueryResponseRow()).thenReturn(
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        setField(portal, "resultFormats", Collections.singletonList(PostgreSQLValueFormat.BINARY));
        portal.bind();
        assertThat(portal.describe(), instanceOf(PostgreSQLRowDescriptionPacket.class));
        setField(portal, "sqlStatement", mock(SelectStatement.class));
        List<PostgreSQLPacket> actualPackets = portal.execute(2);
        assertThat(actualPackets.size(), is(3));
        Iterator<PostgreSQLPacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLPortalSuspendedPacket.class));
    }
    
    @Test
    public void assertExecuteUpdateWithDatabaseCommunicationEngine() throws SQLException {
        setField(portal, "databaseCommunicationEngine", databaseCommunicationEngine);
        setField(portal, "textProtocolBackendHandler", null);
        when(databaseCommunicationEngine.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(databaseCommunicationEngine.next()).thenReturn(false);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        setField(portal, "sqlStatement", mock(InsertStatement.class));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertExecuteEmptyStatementWithDatabaseCommunicationEngine() throws SQLException {
        setField(portal, "databaseCommunicationEngine", databaseCommunicationEngine);
        setField(portal, "textProtocolBackendHandler", null);
        when(databaseCommunicationEngine.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(databaseCommunicationEngine.next()).thenReturn(false);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLEmptyQueryResponsePacket.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertDescribeBeforeBind() throws SQLException {
        PostgreSQLPreparedStatement preparedStatement = mock(PostgreSQLPreparedStatement.class);
        when(preparedStatement.getSql()).thenReturn("");
        when(preparedStatement.getSqlStatement()).thenReturn(new EmptyStatement());
        new JDBCPortal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection).describe();
    }
    
    @Test
    public void assertClose() throws SQLException {
        setField(portal, "databaseCommunicationEngine", databaseCommunicationEngine);
        setField(portal, "textProtocolBackendHandler", textProtocolBackendHandler);
        portal.close();
        verify(backendConnection).unmarkResourceInUse(databaseCommunicationEngine);
        verify(textProtocolBackendHandler).close();
    }
    
    @SneakyThrows
    private void setField(final JDBCPortal portal, final String fieldName, final Object value) {
        Field field = JDBCPortal.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(portal, value);
    }
}
