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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.mysql.exception.TooManyPlaceholdersException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.PreparedStatementMetadataResolutionException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, MySQLProjectionMetadataResolver.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComStmtPrepareExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock
    private MySQLComStmtPreparePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getAttributeMap().attr(MySQLConstants.CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSets.UTF8MB4_UNICODE_CI);
    }
    
    @Test
    void assertPrepareMultiStatements() {
        when(packet.getSQL()).thenReturn("UPDATE t SET v=v+1 WHERE id=1;UPDATE t SET v=v+1 WHERE id=2;UPDATE t SET v=v+1 WHERE id=3");
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY).get()).thenReturn(0);
        assertThrows(UnsupportedPreparedStatementException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertPrepareSelectStatement() {
        String sql = "SELECT name FROM foo_db.user WHERE id = ? AND name = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(1);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(SelectStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(SelectStatement.class));
        assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Arrays.asList(MySQLBinaryColumnType.LONGLONG, MySQLBinaryColumnType.VAR_STRING)));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(1);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        CacheOption cacheOption = new CacheOption(1024, 1024L);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class))
                .thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(cacheOption, cacheOption)));
        RuleMetaData globalRuleMetaData = result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        SQLFederationRule sqlFederationRule = mock(SQLFederationRule.class);
        doReturn(sqlFederationRule).when(globalRuleMetaData).getSingleRule(SQLFederationRule.class);
        ShardingSphereDatabase database = createDatabase();
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("foo_db"))).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("foo_db"))).thenReturn(true);
        return result;
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereTable table = new ShardingSphereTable("user", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, false, false, false),
                new ShardingSphereColumn("secret", Types.VARCHAR, false, false, false, false, false, false),
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true, false),
                new ShardingSphereColumn("content", Types.CLOB, false, false, false, false, false, true),
                new ShardingSphereColumn("national_content", Types.NCLOB, false, false, false, false, false, true),
                new ShardingSphereColumn("foo_date", Types.DATE, false, false, false, true, false, true),
                new ShardingSphereColumn("mysql_datetime", Types.TIMESTAMP, false, false, false, true, false, true),
                new ShardingSphereColumn("mysql_timestamp", Types.TIMESTAMP, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema),
                new ConfigurationProperties(new Properties()));
    }
    
    @Test
    void assertPrepareLocalTemporalProjection() throws Exception {
        when(packet.getSQL()).thenReturn("SELECT mysql_datetime, mysql_timestamp FROM foo_db.user");
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 17;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class)))
                .thenReturn(Collections.singleton(1)).thenThrow(AssertionError.class);
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            assertThat(actual.next(), isA(MySQLComStmtPrepareOKPacket.class));
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.DATETIME);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertThat(actual.next(), isA(MySQLEofPacket.class));
            assertFalse(actual.hasNext());
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
        }
    }
    
    private void assertLocalProjectionPacket(final MySQLColumnDefinition41Packet actual, final MySQLBinaryColumnType expectedType) {
        MySQLColumnDefinition41Packet expected = new MySQLColumnDefinition41Packet(MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId(), 0, "", "", "", "", "", 0, expectedType, 0, false);
        ByteBuf actualBytes = createPayload(actual).getByteBuf();
        ByteBuf expectedBytes = createPayload(expected).getByteBuf();
        try {
            assertThat(ByteBufUtil.getBytes(actualBytes), is(ByteBufUtil.getBytes(expectedBytes)));
        } finally {
            actualBytes.release();
            expectedBytes.release();
        }
    }
    
    private MySQLPacketPayload createPayload(final MySQLColumnDefinition41Packet packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        packet.write(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8));
        return new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    @Test
    void assertPrepareLocalTemporalProjectionWithFederation() throws SQLException {
        ContextManager contextManager = mockLocalTemporalContext();
        RuleMetaData globalRuleMetaData = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        SQLFederationRule sqlFederationRule = globalRuleMetaData.getSingleRule(SQLFederationRule.class);
        when(sqlFederationRule.isSqlFederationEnabled()).thenReturn(true);
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class)))
                .thenAnswer(invocation -> fail("Federation must not add a DATETIME metadata probe"));
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    private ContextManager mockLocalTemporalContext() {
        when(packet.getSQL()).thenReturn("SELECT mysql_datetime, mysql_timestamp FROM foo_db.user");
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getConnectionId()).thenReturn(17);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        ContextManager result = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(result);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("temporalProjectionWithParametersArguments")
    void assertPrepareLocalTemporalProjectionWithParameters(final String predicate, final int paramCount, final MySQLBinaryColumnType expectedParamType) throws SQLException {
        mockLocalTemporalContext();
        String sql = "SELECT mysql_datetime, mysql_timestamp FROM foo_db.user WHERE " + predicate;
        when(packet.getSQL()).thenReturn(sql);
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class)))
                .thenAnswer(invocation -> fail("Parameterized local PREPARE must not add a DATETIME metadata probe"));
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            MySQLComStmtPrepareOKPacket actualPreparePacket = (MySQLComStmtPrepareOKPacket) actual.next();
            ByteBuf actualPrepareBytes = Unpooled.buffer();
            try {
                actualPreparePacket.write(new MySQLPacketPayload(actualPrepareBytes, StandardCharsets.UTF_8));
                actualPrepareBytes.skipBytes(1 + 4);
                assertThat(actualPrepareBytes.readUnsignedShortLE(), is(2));
                assertThat(actualPrepareBytes.readUnsignedShortLE(), is(paramCount));
            } finally {
                actualPrepareBytes.release();
            }
            for (int index = 0; index < paramCount; index++) {
                assertThat(getColumnDefinitionType((MySQLColumnDefinition41Packet) actual.next()), is(expectedParamType.getValue()));
            }
            assertThat(actual.next(), isA(MySQLEofPacket.class));
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertThat(actual.next(), isA(MySQLEofPacket.class));
            assertFalse(actual.hasNext());
            MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
            assertThat(actualPreparedStatement.getSql(), is(sql));
            assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount(), is(paramCount));
            assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Collections.nCopies(paramCount, expectedParamType)));
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    private int getColumnDefinitionType(final MySQLColumnDefinition41Packet packet) {
        MySQLPacketPayload payload = createPayload(packet);
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        payload.skipReserved(2);
        payload.skipReserved(4);
        return payload.readInt1();
    }
    
    private void skipColumnDefinitionStrings(final MySQLPacketPayload payload) {
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
    }
    
    private static Stream<Arguments> temporalProjectionWithParametersArguments() {
        return Stream.of(
                Arguments.of("secret = ?", 1, MySQLBinaryColumnType.VAR_STRING),
                Arguments.of("secret IN (?, ?)", 2, MySQLBinaryColumnType.VAR_STRING),
                Arguments.of("mysql_datetime = ? AND mysql_timestamp = ?", 2, MySQLBinaryColumnType.TIMESTAMP));
    }
    
    @Test
    void assertPrepareLocalTemporalProjectionWithNoConfirmedDatetime() throws SQLException {
        mockLocalTemporalContext();
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class))).thenReturn(Collections.emptySet());
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @Test
    void assertPrepareLocalTemporalProjectionWithSQLException() throws SQLException {
        mockLocalTemporalContext();
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class))).thenThrow(SQLException.class);
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @Test
    void assertPrepareLocalTemporalProjectionWithUnavailableMetadata() throws SQLException {
        mockLocalTemporalContext();
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class)))
                .thenThrow(new PreparedStatementMetadataResolutionException("no execution unit was generated"));
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.TIMESTAMP);
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @Test
    void assertPrepareLocalTemporalProjectionWithUnexpectedError() throws SQLException {
        mockLocalTemporalContext();
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class))).thenThrow(IllegalStateException.class);
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            assertThrows(IllegalStateException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @Test
    void assertPrepareNonlocalProjectionWithSQLException() throws SQLException {
        mockLocalTemporalContext();
        when(packet.getSQL()).thenReturn("SELECT COUNT(id) FROM foo_db.user");
        when(MySQLProjectionMetadataResolver.resolveProjectionPackets(eq(connectionSession), any(MySQLServerPreparedStatement.class), any(SelectStatementContext.class), anyInt()))
                .thenThrow(SQLException.class);
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertLocalProjectionPacket((MySQLColumnDefinition41Packet) actual.next(), MySQLBinaryColumnType.VAR_STRING);
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @Test
    void assertPrepareNonlocalProjectionWithUnavailableMetadata() throws SQLException {
        mockLocalTemporalContext();
        when(packet.getSQL()).thenReturn("SELECT COUNT(id) FROM foo_db.user");
        when(MySQLProjectionMetadataResolver.resolveProjectionPackets(eq(connectionSession), any(MySQLServerPreparedStatement.class), any(SelectStatementContext.class), anyInt()))
                .thenThrow(new PreparedStatementMetadataResolutionException("no execution unit was generated"));
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            assertThrows(PreparedStatementMetadataResolutionException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonTemporalProjectionArguments")
    void assertPrepareLocalNonTemporalProjection(final String column, final MySQLBinaryColumnType expectedType) throws SQLException {
        mockLocalTemporalContext();
        when(packet.getSQL()).thenReturn("SELECT " + column + " FROM foo_db.user");
        when(MySQLProjectionMetadataResolver.resolveDatetimeColumnIndexes(eq(connectionSession), any(MySQLServerPreparedStatement.class)))
                .thenAnswer(invocation -> fail("Non-TIMESTAMP projections must not add a DATETIME metadata probe"));
        MySQLStatementIdGenerator.getInstance().registerConnection(17);
        try {
            Iterator<DatabasePacket> actual = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            actual.next();
            assertThat(getColumnDefinitionType((MySQLColumnDefinition41Packet) actual.next()), is(expectedType.getValue()));
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(17);
        }
    }
    
    private static Stream<Arguments> nonTemporalProjectionArguments() {
        return Stream.of(Arguments.of("name", MySQLBinaryColumnType.VAR_STRING), Arguments.of("age", MySQLBinaryColumnType.SHORT), Arguments.of("foo_date", MySQLBinaryColumnType.DATE));
    }
    
    @Test
    void assertPrepareCharacterLargeObjectStatement() {
        String sql = "SELECT content, national_content FROM foo_db.user WHERE content = ? AND national_content = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 5;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        assertCharacterLargeObjectParameterDefinition((MySQLColumnDefinition41Packet) actualIterator.next());
        assertCharacterLargeObjectParameterDefinition((MySQLColumnDefinition41Packet) actualIterator.next());
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertCharacterLargeObjectProjectionDefinition((MySQLColumnDefinition41Packet) actualIterator.next());
        assertCharacterLargeObjectProjectionDefinition((MySQLColumnDefinition41Packet) actualIterator.next());
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Arrays.asList(MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.VAR_STRING)));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    private void assertCharacterLargeObjectParameterDefinition(final MySQLColumnDefinition41Packet actual) {
        assertThat(getColumnDefinitionCharacterSet(actual), is(MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId()));
        assertThat(getColumnDefinitionType(actual), is(MySQLBinaryColumnType.LONG_BLOB.getValue()));
        assertThat(getColumnDefinitionFlag(actual), is(0));
    }
    
    private void assertCharacterLargeObjectProjectionDefinition(final MySQLColumnDefinition41Packet actual) {
        assertThat(getColumnDefinitionCharacterSet(actual), is(MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId()));
        assertThat(getColumnDefinitionType(actual), is(MySQLBinaryColumnType.BLOB.getValue()));
        assertThat(getColumnDefinitionFlag(actual), is(MySQLColumnDefinitionFlag.BLOB.getValue()));
    }
    
    @Test
    void assertPrepareSelectExpressionStatementByProbe() throws Exception {
        String sql = "SELECT ~id AS bitwise_not FROM foo_db.user";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 4;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        MySQLColumnDefinition41Packet expectedProjectionPacket = new MySQLColumnDefinition41Packet(MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId(),
                MySQLColumnDefinitionFlag.UNSIGNED.getValue() + MySQLColumnDefinitionFlag.NOT_NULL.getValue(), "foo_db", "", "", "bitwise_not", "bitwise_not", 20,
                MySQLBinaryColumnType.LONGLONG, 0, false);
        when(MySQLProjectionMetadataResolver.resolveProjectionPackets(eq(connectionSession), any(MySQLServerPreparedStatement.class), any(SelectStatementContext.class), anyInt()))
                .thenReturn(Collections.singleton(expectedProjectionPacket));
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        DatabasePacket actualProjectionPacket = actualIterator.next();
        assertThat(actualProjectionPacket, sameInstance(expectedProjectionPacket));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) actualProjectionPacket), is(MySQLColumnDefinitionFlag.UNSIGNED.getValue() + MySQLColumnDefinitionFlag.NOT_NULL.getValue()));
        assertThat(getColumnDefinitionType((MySQLColumnDefinition41Packet) actualProjectionPacket), is(MySQLBinaryColumnType.LONGLONG.getValue()));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    @Test
    void assertPrepareInsertStatement() {
        String sql = "INSERT INTO user (id, name, age) VALUES (1, ?, ?), (?, 'bar', ?)";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 2;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        DatabasePacket firstAgeColumnDefinitionPacket = actualIterator.next();
        assertThat(firstAgeColumnDefinitionPacket, isA(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) firstAgeColumnDefinitionPacket), is(MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        DatabasePacket idColumnDefinitionPacket = actualIterator.next();
        assertThat(idColumnDefinitionPacket, isA(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) idColumnDefinitionPacket),
                is(MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue() | MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        DatabasePacket secondAgeColumnDefinitionPacket = actualIterator.next();
        assertThat(secondAgeColumnDefinitionPacket, isA(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) secondAgeColumnDefinitionPacket), is(MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(InsertStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(InsertStatement.class));
        assertThat(actualPreparedStatement.getParameterColumnTypes(),
                is(Arrays.asList(MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.SHORT, MySQLBinaryColumnType.LONGLONG, MySQLBinaryColumnType.SHORT)));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    private int getColumnDefinitionFlag(final MySQLColumnDefinition41Packet packet) {
        MySQLPacketPayload payload = createPayload(packet);
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        payload.skipReserved(2);
        payload.skipReserved(4);
        payload.skipReserved(1);
        return payload.readInt2();
    }
    
    private int getColumnDefinitionCharacterSet(final MySQLColumnDefinition41Packet packet) {
        MySQLPacketPayload payload = createPayload(packet);
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        return payload.readInt2();
    }
    
    @Test
    void assertPrepareInsertStatementWithTooManyPlaceholders() {
        String sql = createTooManyPlaceholdersSQL();
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 2;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(TooManyPlaceholdersException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
    }
    
    private String createTooManyPlaceholdersSQL() {
        StringBuilder builder = new StringBuilder("INSERT INTO USER (ID, NAME, AGE) VALUES (?, ?, ?)");
        for (int index = 0; index < Short.MAX_VALUE; index++) {
            builder.append(", (?, ?, ?)");
        }
        return builder.toString();
    }
    
    @Test
    void assertPrepareUpdateStatement() {
        String sql = "UPDATE user SET name = ?, age = ? WHERE id = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(1);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(UpdateStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(UpdateStatement.class));
        assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Arrays.asList(MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.SHORT, MySQLBinaryColumnType.LONGLONG)));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(1);
    }
    
    @Test
    void assertPrepareUpdateStatementWithDatabaseDefaultSchema() {
        String sql = "UPDATE user SET age = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 6;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        ContextManager contextManager = mockContextManager();
        ShardingSphereSchema defaultSchema = createDatabase().getSchema("foo_db");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.findDefaultSchema()).thenReturn(Optional.of(defaultSchema));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        UpdateStatement sqlStatement = (UpdateStatement) contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class)
                .getSQLParserEngine(databaseType).parse(sql, true);
        UpdateStatementContext sqlStatementContext = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(tablesContext.getSchemaName()).thenReturn(Optional.empty());
        when(tablesContext.getTableNames()).thenReturn(Collections.singleton("user"));
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        try (
                MockedConstruction<SQLBindEngine> ignored = mockConstruction(SQLBindEngine.class,
                        (mock, context) -> when(mock.bind(any(UpdateStatement.class))).thenReturn(sqlStatementContext))) {
            Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
            assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
            assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
            assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
            assertFalse(actualIterator.hasNext());
            MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
            assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Collections.singletonList(MySQLBinaryColumnType.SHORT)));
        } finally {
            MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
        }
    }
    
    @Test
    void assertPrepareDeleteStatement() {
        String sql = "DELETE FROM user WHERE name = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 3;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), isA(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), isA(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(DeleteStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(DeleteStatement.class));
        assertThat(actualPreparedStatement.getParameterColumnTypes(), is(Collections.singletonList(MySQLBinaryColumnType.VAR_STRING)));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    @Test
    void assertPrepareHintedWildcardOKPacketColumnCountMatchesDefinitions() throws Exception {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setDataSourceName("foo_ds");
        when(packet.getSQL()).thenReturn("SELECT * FROM foo_db.user");
        when(packet.getHintValueContext()).thenReturn(hintValueContext);
        int connectionId = 6;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        int physicalColumnCount = 3;
        when(MySQLProjectionMetadataResolver.resolveProjectionPackets(eq(connectionSession), any(MySQLServerPreparedStatement.class), any(SelectStatementContext.class), anyInt()))
                .thenReturn(Collections.nCopies(physicalColumnCount, mock(MySQLColumnDefinition41Packet.class)));
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        MySQLComStmtPrepareOKPacket okPacket = (MySQLComStmtPrepareOKPacket) actualIterator.next();
        ByteBuf okBuf = Unpooled.buffer();
        okPacket.write(new MySQLPacketPayload(okBuf, StandardCharsets.UTF_8));
        okBuf.skipBytes(1 + 4);
        int okColumnCount = okBuf.readShortLE();
        okBuf.release();
        int columnDefinitionCount = 0;
        while (actualIterator.hasNext()) {
            DatabasePacket next = actualIterator.next();
            if (next instanceof MySQLColumnDefinition41Packet) {
                columnDefinitionCount++;
            }
        }
        assertThat("OK packet column count must match emitted definitions", okColumnCount, is(columnDefinitionCount));
        assertThat("visible column count must match physical columns", columnDefinitionCount, is(physicalColumnCount));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    @Test
    void assertPrepareNotAllowedStatement() {
        when(packet.getSQL()).thenReturn("begin");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(UnsupportedPreparedStatementException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
    }
}
