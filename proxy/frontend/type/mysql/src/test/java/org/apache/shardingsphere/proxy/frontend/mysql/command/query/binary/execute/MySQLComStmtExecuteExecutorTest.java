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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLBinaryResultSetRowPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.UnknownSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyBackendHandlerFactory.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComStmtExecuteExecutorTest {
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyDatabaseConnectionManager databaseConnectionManager;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_GENERAL_CI);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        SQLStatementContext selectStatementContext = prepareSelectStatementContext();
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1))
                .thenReturn(new MySQLServerPreparedStatement("SELECT * FROM tbl WHERE id = ?", selectStatementContext, new HintValueContext(), Collections.emptyList()));
        UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(updateStatementContext.getSqlStatement()).thenReturn(prepareUpdateStatement());
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(2))
                .thenReturn(new MySQLServerPreparedStatement("UPDATE tbl SET col=1 WHERE id = ?", updateStatementContext, new HintValueContext(), Collections.emptyList()));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(3))
                .thenReturn(new MySQLServerPreparedStatement("COMMIT", new UnknownSQLStatementContext(new MySQLCommitStatement()), new HintValueContext(), Collections.emptyList()));
    }
    
    private SQLStatementContext prepareSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
        when(result.getSqlStatement()).thenReturn(prepareSelectStatement());
        return result;
    }
    
    private MySQLSelectStatement prepareSelectStatement() {
        MySQLSelectStatement result = new MySQLSelectStatement();
        result.setProjections(new ProjectionsSegment(0, 0));
        return result;
    }
    
    private MySQLUpdateStatement prepareUpdateStatement() {
        MySQLUpdateStatement result = new MySQLUpdateStatement();
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), new ParameterMarkerExpressionSegment(0, 0, 0));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(columnAssignmentSegment)));
        return result;
    }
    
    @Test
    void assertIsQueryResponse() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(1);
        MySQLComStmtExecuteExecutor executor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        QueryHeader queryHeader = mock(QueryHeader.class);
        when(queryHeader.getColumnTypeName()).thenReturn("VARCHAR");
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(queryHeader)));
        when(proxyBackendHandler.next()).thenReturn(true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        when(ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean())).thenReturn(proxyBackendHandler);
        Iterator<DatabasePacket> actual = executor.execute().iterator();
        assertThat(executor.getResponseType(), is(ResponseType.QUERY));
        assertThat(actual.next(), instanceOf(MySQLFieldCountPacket.class));
        assertThat(actual.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actual.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actual.hasNext());
        assertTrue(executor.next());
        MySQLPacket actualQueryRowPacket = executor.getQueryRowPacket();
        assertThat(actualQueryRowPacket, instanceOf(MySQLBinaryResultSetRowPacket.class));
        executor.close();
        verify(proxyBackendHandler).close();
    }
    
    @Test
    void assertIsUpdateResponse() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getNewParametersBoundFlag()).thenReturn(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST);
        MySQLComStmtExecuteExecutor executor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new MySQLUpdateStatement()));
        when(ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean())).thenReturn(proxyBackendHandler);
        Iterator<DatabasePacket> actual = executor.execute().iterator();
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.next(), instanceOf(MySQLOKPacket.class));
        assertFalse(actual.hasNext());
    }
    
    @Test
    void assertExecutePreparedCommit() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(3);
        MySQLComStmtExecuteExecutor executor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        ProxyBackendHandler proxyBackendHandler = mock(ProxyBackendHandler.class);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new MySQLCommitStatement()));
        when(ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), eq(true))).thenReturn(proxyBackendHandler);
        Iterator<DatabasePacket> actual = executor.execute().iterator();
        assertThat(executor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.next(), instanceOf(MySQLOKPacket.class));
        assertFalse(actual.hasNext());
        executor.close();
        verify(proxyBackendHandler).close();
    }
}
