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
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
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
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, MySQLPreparedStatementMetadataFactory.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComStmtPrepareExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock
    private MySQLComStmtPreparePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
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
        PreparedStatement actualPreparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(actualPreparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnName(1)).thenReturn("bitwise_not");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("bitwise_not");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("BIGINT UNSIGNED");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(20);
        when(resultSetMetaData.getScale(1)).thenReturn(0);
        when(resultSetMetaData.isSigned(1)).thenReturn(false);
        when(resultSetMetaData.isNullable(1)).thenReturn(ResultSetMetaData.columnNoNulls);
        when(resultSetMetaData.isAutoIncrement(1)).thenReturn(false);
        when(resultSetMetaData.getTableName(1)).thenReturn("");
        when(MySQLPreparedStatementMetadataFactory.load(eq(connectionSession), any(MySQLServerPreparedStatement.class))).thenReturn(actualPreparedStatement);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), isA(MySQLComStmtPrepareOKPacket.class));
        DatabasePacket actualProjectionPacket = actualIterator.next();
        assertThat(actualProjectionPacket, isA(MySQLColumnDefinition41Packet.class));
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
    
    private int getColumnDefinitionType(final MySQLColumnDefinition41Packet packet) {
        MySQLPacketPayload payload = createPayload(packet);
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        payload.skipReserved(2);
        payload.skipReserved(4);
        return payload.readInt1();
    }
    
    private MySQLPacketPayload createPayload(final MySQLColumnDefinition41Packet packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        packet.write(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8));
        return new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    private void skipColumnDefinitionStrings(final MySQLPacketPayload payload) {
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
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
    void assertPrepareNotAllowedStatement() {
        when(packet.getSQL()).thenReturn("begin");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThrows(UnsupportedPreparedStatementException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        CacheOption cacheOption = new CacheOption(1024, 1024L);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class))
                .thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(cacheOption, cacheOption)));
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
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", databaseType, Collections.singleton(table), Collections.emptyList());
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
    }
}
