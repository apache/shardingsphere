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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxySQLComQueryParser;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxySQLComQueryParser.class, ProxyBackendHandlerFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLComQueryExecutorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private PostgreSQLComQueryPacket queryPacket;
    
    @Mock
    private ConnectionSession connectionSession;
    
    private PostgreSQLComQueryExecutor queryExecutor;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(queryPacket.getSQL()).thenReturn("select 1");
        when(queryPacket.getHintValueContext()).thenReturn(new HintValueContext());
        SQLStatement sqlStatement = new SQLStatement(DATABASE_TYPE);
        when(ProxySQLComQueryParser.parse(queryPacket.getSQL(), DATABASE_TYPE, connectionSession)).thenReturn(sqlStatement);
        when(ProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, queryPacket.getSQL(), sqlStatement, connectionSession, queryPacket.getHintValueContext())).thenReturn(proxyBackendHandler);
        queryExecutor = new PostgreSQLComQueryExecutor(portalContext, queryPacket, connectionSession);
    }
    
    @Test
    void assertExecuteQueryWithColumnDescription() throws SQLException, ReflectiveOperationException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(new QueryHeader("schema", "table", "label", "column", 1, "type", 2, 3, true, true, true, true)));
        when(proxyBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket> actual = queryExecutor.execute();
        PostgreSQLRowDescriptionPacket rowDescriptionPacket = (PostgreSQLRowDescriptionPacket) actual.iterator().next();
        Collection<PostgreSQLColumnDescription> columnDescriptions = getColumnDescriptions(rowDescriptionPacket);
        PostgreSQLColumnDescription columnDescription = columnDescriptions.iterator().next();
        assertThat(actual.size(), is(1));
        assertThat(columnDescriptions.size(), is(1));
        assertThat(columnDescription.getColumnName(), is("label"));
        assertThat(columnDescription.getColumnIndex(), is(1));
        assertThat(columnDescription.getColumnLength(), is(2));
        assertThat(columnDescription.getTypeOID(), is(new PostgreSQLColumnDescription("column", 1, 1, 2, "type").getTypeOID()));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @Test
    void assertExecuteQueryWithEmptyColumns() throws SQLException, ReflectiveOperationException {
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.emptyList());
        when(proxyBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket> actual = queryExecutor.execute();
        PostgreSQLRowDescriptionPacket rowDescriptionPacket = (PostgreSQLRowDescriptionPacket) actual.iterator().next();
        Collection<PostgreSQLColumnDescription> columnDescriptions = getColumnDescriptions(rowDescriptionPacket);
        assertThat(columnDescriptions.size(), is(0));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("updateScenarios")
    void assertExecuteUpdate(final String testName, final SQLStatement sqlStatement, final Class<? extends DatabasePacket> expectedPacketType,
                             final String expectedTag, final boolean expectCloseAll) throws SQLException, ReflectiveOperationException {
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(sqlStatement));
        Collection<DatabasePacket> actual = queryExecutor.execute();
        DatabasePacket packet = actual.iterator().next();
        assertThat(actual.size(), is(1));
        assertThat(packet, isA(expectedPacketType));
        if (packet instanceof PostgreSQLCommandCompletePacket && null != expectedTag) {
            assertThat(getSqlCommand((PostgreSQLCommandCompletePacket) packet), is(expectedTag));
        }
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
        if (expectCloseAll) {
            verify(portalContext).closeAll();
        } else {
            verify(portalContext, never()).closeAll();
        }
    }
    
    @Test
    void assertExecuteUpdateWithSetStatement() throws SQLException, ReflectiveOperationException {
        VariableSegment searchPathVariable = new VariableSegment(0, 0, "search_path");
        VariableSegment workMemVariable = new VariableSegment(1, 1, "work_mem");
        List<VariableAssignSegment> variableAssigns = new ArrayList<>(2);
        variableAssigns.add(new VariableAssignSegment(0, 0, searchPathVariable, null));
        variableAssigns.add(new VariableAssignSegment(1, 1, workMemVariable, "'64MB'"));
        UpdateResponseHeader updateResponseHeader = new UpdateResponseHeader(new SetStatement(DATABASE_TYPE, variableAssigns));
        when(proxyBackendHandler.execute()).thenReturn(updateResponseHeader);
        Collection<DatabasePacket> actual = queryExecutor.execute();
        Iterator<DatabasePacket> iterator = actual.iterator();
        PostgreSQLCommandCompletePacket commandCompletePacket = (PostgreSQLCommandCompletePacket) iterator.next();
        PostgreSQLParameterStatusPacket firstStatusPacket = (PostgreSQLParameterStatusPacket) iterator.next();
        assertThat(actual.size(), is(3));
        assertThat(getSqlCommand(commandCompletePacket), is("SET"));
        assertThat(getParameterStatusKey(firstStatusPacket), is("search_path"));
        assertNull(getParameterStatusValue(firstStatusPacket));
        PostgreSQLParameterStatusPacket secondStatusPacket = (PostgreSQLParameterStatusPacket) iterator.next();
        assertThat(getParameterStatusKey(secondStatusPacket), is("work_mem"));
        assertThat(getParameterStatusValue(secondStatusPacket), is("64MB"));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
        verify(portalContext, never()).closeAll();
    }
    
    @Test
    void assertNext() throws SQLException {
        when(proxyBackendHandler.next()).thenReturn(true);
        assertTrue(queryExecutor.next());
    }
    
    @Test
    void assertGetQueryRowPacket() throws SQLException {
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.emptyList()));
        PostgreSQLPacket actual = queryExecutor.getQueryRowPacket();
        assertThat(actual, is(isA(PostgreSQLDataRowPacket.class)));
    }
    
    @Test
    void assertClose() throws SQLException {
        queryExecutor.close();
        verify(proxyBackendHandler).close();
    }
    
    @SuppressWarnings("unchecked")
    private Collection<PostgreSQLColumnDescription> getColumnDescriptions(final PostgreSQLRowDescriptionPacket packet) throws ReflectiveOperationException {
        return (Collection<PostgreSQLColumnDescription>) Plugins.getMemberAccessor().get(PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions"), packet);
    }
    
    private String getSqlCommand(final PostgreSQLCommandCompletePacket packet) throws ReflectiveOperationException {
        return (String) Plugins.getMemberAccessor().get(PostgreSQLCommandCompletePacket.class.getDeclaredField("sqlCommand"), packet);
    }
    
    private String getParameterStatusKey(final PostgreSQLParameterStatusPacket packet) throws ReflectiveOperationException {
        return (String) Plugins.getMemberAccessor().get(PostgreSQLParameterStatusPacket.class.getDeclaredField("key"), packet);
    }
    
    private String getParameterStatusValue(final PostgreSQLParameterStatusPacket packet) throws ReflectiveOperationException {
        return (String) Plugins.getMemberAccessor().get(PostgreSQLParameterStatusPacket.class.getDeclaredField("value"), packet);
    }
    
    private static Stream<Arguments> updateScenarios() {
        return Stream.of(
                Arguments.of("commit statement", new CommitStatement(DATABASE_TYPE), PostgreSQLCommandCompletePacket.class, "COMMIT", true),
                Arguments.of("rollback statement", new RollbackStatement(DATABASE_TYPE), PostgreSQLCommandCompletePacket.class, "ROLLBACK", true),
                Arguments.of("recognized command", new InsertStatement(DATABASE_TYPE), PostgreSQLCommandCompletePacket.class, "INSERT", false),
                Arguments.of("empty statement", new EmptyStatement(DATABASE_TYPE), PostgreSQLEmptyQueryResponsePacket.class, null, false),
                Arguments.of("unrecognized statement", new SQLStatement(DATABASE_TYPE), PostgreSQLCommandCompletePacket.class, "", false));
    }
}
