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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    void assertDescribePreparedStatementWithExistingRowDescription() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_exist";
        when(packet.getName()).thenReturn(statementId);
        String sql = "SELECT 1";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ServerPreparedStatementRegistry serverPreparedStatementRegistry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(serverPreparedStatementRegistry);
        PostgreSQLServerPreparedStatement preparedStatement = new PostgreSQLServerPreparedStatement(
                sql, sqlStatementContext, new HintValueContext(), new ArrayList<>(), Collections.emptyList());
        preparedStatement.setRowDescription(PostgreSQLNoDataPacket.getInstance());
        serverPreparedStatementRegistry.addPreparedStatement(statementId, preparedStatement);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> actualIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(0);
        assertThat(actualIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsertMetaDataCases")
    void assertDescribePreparedStatementInsertByMetaData(final String testName, final String statementId, final String sql,
                                                         final int expectedParamCount, final int expectedInt4Count, final int expectedCharCount) throws SQLException {
        when(packet.getType()).thenReturn('S');
        when(packet.getName()).thenReturn(statementId);
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
        Iterator<DatabasePacket> actualPacketsIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket actualParameterDescription = (PostgreSQLParameterDescriptionPacket) actualPacketsIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        actualParameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(expectedParamCount);
        verify(mockPayload, times(expectedInt4Count)).writeInt4(PostgreSQLColumnType.INT4.getValue());
        verify(mockPayload, times(expectedCharCount)).writeInt4(PostgreSQLColumnType.CHAR.getValue());
        assertThat(actualPacketsIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithoutParameters() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_early_return";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order VALUES (1)";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ServerPreparedStatementRegistry serverPreparedStatementRegistry = new ServerPreparedStatementRegistry();
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(serverPreparedStatementRegistry);
        serverPreparedStatementRegistry.addPreparedStatement(statementId,
                new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), new ArrayList<>(), Collections.emptyList()));
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> actualIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(0);
        assertThat(actualIterator.hasNext(), is(false));
    }
    
    @Test
    void assertDescribePreparedStatementInsertWithSchemaAndMixedParameterTypes() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_schema";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO public.t_small (col1, col2) VALUES (?, ?) RETURNING *, col1 + col2 expr_sum";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.UNSPECIFIED));
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ShardingSphereTable table = new ShardingSphereTable("t_small",
                Arrays.asList(
                        new ShardingSphereColumn("col1", Types.INTEGER, true, false, false, true, false, false),
                        new ShardingSphereColumn("col2", Types.SMALLINT, true, false, false, true, false, false)),
                Collections.emptyList(), Collections.emptyList());
        ContextManager contextManager = mockContextManager(table);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> actualIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(2);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT4.getValue());
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT2.getValue());
        PostgreSQLRowDescriptionPacket rowDescriptionPacket = (PostgreSQLRowDescriptionPacket) actualIterator.next();
        List<PostgreSQLColumnDescription> columnDescriptions = getColumnDescriptions(rowDescriptionPacket);
        assertThat(columnDescriptions.size(), is(3));
        assertThat(columnDescriptions.get(0).getColumnName(), is("col1"));
        assertThat(columnDescriptions.get(0).getColumnLength(), is(4));
        assertThat(columnDescriptions.get(1).getColumnName(), is("col2"));
        assertThat(columnDescriptions.get(1).getColumnLength(), is(2));
        assertThat(columnDescriptions.get(2).getColumnName(), is("expr_sum"));
        assertThat(columnDescriptions.get(2).getTypeOID(), is(PostgreSQLColumnType.VARCHAR.getValue()));
        assertThat(columnDescriptions.get(2).getColumnLength(), is(-1));
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
    void assertDescribePreparedStatementInsertWithUnspecifiedTypesAndNoMarkers() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_mismatch";
        when(packet.getName()).thenReturn(statementId);
        String sql = "INSERT INTO t_order VALUES (1)";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Collections.singletonList(PostgreSQLColumnType.UNSPECIFIED));
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, Collections.emptyList()));
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> actualIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(1);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.UNSPECIFIED.getValue());
        assertThat(actualIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideReturningCases")
    void assertDescribePreparedStatementInsertWithReturning(final String testName, final String statementId, final String sql,
                                                            final List<PostgreSQLColumnType> expectedParamTypes, final List<PostgreSQLColumnDescription> expectedColumns) throws SQLException {
        when(packet.getType()).thenReturn('S');
        when(packet.getName()).thenReturn(statementId);
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
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        Collection<DatabasePacket> actualPackets = executor.execute();
        Iterator<DatabasePacket> actualIterator = actualPackets.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(expectedParamTypes.size());
        Map<Integer, Long> expectedTypeCounts = expectedParamTypes.stream()
                .collect(Collectors.groupingBy(PostgreSQLColumnType::getValue, Collectors.counting()));
        for (Map.Entry<Integer, Long> entry : expectedTypeCounts.entrySet()) {
            verify(mockPayload, times(entry.getValue().intValue())).writeInt4(entry.getKey());
        }
        PostgreSQLRowDescriptionPacket rowDescriptionPacket = (PostgreSQLRowDescriptionPacket) actualIterator.next();
        List<PostgreSQLColumnDescription> actualColumnDescriptions = getColumnDescriptions(rowDescriptionPacket);
        assertThat(actualColumnDescriptions.size(), is(expectedColumns.size()));
        for (int i = 0; i < expectedColumns.size(); i++) {
            PostgreSQLColumnDescription expectedColumn = expectedColumns.get(i);
            PostgreSQLColumnDescription actualColumn = actualColumnDescriptions.get(i);
            assertThat(actualColumn.getColumnName(), is(expectedColumn.getColumnName()));
            assertThat(actualColumn.getTypeOID(), is(expectedColumn.getTypeOID()));
            assertThat(actualColumn.getColumnLength(), is(expectedColumn.getColumnLength()));
        }
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
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
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
    
    @Test
    void assertDescribeSelectPreparedStatementWithNullMetaData() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_null_metadata";
        when(packet.getName()).thenReturn(statementId);
        String sql = "SELECT id, k FROM t_order";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        prepareJDBCBackendConnectionWithNullMetaData(sql);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>();
        List<Integer> parameterIndexes = Collections.emptyList();
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> actualIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(0);
        assertThat(actualIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDescribeSelectPreparedStatementWithPresetRowDescription() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_pre_described";
        when(packet.getName()).thenReturn(statementId);
        String sql = "SELECT id FROM t_order WHERE id = ?";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Collections.singleton(PostgreSQLColumnType.INT4));
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        PostgreSQLServerPreparedStatement preparedStatement = mock(PostgreSQLServerPreparedStatement.class);
        when(preparedStatement.describeRows()).thenReturn(Optional.empty(), Optional.of(PostgreSQLNoDataPacket.getInstance()));
        when(preparedStatement.describeParameters()).thenReturn(new PostgreSQLParameterDescriptionPacket(parameterTypes));
        when(preparedStatement.getSql()).thenReturn(sql);
        when(preparedStatement.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(preparedStatement.getHintValueContext()).thenReturn(new HintValueContext());
        when(preparedStatement.getParameterTypes()).thenReturn(parameterTypes);
        when(preparedStatement.getActualParameterMarkerIndexes()).thenReturn(parameterIndexes);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        prepareJDBCBackendConnectionWithPreparedStatement(sql);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, preparedStatement);
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> actualIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(1);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT4.getValue());
        assertThat(actualIterator.next(), is(PostgreSQLNoDataPacket.getInstance()));
    }
    
    @Test
    void assertPopulateParameterTypesWithMixedSpecifiedAndUnspecified() throws SQLException {
        when(packet.getType()).thenReturn('S');
        String statementId = "S_mixed_params";
        when(packet.getName()).thenReturn(statementId);
        String sql = "SELECT id FROM t_order WHERE id = ? AND k = ?";
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse(sql, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        prepareJDBCBackendConnectionWithParamTypes(sql, new int[]{Types.INTEGER, Types.SMALLINT}, new String[]{"int4", "int2"});
        List<PostgreSQLColumnType> parameterTypes = new ArrayList<>(Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.UNSPECIFIED));
        List<Integer> parameterIndexes = IntStream.range(0, sqlStatement.getParameterCount()).boxed().collect(Collectors.toList());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(
                statementId, new PostgreSQLServerPreparedStatement(sql, sqlStatementContext, new HintValueContext(), parameterTypes, parameterIndexes));
        Collection<DatabasePacket> actual = executor.execute();
        Iterator<DatabasePacket> actualIterator = actual.iterator();
        PostgreSQLParameterDescriptionPacket parameterDescription = (PostgreSQLParameterDescriptionPacket) actualIterator.next();
        PostgreSQLPacketPayload mockPayload = mock(PostgreSQLPacketPayload.class);
        parameterDescription.write(mockPayload);
        verify(mockPayload).writeInt2(2);
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT4.getValue());
        verify(mockPayload).writeInt4(PostgreSQLColumnType.INT2.getValue());
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
    
    private ContextManager mockContextManager(final ShardingSphereTable table) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getProtocolType()).thenReturn(DATABASE_TYPE);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(DATABASE_TYPE);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        when(result.getMetaDataContexts().getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).containsSchema("public")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME).getSchema("public")).thenReturn(schema);
        when(schema.containsTable(table.getName())).thenReturn(true);
        when(schema.getTable(table.getName())).thenReturn(table);
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
    
    private void prepareJDBCBackendConnectionWithNullMetaData(final String sql) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(preparedStatement.getMetaData()).thenReturn(null);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
    
    private void prepareJDBCBackendConnectionWithPreparedStatement(final String sql) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
    
    private void prepareJDBCBackendConnectionWithParamTypes(final String sql, final int[] paramTypes, final String[] paramTypeNames) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        for (int i = 0; i < paramTypes.length; i++) {
            int index = i + 1;
            when(parameterMetaData.getParameterType(index)).thenReturn(paramTypes[i]);
            when(parameterMetaData.getParameterTypeName(index)).thenReturn(paramTypeNames[i]);
        }
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        ResultSetMetaData resultSetMetaData = prepareResultSetMetaDataForSingleColumn();
        when(preparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
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
    
    private ResultSetMetaData prepareResultSetMetaDataForSingleColumn() throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(1);
        when(result.getColumnName(1)).thenReturn("id");
        when(result.getColumnType(1)).thenReturn(Types.INTEGER);
        when(result.getColumnDisplaySize(1)).thenReturn(11);
        when(result.getColumnTypeName(1)).thenReturn("int4");
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
    
    private static Stream<Arguments> provideInsertMetaDataCases() {
        return Stream.of(
                Arguments.of("insert without columns", "S_meta_1", "INSERT INTO t_order VALUES (?, 0, 'char', ?), (2, ?, ?, '')", 4, 2, 2),
                Arguments.of("insert with columns", "S_meta_2", "INSERT INTO t_order (id, k, c, pad) VALUES (1, ?, ?, ?), (?, 2, ?, '')", 5, 2, 3),
                Arguments.of("insert with case-insensitive columns", "S_meta_3", "INSERT INTO t_order (iD, k, c, PaD) VALUES (1, ?, ?, ?), (?, 2, ?, '')", 5, 2, 3)
        );
    }
    
    private static Stream<Arguments> provideReturningCases() {
        return Stream.of(
                Arguments.of("returning complex columns", "S_returning_complex",
                        "INSERT INTO t_order (k, c, pad) VALUES (?, ?, ?) "
                                + "RETURNING id, id alias_id, 'anonymous', 'OK' literal_string, 1 literal_int, 4294967296 literal_bigint, 1.1 literal_numeric, t_order.*, t_order, t_order alias_t_order",
                        Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.CHAR, PostgreSQLColumnType.CHAR),
                        getExpectedReturningColumns()),
                Arguments.of("returning numeric literal", "S_numeric_returning",
                        "INSERT INTO t_order (k) VALUES (?) RETURNING 1.2 numeric_value",
                        Collections.singletonList(PostgreSQLColumnType.INT4),
                        Collections.singletonList(expectedColumn("numeric_value", Types.NUMERIC, -1, "numeric"))),
                Arguments.of("returning boolean literal", "S_boolean_returning",
                        "INSERT INTO t_order (k) VALUES (?) RETURNING true bool_value",
                        Collections.singletonList(PostgreSQLColumnType.INT4),
                        Collections.singletonList(expectedColumn("bool_value", Types.VARCHAR, -1, "varchar"))),
                Arguments.of("returning without parameters", "S_returning_only",
                        "INSERT INTO t_order VALUES (1) RETURNING id",
                        Collections.emptyList(),
                        Collections.singletonList(expectedColumn("id", Types.INTEGER, 4, "int4")))
        );
    }
    
    private static PostgreSQLColumnDescription expectedColumn(final String columnName, final int jdbcType, final int columnLength, final String columnTypeName) {
        return new PostgreSQLColumnDescription(columnName, 0, jdbcType, columnLength, columnTypeName);
    }
    
    private static List<PostgreSQLColumnDescription> getExpectedReturningColumns() {
        return Arrays.asList(
                expectedColumn("id", Types.INTEGER, 4, "int4"),
                expectedColumn("alias_id", Types.INTEGER, 4, "int4"),
                expectedColumn("?column?", Types.VARCHAR, -1, "varchar"),
                expectedColumn("literal_string", Types.VARCHAR, -1, "varchar"),
                expectedColumn("literal_int", Types.INTEGER, 4, "int4"),
                expectedColumn("literal_bigint", Types.BIGINT, 8, "int8"),
                expectedColumn("literal_numeric", Types.NUMERIC, -1, "numeric"),
                expectedColumn("id", Types.INTEGER, 4, "int4"),
                expectedColumn("k", Types.INTEGER, 4, "int4"),
                expectedColumn("c", Types.CHAR, -1, "char"),
                expectedColumn("pad", Types.CHAR, -1, "char"),
                expectedColumn("t_order", Types.VARCHAR, -1, "varchar"),
                expectedColumn("alias_t_order", Types.VARCHAR, -1, "varchar")
        );
    }
}
