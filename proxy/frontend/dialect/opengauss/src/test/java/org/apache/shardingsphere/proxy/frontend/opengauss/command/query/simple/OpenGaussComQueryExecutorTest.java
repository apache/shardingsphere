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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxySQLComQueryParser.class, ProxyBackendHandlerFactory.class})
class OpenGaussComQueryExecutorTest {
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PostgreSQLComQueryPacket queryPacket;
    
    private DatabaseType databaseType;
    
    private OpenGaussComQueryExecutor queryExecutor;
    
    @BeforeEach
    void setUp() throws SQLException {
        databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(queryPacket.getSQL()).thenReturn("sql");
        when(queryPacket.getHintValueContext()).thenReturn(new HintValueContext());
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(ProxySQLComQueryParser.parse("sql", databaseType, connectionSession)).thenReturn(sqlStatement);
        when(ProxyBackendHandlerFactory.newInstance(databaseType, "sql", sqlStatement, connectionSession, queryPacket.getHintValueContext())).thenReturn(proxyBackendHandler);
        queryExecutor = new OpenGaussComQueryExecutor(portalContext, queryPacket, connectionSession);
    }
    
    @Test
    void assertExecuteQueryReturnsRowDescription() throws SQLException, ReflectiveOperationException {
        QueryHeader queryHeader = new QueryHeader("schema", "table", "label", "column", 1, "type", 2, 3, true, true, true, true);
        QueryResponseHeader queryResponseHeader = mock(QueryResponseHeader.class);
        when(queryResponseHeader.getQueryHeaders()).thenReturn(Collections.singletonList(queryHeader));
        when(proxyBackendHandler.execute()).thenReturn(queryResponseHeader);
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        List<DatabasePacket> actualPacketList = new LinkedList<>(actualPackets);
        assertThat(actualPacketList.size(), is(1));
        PostgreSQLRowDescriptionPacket actualPacket = (PostgreSQLRowDescriptionPacket) actualPacketList.iterator().next();
        assertThat(queryExecutor.getResponseType(), is(ResponseType.QUERY));
        Collection<?> columnDescriptions = (Collection<?>) Plugins.getMemberAccessor()
                .get(PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions"), actualPacket);
        PostgreSQLColumnDescription actualColumn = (PostgreSQLColumnDescription) columnDescriptions.iterator().next();
        assertThat(actualColumn.getColumnName(), is("label"));
        assertThat(actualColumn.getColumnIndex(), is(1));
    }
    
    @Test
    void assertExecuteUpdateWithCommitClosesPortals() throws SQLException {
        UpdateResponseHeader updateResponseHeader = new UpdateResponseHeader(new CommitStatement(databaseType));
        when(proxyBackendHandler.execute()).thenReturn(updateResponseHeader);
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        assertThat(new LinkedList<>(actualPackets).getFirst(), is(isA(PostgreSQLCommandCompletePacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
        verify(portalContext).closeAll();
    }
    
    @Test
    void assertExecuteUpdateWithRollbackClosesPortals() throws SQLException {
        UpdateResponseHeader updateResponseHeader = new UpdateResponseHeader(new RollbackStatement(databaseType));
        when(proxyBackendHandler.execute()).thenReturn(updateResponseHeader);
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        assertThat(new LinkedList<>(actualPackets).getFirst(), is(isA(PostgreSQLCommandCompletePacket.class)));
        verify(portalContext).closeAll();
    }
    
    @Test
    void assertExecuteUpdateWithSetStatementReturnsParameterStatus() throws SQLException, ReflectiveOperationException {
        VariableAssignSegment assignWithValue = new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "timezone"), "'UTC'");
        VariableAssignSegment assignWithNull = new VariableAssignSegment(0, 0, new VariableSegment(0, 0, "work_mem"), null);
        SetStatement setStatement = new SetStatement(databaseType, Arrays.asList(assignWithValue, assignWithNull));
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(setStatement));
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        List<DatabasePacket> actualPacketList = new LinkedList<>(actualPackets);
        PostgreSQLCommandCompletePacket commandCompletePacket = (PostgreSQLCommandCompletePacket) actualPacketList.get(0);
        PostgreSQLParameterStatusPacket parameterStatusPacket = (PostgreSQLParameterStatusPacket) actualPacketList.get(1);
        PostgreSQLParameterStatusPacket parameterStatusPacketWithNull = (PostgreSQLParameterStatusPacket) actualPacketList.get(2);
        assertThat(commandCompletePacket, is(isA(PostgreSQLCommandCompletePacket.class)));
        Object actualValue = Plugins.getMemberAccessor().get(PostgreSQLParameterStatusPacket.class.getDeclaredField("value"), parameterStatusPacket);
        assertThat(actualValue, is("UTC"));
        Object actualNullValue = Plugins.getMemberAccessor().get(PostgreSQLParameterStatusPacket.class.getDeclaredField("value"), parameterStatusPacketWithNull);
        assertNull(actualNullValue);
    }
    
    @Test
    void assertExecuteUpdateWithEmptyStatementReturnsEmptyQueryResponse() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new EmptyStatement(databaseType)));
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        assertThat(new LinkedList<>(actualPackets).getFirst(), is(isA(org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    void assertExecuteUpdateReturnsCommandCompleteForDML() throws SQLException {
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new InsertStatement(databaseType)));
        Collection<DatabasePacket> actualPackets = queryExecutor.execute();
        assertThat(new LinkedList<>(actualPackets).getFirst(), is(isA(PostgreSQLCommandCompletePacket.class)));
        assertThat(queryExecutor.getResponseType(), is(ResponseType.UPDATE));
    }
    
    @Test
    void assertNext() throws SQLException {
        when(proxyBackendHandler.next()).thenReturn(true);
        assertTrue(queryExecutor.next());
    }
    
    @Test
    void assertGetQueryRowPacket() throws SQLException {
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(new LinkedList<>()));
        assertThat(queryExecutor.getQueryRowPacket(), is(isA(PostgreSQLDataRowPacket.class)));
    }
    
    @Test
    void assertClose() throws SQLException {
        queryExecutor.close();
        verify(proxyBackendHandler).close();
    }
}
