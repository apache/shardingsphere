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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLParameterDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLComDescribeExecutorTest {
    
    private static final String DATABASE_NAME = "postgres";
    
    private static final String TABLE_NAME = "t_order";
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final SQLParserEngine SQL_PARSER_ENGINE = new ShardingSphereSQLParserEngine(DATABASE_TYPE, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComDescribePacket packet;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComDescribeExecutor executor;
    
    @Test
    void assertDescribePortal() throws SQLException {
        when(packet.getType()).thenReturn('P');
        when(packet.getName()).thenReturn("P_1");
        Portal portal = mock(Portal.class);
        PostgreSQLRowDescriptionPacket expected = mock(PostgreSQLRowDescriptionPacket.class);
        when(portal.describe()).thenReturn(expected);
        when(portalContext.get("P_1")).thenReturn(portal);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(expected));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithoutColumns() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_1";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order VALUES (?, 0, 'char', ?), (2, ?, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes,
                parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(4);
        verify(mockPayload, times(2)).writeInt4(23);
        verify(mockPayload, times(2)).writeInt4(18);
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithColumns() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_2";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order (id, k, c, pad) VALUES (1, ?, ?, ?), (?, 2, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes,
                parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(5);
        verify(mockPayload, times(2)).writeInt4(23);
        verify(mockPayload, times(3)).writeInt4(18);
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithCaseInsensitiveColumns() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_2";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order (iD, k, c, PaD) VALUES (1, ?, ?, ?), (?, 2, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes,
                parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(5);
        verify(mockPayload, times(2)).writeInt4(23);
        verify(mockPayload, times(3)).writeInt4(18);
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithUndefinedColumns() {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_2";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order (undefined_column, k, c, pad) VALUES (1, ?, ?, ?), (?, 2, ?, '')";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId,
                new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        assertThrows(ColumnNotFoundException.class, () -> executor.execute());
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithReturningClause() throws SQLException {
        when(packet.getType()).thenReturn('S');
        final String statementId = "S_2";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order (k, c, pad) VALUES (?, ?, ?) "
                + "RETURNING id, id alias_id, 'anonymous', 'OK' literal_string, 1 literal_int, 4294967296 literal_bigint, 1.1 literal_numeric, t_order.*, t_order, t_order alias_t_order";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(sqlStatement.getParameterCount());
        for (int i = 0; i < sqlStatement.getParameterCount(); i++) {
            parameterTypes.add(PostgreSQLColumnType.UNSPECIFIED);
        }
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes,
                parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(2));
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(3);
        verify(mockPayload).writeInt4(23);
        verify(mockPayload, times(2)).writeInt4(18);
        DatabasePacket actualRowDescriptionPacket = actualPacketsIterator.next();
        assertThat(actualRowDescriptionPacket, is(isA(PostgreSQLRowDescriptionPacket.class)));
        assertRowDescriptions((PostgreSQLRowDescriptionPacket) actualRowDescriptionPacket);
    }
    
    private void assertRowDescriptions(final PostgreSQLRowDescriptionPacket actualRowDescriptionPacket) {
        List<PostgreSQLColumnDescription> actualColumnDescriptions = new ArrayList<>(getColumnDescriptionsFromPacket(actualRowDescriptionPacket));
        assertThat(actualColumnDescriptions.size(), is(13));
        assertThat(actualColumnDescriptions.get(0).getColumnName(), is("id"));
        assertThat(actualColumnDescriptions.get(0).getTypeOID(), is(PostgreSQLColumnType.INT4.getValue()));
        assertThat(actualColumnDescriptions.get(0).getColumnLength(), is(4));
        assertThat(actualColumnDescriptions.get(1).getColumnName(), is("alias_id"));
        assertThat(actualColumnDescriptions.get(1).getTypeOID(), is(PostgreSQLColumnType.INT4.getValue()));
        assertThat(actualColumnDescriptions.get(1).getColumnLength(), is(4));
        assertThat(actualColumnDescriptions.get(2).getColumnName(), is("?column?"));
        assertThat(actualColumnDescriptions.get(2).getTypeOID(), is(PostgreSQLColumnType.VARCHAR.getValue()));
        assertThat(actualColumnDescriptions.get(2).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(3).getColumnName(), is("literal_string"));
        assertThat(actualColumnDescriptions.get(3).getTypeOID(), is(PostgreSQLColumnType.VARCHAR.getValue()));
        assertThat(actualColumnDescriptions.get(3).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(4).getColumnName(), is("literal_int"));
        assertThat(actualColumnDescriptions.get(4).getTypeOID(), is(PostgreSQLColumnType.INT4.getValue()));
        assertThat(actualColumnDescriptions.get(4).getColumnLength(), is(4));
        assertThat(actualColumnDescriptions.get(5).getColumnName(), is("literal_bigint"));
        assertThat(actualColumnDescriptions.get(5).getTypeOID(), is(PostgreSQLColumnType.INT8.getValue()));
        assertThat(actualColumnDescriptions.get(5).getColumnLength(), is(8));
        assertThat(actualColumnDescriptions.get(6).getColumnName(), is("literal_numeric"));
        assertThat(actualColumnDescriptions.get(6).getTypeOID(), is(PostgreSQLColumnType.NUMERIC.getValue()));
        assertThat(actualColumnDescriptions.get(6).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(7).getColumnName(), is("id"));
        assertThat(actualColumnDescriptions.get(7).getTypeOID(), is(PostgreSQLColumnType.INT4.getValue()));
        assertThat(actualColumnDescriptions.get(7).getColumnLength(), is(4));
        assertThat(actualColumnDescriptions.get(8).getColumnName(), is("k"));
        assertThat(actualColumnDescriptions.get(8).getTypeOID(), is(PostgreSQLColumnType.INT4.getValue()));
        assertThat(actualColumnDescriptions.get(8).getColumnLength(), is(4));
        assertThat(actualColumnDescriptions.get(9).getColumnName(), is("c"));
        assertThat(actualColumnDescriptions.get(9).getTypeOID(), is(PostgreSQLColumnType.CHAR.getValue()));
        assertThat(actualColumnDescriptions.get(9).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(10).getColumnName(), is("pad"));
        assertThat(actualColumnDescriptions.get(10).getTypeOID(), is(PostgreSQLColumnType.CHAR.getValue()));
        assertThat(actualColumnDescriptions.get(10).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(11).getColumnName(), is("t_order"));
        assertThat(actualColumnDescriptions.get(11).getTypeOID(), is(PostgreSQLColumnType.VARCHAR.getValue()));
        assertThat(actualColumnDescriptions.get(11).getColumnLength(), is(-1));
        assertThat(actualColumnDescriptions.get(12).getColumnName(), is("alias_t_order"));
        assertThat(actualColumnDescriptions.get(12).getTypeOID(), is(PostgreSQLColumnType.VARCHAR.getValue()));
        assertThat(actualColumnDescriptions.get(12).getColumnLength(), is(-1));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<PostgreSQLColumnDescription> getColumnDescriptionsFromPacket(final PostgreSQLRowDescriptionPacket packet) {
        return (Collection<PostgreSQLColumnDescription>) Plugins.getMemberAccessor().get(PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions"), packet);
    }
    
    @Test
    void assertDescribeSelectPreparedStatement() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_3";
        when(packet.getName()).thenReturn(statementId);
        String sql = "SELECT id, k, c, pad FROM t_order WHERE id = ?";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        prepareJDBCBackendConnection(sql);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Collections.singleton(PostgreSQLColumnType.UNSPECIFIED));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(2));
        Iterator<DatabasePacket> actualPacketsIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        assertThat(actualParameterDescription, isA(PostgreSQLParameterDescriptionPacket.class));
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(1);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT4.getValue());
        PostgreSQLRowDescriptionPacket actualRowDescription = (PostgreSQLRowDescriptionPacket) actualPacketsIterator.next();
        List<PostgreSQLColumnDescription> actualColumnDescriptions = getColumnDescriptions(actualRowDescription);
        List<PostgreSQLColumnDescription> expectedColumnDescriptions = Arrays.asList(
                new PostgreSQLColumnDescription("id", 1, Types.INTEGER, 11, "int4"),
                new PostgreSQLColumnDescription("k", 2, Types.INTEGER, 11, "int4"),
                new PostgreSQLColumnDescription("c", 3, Types.CHAR, 60, "int4"),
                new PostgreSQLColumnDescription("pad", 4, Types.CHAR, 120, "int4"));
        for (int i = 0; i < expectedColumnDescriptions.size(); i++) {
            PostgreSQLColumnDescription expectedColumnDescription = expectedColumnDescriptions.get(i);
            PostgreSQLColumnDescription actualColumnDescription = actualColumnDescriptions.get(i);
            assertThat(actualColumnDescription.getColumnName(), is(expectedColumnDescription.getColumnName()));
            assertThat(actualColumnDescription.getColumnIndex(), is(expectedColumnDescription.getColumnIndex()));
            assertThat(actualColumnDescription.getColumnLength(), is(expectedColumnDescription.getColumnLength()));
            assertThat(actualColumnDescription.getTypeOID(), is(expectedColumnDescription.getTypeOID()));
        }
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getSchema("public")).thenReturn(schema);
        Collection<ShardingSphereColumn> columns = Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("k", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("c", Types.CHAR, true, false, false, true, false, false),
                new ShardingSphereColumn("pad", Types.CHAR, true, false, false, true, false, false));
        when(schema.getTable(TABLE_NAME)).thenReturn(new ShardingSphereTable(TABLE_NAME, columns, Collections.emptyList(), Collections.emptyList()));
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getProtocolType()).thenReturn(DATABASE_TYPE);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(DATABASE_TYPE);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(result.getMetaDataContexts().getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).containsSchema("public")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getSchema("public").containsTable(TABLE_NAME)).thenReturn(true);
        ShardingSphereDatabase database = result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME);
        when(result.getDatabase(DATABASE_NAME)).thenReturn(database);
        return result;
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private void prepareJDBCBackendConnection(final String sql) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(connection.prepareStatement(sql).getParameterMetaData()).thenReturn(parameterMetaData);
        ResultSetMetaData resultSetMetaData = prepareResultSetMetaData();
        when(connection.prepareStatement(sql).getMetaData()).thenReturn(resultSetMetaData);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
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
    @SneakyThrows(ReflectiveOperationException.class)
    private List<PostgreSQLColumnDescription> getColumnDescriptions(final PostgreSQLRowDescriptionPacket packet) {
        return (List<PostgreSQLColumnDescription>) Plugins.getMemberAccessor().get(PostgreSQLRowDescriptionPacket.class.getDeclaredField("columnDescriptions"), packet);
    }
    
    @Test
    void assertDescribeUnknownType() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLComDescribeExecutor(portalContext, packet, connectionSession).execute());
    }
}
