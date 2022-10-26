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
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.mysql.ProxyContextRestorer;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComStmtExecuteExecutorTest extends ProxyContextRestorer {
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Before
    public void setUp() {
        Map<String, ShardingSphereDatabase> databases = new LinkedHashMap<>(1, 1);
        databases.put("logic_db", mockDatabase());
        ShardingSphereRuleMetaData metaData = mock(ShardingSphereRuleMetaData.class);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, metaData, new ConfigurationProperties(new Properties())));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_GENERAL_CI);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        SQLStatementContext<?> selectStatementContext = prepareSelectStatementContext();
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1))
                .thenReturn(new MySQLServerPreparedStatement("select * from tbl where id = ?", prepareSelectStatement(), selectStatementContext));
        UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(2))
                .thenReturn(new MySQLServerPreparedStatement("update tbl set col=1 where id = ?", prepareUpdateStatement(), updateStatementContext));
        when(connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(3))
                .thenReturn(new MySQLServerPreparedStatement("commit", new MySQLCommitStatement(), new CommonSQLStatementContext<>(new MySQLCommitStatement())));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(result.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new MySQLDatabaseType()));
        when(result.getProtocolType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
    
    private MySQLSelectStatement prepareSelectStatement() {
        MySQLSelectStatement sqlStatement = new MySQLSelectStatement();
        sqlStatement.setProjections(new ProjectionsSegment(0, 0));
        return sqlStatement;
    }
    
    private SQLStatementContext<?> prepareSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getDatabaseName()).thenReturn(Optional.empty());
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
    public void assertIsQueryResponse() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(1);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new QueryResponseHeader(Collections.singletonList(mock(QueryHeader.class))));
        when(proxyBackendHandler.next()).thenReturn(true, false);
        when(proxyBackendHandler.getRowData()).thenReturn(new QueryResponseRow(Collections.singletonList(new QueryResponseCell(Types.INTEGER, 1))));
        Iterator<DatabasePacket<?>> actual;
        try (MockedStatic<ProxyBackendHandlerFactory> mockedStatic = mockStatic(ProxyBackendHandlerFactory.class)) {
            mockedStatic.when(() -> ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean()))
                    .thenReturn(proxyBackendHandler);
            actual = mysqlComStmtExecuteExecutor.execute().iterator();
        }
        assertThat(mysqlComStmtExecuteExecutor.getResponseType(), is(ResponseType.QUERY));
        assertThat(actual.next(), instanceOf(MySQLFieldCountPacket.class));
        assertThat(actual.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actual.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actual.hasNext());
        assertTrue(mysqlComStmtExecuteExecutor.next());
        MySQLPacket actualQueryRowPacket = mysqlComStmtExecuteExecutor.getQueryRowPacket();
        assertThat(actualQueryRowPacket, instanceOf(MySQLBinaryResultSetRowPacket.class));
        assertThat(actualQueryRowPacket.getSequenceId(), is(4));
        mysqlComStmtExecuteExecutor.close();
        verify(proxyBackendHandler).close();
    }
    
    @Test
    public void assertIsUpdateResponse() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(2);
        when(packet.getNewParametersBoundFlag()).thenReturn(MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new MySQLUpdateStatement()));
        Iterator<DatabasePacket<?>> actual;
        try (MockedStatic<ProxyBackendHandlerFactory> mockedStatic = mockStatic(ProxyBackendHandlerFactory.class)) {
            mockedStatic.when(() -> ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), anyBoolean()))
                    .thenReturn(proxyBackendHandler);
            actual = mysqlComStmtExecuteExecutor.execute().iterator();
        }
        assertThat(mysqlComStmtExecuteExecutor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.next(), instanceOf(MySQLOKPacket.class));
        assertFalse(actual.hasNext());
    }
    
    @Test
    public void assertExecutePreparedCommit() throws SQLException {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getStatementId()).thenReturn(3);
        MySQLComStmtExecuteExecutor mysqlComStmtExecuteExecutor = new MySQLComStmtExecuteExecutor(packet, connectionSession);
        ProxyBackendHandler proxyBackendHandler = mock(ProxyBackendHandler.class);
        when(proxyBackendHandler.execute()).thenReturn(new UpdateResponseHeader(new MySQLCommitStatement()));
        Iterator<DatabasePacket<?>> actual;
        try (MockedStatic<ProxyBackendHandlerFactory> mockedStatic = mockStatic(ProxyBackendHandlerFactory.class)) {
            mockedStatic.when(() -> ProxyBackendHandlerFactory.newInstance(any(MySQLDatabaseType.class), any(QueryContext.class), eq(connectionSession), eq(true)))
                    .thenReturn(proxyBackendHandler);
            actual = mysqlComStmtExecuteExecutor.execute().iterator();
        }
        assertThat(mysqlComStmtExecuteExecutor.getResponseType(), is(ResponseType.UPDATE));
        assertThat(actual.next(), instanceOf(MySQLOKPacket.class));
        assertFalse(actual.hasNext());
        mysqlComStmtExecuteExecutor.close();
        verify(proxyBackendHandler).close();
    }
}
