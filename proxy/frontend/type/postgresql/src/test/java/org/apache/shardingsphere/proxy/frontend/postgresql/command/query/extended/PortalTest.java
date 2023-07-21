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

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLEmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class PortalTest {
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setup() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ShardingSphereDatabase database = mockDatabase();
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("foo_db");
        when(ProxyBackendHandlerFactory.newInstance(any(PostgreSQLDatabaseType.class), anyString(), any(SQLStatement.class), eq(connectionSession), any(HintValueContext.class)))
                .thenReturn(proxyBackendHandler);
        when(ProxyBackendHandlerFactory.newInstance(any(PostgreSQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean())).thenReturn(proxyBackendHandler);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        return result;
    }
    
    @Test
    void assertGetName() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new UnknownSQLStatementContext(new PostgreSQLEmptyStatement()), new HintValueContext(), Collections.emptyList(), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        assertThat(portal.getName(), is(""));
    }
    
    @Test
    void assertExecuteSelectStatementAndReturnAllRows() throws SQLException {
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.VARCHAR, "columnTypeName", 0, 0, false, false, false, false);
        QueryHeader intColumnQueryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Arrays.asList(queryHeader, intColumnQueryHeader));
        when(proxyBackendHandler.execute()).thenReturn(responseHeader);
        when(proxyBackendHandler.next()).thenReturn(true, true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new PostgreSQLSelectStatement());
        PostgreSQLServerPreparedStatement preparedStatement =
                new PostgreSQLServerPreparedStatement("", sqlStatementContext, new HintValueContext(), Collections.emptyList(), Collections.emptyList());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), resultFormats, databaseConnectionManager);
        portal.bind();
        PostgreSQLPacket portalDescription = portal.describe();
        assertThat(portalDescription, instanceOf(PostgreSQLRowDescriptionPacket.class));
        Optional<Collection<PostgreSQLColumnDescription>> columnDescriptions = ReflectionUtils.getFieldValue(portalDescription, "columnDescriptions");
        assertTrue(columnDescriptions.isPresent());
        Iterator<PostgreSQLColumnDescription> columnDescriptionIterator = columnDescriptions.get().iterator();
        PostgreSQLColumnDescription textColumnDescription = columnDescriptionIterator.next();
        PostgreSQLColumnDescription intColumnDescription = columnDescriptionIterator.next();
        assertThat(textColumnDescription.getDataFormat(), is(PostgreSQLValueFormat.TEXT.getCode()));
        assertThat(intColumnDescription.getDataFormat(), is(PostgreSQLValueFormat.BINARY.getCode()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(3));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    void assertExecuteSelectStatementAndPortalSuspended() throws SQLException {
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
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("", selectStatementContext, new HintValueContext(), Collections.emptyList(),
                Collections.emptyList());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), resultFormats, databaseConnectionManager);
        portal.bind();
        assertThat(portal.describe(), instanceOf(PostgreSQLRowDescriptionPacket.class));
        List<DatabasePacket> actualPackets = portal.execute(2);
        assertThat(actualPackets.size(), is(3));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), instanceOf(PostgreSQLPortalSuspendedPacket.class));
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement()).thenReturn(new PostgreSQLInsertStatement());
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("", insertStatementContext, new HintValueContext(), Collections.emptyList(),
                Collections.emptyList());
        Portal portal = new Portal("insert into t values (1)", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    void assertExecuteEmptyStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new UnknownSQLStatementContext(new PostgreSQLEmptyStatement()), new HintValueContext(), Collections.emptyList(), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), instanceOf(PostgreSQLEmptyQueryResponsePacket.class));
    }
    
    @Test
    void assertExecuteSetStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        String sql = "set client_encoding = utf8";
        PostgreSQLSetStatement setStatement = new PostgreSQLSetStatement();
        VariableAssignSegment variableAssignSegment = new VariableAssignSegment();
        variableAssignSegment.setVariable(new VariableSegment(0, 0, "client_encoding"));
        setStatement.getVariableAssigns().add(variableAssignSegment);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(sql, new UnknownSQLStatementContext(setStatement), new HintValueContext(), Collections.emptyList(),
                Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(2));
        assertThat(actualPackets.get(0), instanceOf(PostgreSQLCommandCompletePacket.class));
        assertThat(actualPackets.get(1), instanceOf(PostgreSQLParameterStatusPacket.class));
    }
    
    @Test
    void assertDescribeBeforeBind() {
        PostgreSQLServerPreparedStatement preparedStatement = mock(PostgreSQLServerPreparedStatement.class);
        when(preparedStatement.getSqlStatementContext()).thenReturn(mock(SQLStatementContext.class));
        assertThrows(IllegalStateException.class, () -> new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager).describe());
    }
    
    @Test
    void assertClose() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new UnknownSQLStatementContext(new PostgreSQLEmptyStatement()), new HintValueContext(), Collections.emptyList(), Collections.emptyList());
        new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager).close();
        verify(databaseConnectionManager).unmarkResourceInUse(proxyBackendHandler);
        verify(proxyBackendHandler).close();
    }
}
