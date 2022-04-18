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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLParameterDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.parser.ParserConfiguration;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComDescribeExecutorTest {
    
    private static final String SCHEMA_NAME = "postgres";
    
    private static final String TABLE_NAME = "t_order";
    
    private static final ShardingSphereSQLParserEngine SQL_PARSER_ENGINE = new ShardingSphereSQLParserEngine("PostgreSQL", 
            new ParserConfiguration(new CacheOption(2000, 65535L, 4), new CacheOption(128, 1024L, 4), false));
    
    private ContextManager contextManagerBefore;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager mockContextManager;
    
    @Mock
    private PostgreSQLConnectionContext connectionContext;
    
    @Mock
    private PostgreSQLComDescribePacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComDescribeExecutor executor;
    
    @Before
    public void setup() {
        contextManagerBefore = ProxyContext.getInstance().getContextManager();
        ProxyContext.getInstance().init(mockContextManager);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(connectionSession.getSchemaName()).thenReturn(SCHEMA_NAME);
        when(mockContextManager.getMetaDataContexts().getAllSchemaNames().contains(SCHEMA_NAME)).thenReturn(true);
        prepareTableMetaData();
    }
    
    private void prepareTableMetaData() {
        Collection<ColumnMetaData> columnMetaData = Arrays.asList(
                new ColumnMetaData("id", Types.INTEGER, true, false, false),
                new ColumnMetaData("k", Types.INTEGER, true, false, false),
                new ColumnMetaData("c", Types.CHAR, true, false, false),
                new ColumnMetaData("pad", Types.CHAR, true, false, false));
        TableMetaData tableMetaData = new TableMetaData(TABLE_NAME, columnMetaData, Collections.emptyList(), Collections.emptyList());
        when(mockContextManager.getMetaDataContexts().getMetaData(SCHEMA_NAME).getDefaultSchema().get(TABLE_NAME)).thenReturn(tableMetaData);
    }
    
    @Test
    public void assertDescribePortal() throws SQLException {
        when(packet.getType()).thenReturn('P');
        when(packet.getName()).thenReturn("P_1");
        Portal<?> portal = mock(Portal.class);
        PostgreSQLRowDescriptionPacket expected = mock(PostgreSQLRowDescriptionPacket.class);
        when(portal.describe()).thenReturn(expected);
        when(connectionContext.getPortal("P_1")).thenReturn(portal);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expected));
    }
    
    @Test
    public void assertDescribePreparedStatementInsertWithoutColumns() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_1";
        when(packet.getName()).thenReturn(statementId);
        final int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        String sql = "insert into t_order values (?, 0, 'char', ?), (2, ?, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED);
        }
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId, statementId, sql, sqlStatement, parameterTypes);
        Collection<DatabasePacket<?>> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket<?>> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(4);
        verify(mockPayload, times(2)).writeInt4(23);
        verify(mockPayload, times(2)).writeInt4(18);
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    public void assertDescribePreparedStatementInsertWithColumns() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_2";
        when(packet.getName()).thenReturn(statementId);
        final int connectionId = 1;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        String sql = "insert into t_order (id, k, c, pad) values (1, ?, ?, ?), (?, 2, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED);
        }
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId, statementId, sql, sqlStatement, parameterTypes);
        Collection<DatabasePacket<?>> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket<?>> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(5);
        verify(mockPayload, times(2)).writeInt4(23);
        verify(mockPayload, times(3)).writeInt4(18);
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    public void assertDescribeSelectPreparedStatement() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_3";
        when(packet.getName()).thenReturn(statementId);
        when(connectionSession.getConnectionId()).thenReturn(1);
        final String sql = "select id, k, c, pad from t_order where id = ?";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        prepareJDBCBackendConnection(sql);
        PostgreSQLPreparedStatementRegistry.getInstance().register(1);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED));
        PostgreSQLPreparedStatementRegistry.getInstance().register(1, statementId, sql, sqlStatement, parameterTypes);
        Collection<DatabasePacket<?>> actual = executor.execute();
        assertThat(actual.size(), is(2));
        Iterator<DatabasePacket<?>> actualPacketsIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        assertThat(actualParameterDescription, instanceOf(PostgreSQLParameterDescriptionPacket.class));
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(1);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4.getValue());
        PostgreSQLRowDescriptionPacket actualRowDescription = (PostgreSQLRowDescriptionPacket) actualPacketsIterator.next();
        List<PostgreSQLColumnDescription> actualColumnDescriptions = getColumnDescriptions(actualRowDescription);
        List<PostgreSQLColumnDescription> expectedColumnDescriptions = Arrays.asList(
                new PostgreSQLColumnDescription("id", 1, Types.INTEGER, 11, "int4"),
                new PostgreSQLColumnDescription("k", 2, Types.INTEGER, 11, "int4"),
                new PostgreSQLColumnDescription("c", 3, Types.CHAR, 60, "int4"),
                new PostgreSQLColumnDescription("pad", 4, Types.CHAR, 120, "int4")
        );
        for (int i = 0; i < expectedColumnDescriptions.size(); i++) {
            PostgreSQLColumnDescription expectedColumnDescription = expectedColumnDescriptions.get(i);
            PostgreSQLColumnDescription actualColumnDescription = actualColumnDescriptions.get(i);
            assertThat(actualColumnDescription.getColumnName(), is(expectedColumnDescription.getColumnName()));
            assertThat(actualColumnDescription.getColumnIndex(), is(expectedColumnDescription.getColumnIndex()));
            assertThat(actualColumnDescription.getColumnLength(), is(expectedColumnDescription.getColumnLength()));
            assertThat(actualColumnDescription.getTypeOID(), is(expectedColumnDescription.getTypeOID()));
        }
    }
    
    private void prepareJDBCBackendConnection(final String sql) throws SQLException {
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(connection.prepareStatement(sql).getParameterMetaData()).thenReturn(parameterMetaData);
        ResultSetMetaData resultSetMetaData = prepareResultSetMetaData();
        when(connection.prepareStatement(sql).getMetaData()).thenReturn(resultSetMetaData);
        when(backendConnection.getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
    }
    
    private ResultSetMetaData prepareResultSetMetaData() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(4);
        when(result.getColumnName(anyInt())).thenReturn("id", "k", "c", "pad");
        when(result.getColumnType(anyInt())).thenReturn(Types.INTEGER, Types.INTEGER, Types.CHAR, Types.CHAR);
        when(result.getColumnDisplaySize(anyInt())).thenReturn(11, 11, 60, 120);
        when(result.getColumnTypeName(anyInt())).thenReturn("int4", "int4", "char", "char");
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private List<PostgreSQLColumnDescription> getColumnDescriptions(final PostgreSQLRowDescriptionPacket packet) {
        Field columnDescriptionsField = PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions");
        columnDescriptionsField.setAccessible(true);
        return (List<PostgreSQLColumnDescription>) columnDescriptionsField.get(packet);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDescribeUnknownType() throws SQLException {
        new PostgreSQLComDescribeExecutor(connectionContext, packet, connectionSession).execute();
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(contextManagerBefore);
    }
}
