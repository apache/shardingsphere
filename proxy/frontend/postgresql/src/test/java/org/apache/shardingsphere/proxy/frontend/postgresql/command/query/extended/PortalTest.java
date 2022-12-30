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

import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLEmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PortalTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager mockContextManager;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private BackendConnection backendConnection;
    
    private MockedStatic<ProxyBackendHandlerFactory> mockedStatic;
    
    @Before
    public void setup() {
        ProxyContext.init(mockContextManager);
        when(mockContextManager.getMetaDataContexts().getMetaData().containsDatabase("db")).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        when(ProxyContext.getInstance().getDatabase("db")).thenReturn(database);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        mockedStatic = mockStatic(ProxyBackendHandlerFactory.class);
        mockedStatic.when(() -> ProxyBackendHandlerFactory.newInstance(any(PostgreSQLDatabaseType.class), anyString(), any(SQLStatement.class), eq(connectionSession), any(HintValueContext.class)))
                .thenReturn(proxyBackendHandler);
        mockedStatic.when(() -> ProxyBackendHandlerFactory.newInstance(any(PostgreSQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean()))
                .thenReturn(proxyBackendHandler);
    }
    
    @After
    public void tearDown() {
        mockedStatic.close();
    }
    
    @Test
    public void assertGetName() throws SQLException {
        Portal portal = new Portal("", new PostgreSQLServerPreparedStatement("",
                new CommonSQLStatementContext<>(new PostgreSQLEmptyStatement()), Collections.emptyList()), Collections.emptyList(), Collections.emptyList(), backendConnection);
        assertThat(portal.getName(), is(""));
    }
    
    @Test
    public void assertExecuteSelectStatementAndReturnAllRows() throws SQLException {
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        when(proxyBackendHandler.execute()).thenReturn(responseHeader);
        when(proxyBackendHandler.next()).thenReturn(true, true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new PostgreSQLSelectStatement());
        PostgreSQLServerPreparedStatement preparedStatement =
                new PostgreSQLServerPreparedStatement("", sqlStatementContext, Collections.emptyList());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), resultFormats, backendConnection);
        portal.bind();
        assertThat(portal.describe(), instanceOf(PostgreSQLRowDescriptionPacket.class));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(3));
        Iterator<PostgreSQLPacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertExecuteSelectStatementAndPortalSuspended() throws SQLException {
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        when(proxyBackendHandler.execute()).thenReturn(responseHeader);
        when(proxyBackendHandler.next()).thenReturn(true, true);
        when(proxyBackendHandler.getRowData()).thenReturn(
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getSqlStatement()).thenReturn(new PostgreSQLSelectStatement());
        PostgreSQLServerPreparedStatement preparedStatement =
                new PostgreSQLServerPreparedStatement("", selectStatementContext, Collections.emptyList());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), resultFormats, backendConnection);
        portal.bind();
        assertThat(portal.describe(), instanceOf(PostgreSQLRowDescriptionPacket.class));
        List<PostgreSQLPacket> actualPackets = portal.execute(2);
        assertThat(actualPackets.size(), is(3));
        Iterator<PostgreSQLPacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLPortalSuspendedPacket.class));
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement()).thenReturn(new PostgreSQLInsertStatement());
        PostgreSQLServerPreparedStatement preparedStatement =
                new PostgreSQLServerPreparedStatement("", insertStatementContext, Collections.emptyList());
        Portal portal = new Portal("insert into t values (1)", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertExecuteEmptyStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("", new CommonSQLStatementContext<>(new PostgreSQLEmptyStatement()), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLEmptyQueryResponsePacket.class));
    }
    
    @Test
    public void assertExecuteSetStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        String sql = "set client_encoding = utf8";
        PostgreSQLSetStatement setStatement = new PostgreSQLSetStatement();
        VariableAssignSegment variableAssignSegment = new VariableAssignSegment();
        variableAssignSegment.setVariable(new VariableSegment());
        setStatement.getVariableAssigns().add(variableAssignSegment);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(sql, new CommonSQLStatementContext<>(setStatement), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection);
        portal.bind();
        List<PostgreSQLPacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(2));
        assertThat(actualPackets.get(0), instanceOf(PostgreSQLCommandCompletePacket.class));
        assertThat(actualPackets.get(1), instanceOf(PostgreSQLParameterStatusPacket.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void assertDescribeBeforeBind() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = mock(PostgreSQLServerPreparedStatement.class);
        when(preparedStatement.getSql()).thenReturn("");
        when(preparedStatement.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
        new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection).describe();
    }
    
    @Test
    public void assertClose() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("", new CommonSQLStatementContext<>(new PostgreSQLEmptyStatement()), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), backendConnection);
        portal.close();
        verify(backendConnection).unmarkResourceInUse(proxyBackendHandler);
        verify(proxyBackendHandler).close();
    }
}
