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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setup() throws SQLException {
        ShardingSphereDatabase database = mockDatabase();
        ContextManager contextManager = mockContextManager(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), anyString(), any(SQLStatement.class), eq(connectionSession), any(HintValueContext.class))).thenReturn(proxyBackendHandler);
        when(ProxyBackendHandlerFactory.newInstance(eq(databaseType), any(QueryContext.class), eq(connectionSession), anyBoolean())).thenReturn(proxyBackendHandler);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database) {
        ContextManager result = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getProtocolType()).thenReturn(databaseType);
        return result;
    }
    
    @Test
    void assertGetName() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new CommonSQLStatementContext(new EmptyStatement(databaseType)), new HintValueContext(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        assertThat(portal.getName(), is(""));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertExecuteSelectStatementAndReturnAllRows() throws SQLException, ReflectiveOperationException {
        QueryResponseHeader responseHeader = mock(QueryResponseHeader.class);
        QueryHeader queryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.VARCHAR, "columnTypeName", 0, 0, false, false, false, false);
        QueryHeader intColumnQueryHeader = new QueryHeader("schema", "table", "columnLabel", "columnName", Types.INTEGER, "columnTypeName", 0, 0, false, false, false, false);
        when(responseHeader.getQueryHeaders()).thenReturn(Arrays.asList(queryHeader, intColumnQueryHeader));
        when(proxyBackendHandler.execute()).thenReturn(responseHeader);
        when(proxyBackendHandler.next()).thenReturn(true, true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 0))),
                new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        when(sqlStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        Portal portal = createPortal(sqlStatementContext);
        PostgreSQLPacket portalDescription = portal.describe();
        assertThat(portalDescription, isA(PostgreSQLRowDescriptionPacket.class));
        Collection<PostgreSQLColumnDescription> columnDescriptions = (Collection<PostgreSQLColumnDescription>) Plugins.getMemberAccessor()
                .get(PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions"), portalDescription);
        Iterator<PostgreSQLColumnDescription> columnDescriptionIterator = columnDescriptions.iterator();
        PostgreSQLColumnDescription textColumnDescription = columnDescriptionIterator.next();
        PostgreSQLColumnDescription intColumnDescription = columnDescriptionIterator.next();
        assertThat(textColumnDescription.getDataFormat(), is(PostgreSQLValueFormat.TEXT.getCode()));
        assertThat(intColumnDescription.getDataFormat(), is(PostgreSQLValueFormat.BINARY.getCode()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(3));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLCommandCompletePacket.class));
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
        when(selectStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        when(selectStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        Portal portal = createPortal(selectStatementContext);
        assertThat(portal.describe(), isA(PostgreSQLRowDescriptionPacket.class));
        List<DatabasePacket> actualPackets = portal.execute(2);
        assertThat(actualPackets.size(), is(3));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLDataRowPacket.class));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLPortalSuspendedPacket.class));
    }
    
    private Portal createPortal(final SelectStatementContext selectStatementContext) throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                "", selectStatementContext, new HintValueContext(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList());
        List<PostgreSQLValueFormat> resultFormats = new ArrayList<>(Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY));
        Portal result = new Portal("", preparedStatement, Collections.emptyList(), resultFormats, databaseConnectionManager);
        result.bind();
        return result;
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement()).thenReturn(new InsertStatement(databaseType));
        when(insertStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("", insertStatementContext, new HintValueContext(), Collections.emptyList(),Collections.emptyList(),
                Collections.emptyList());
        Portal portal = new Portal("INSERT INTO t VALUES (1)", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), isA(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    void assertExecuteEmptyStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new CommonSQLStatementContext(new EmptyStatement(databaseType)), new HintValueContext(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        assertThat(portal.describe(), is(PostgreSQLNoDataPacket.getInstance()));
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.iterator().next(), isA(PostgreSQLEmptyQueryResponsePacket.class));
    }
    
    @Test
    void assertExecuteSetStatement() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(mock(UpdateResponseHeader.class));
        when(proxyBackendHandler.next()).thenReturn(false);
        String sql = "set client_encoding = utf8";
        SetStatement setStatement = new SetStatement(databaseType, Collections.singletonList(new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "client_encoding"), null)));
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                sql, new CommonSQLStatementContext(setStatement), new HintValueContext(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList());
        Portal portal = new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager);
        portal.bind();
        List<DatabasePacket> actualPackets = portal.execute(0);
        assertThat(actualPackets.size(), is(2));
        assertThat(actualPackets.get(0), isA(PostgreSQLCommandCompletePacket.class));
        assertThat(actualPackets.get(1), isA(PostgreSQLParameterStatusPacket.class));
    }
    
    @Test
    void assertDescribeBeforeBind() {
        PostgreSQLServerPreparedStatement preparedStatement = mock(PostgreSQLServerPreparedStatement.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(preparedStatement.getHintValueContext()).thenReturn(new HintValueContext());
        when(preparedStatement.getSqlStatementContext()).thenReturn(sqlStatementContext);
        assertThrows(IllegalStateException.class, () -> new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager).describe());
    }
    
    @Test
    void assertClose() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement("",
                new CommonSQLStatementContext(new EmptyStatement(databaseType)), new HintValueContext(), Collections.emptyList(),Collections.emptyList(), Collections.emptyList());
        new Portal("", preparedStatement, Collections.emptyList(), Collections.emptyList(), databaseConnectionManager).close();
        verify(databaseConnectionManager).unmarkResourceInUse(proxyBackendHandler);
        verify(proxyBackendHandler).close();
    }
}
