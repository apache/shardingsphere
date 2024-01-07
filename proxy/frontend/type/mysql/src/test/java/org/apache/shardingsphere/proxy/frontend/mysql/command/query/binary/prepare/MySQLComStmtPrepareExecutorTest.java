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
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPrepareOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.mysql.exception.TooManyPlaceholdersException;
import org.apache.shardingsphere.infra.exception.mysql.exception.UnsupportedPreparedStatementException;
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
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLComStmtPrepareExecutorTest {
    
    @Mock
    private MySQLComStmtPreparePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setup() {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_UNICODE_CI);
    }
    
    @Test
    void assertPrepareMultiStatements() {
        when(packet.getSQL()).thenReturn("update t set v=v+1 where id=1;update t set v=v+1 where id=2;update t set v=v+1 where id=3");
        when(connectionSession.getAttributeMap().hasAttr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS)).thenReturn(true);
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_OPTION_MULTI_STATEMENTS).get()).thenReturn(0);
        assertThrows(UnsupportedPreparedStatementException.class, () -> new MySQLComStmtPrepareExecutor(packet, connectionSession).execute());
    }
    
    @Test
    void assertPrepareSelectStatement() {
        String sql = "select name from foo_db.user where id = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getConnectionId()).thenReturn(1);
        MySQLStatementIdGenerator.getInstance().registerConnection(1);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), instanceOf(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), instanceOf(SelectStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), instanceOf(MySQLSelectStatement.class));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(1);
    }
    
    @Test
    void assertPrepareInsertStatement() {
        String sql = "insert into user (id, name, age) values (1, ?, ?), (?, 'bar', ?)";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 2;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(connectionId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), instanceOf(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        DatabasePacket firstAgeColumnDefinitionPacket = actualIterator.next();
        assertThat(firstAgeColumnDefinitionPacket, instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) firstAgeColumnDefinitionPacket), is(MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        DatabasePacket idColumnDefinitionPacket = actualIterator.next();
        assertThat(idColumnDefinitionPacket, instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) idColumnDefinitionPacket),
                is(MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue() | MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        DatabasePacket secondAgeColumnDefinitionPacket = actualIterator.next();
        assertThat(secondAgeColumnDefinitionPacket, instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(getColumnDefinitionFlag((MySQLColumnDefinition41Packet) secondAgeColumnDefinitionPacket), is(MySQLColumnDefinitionFlag.UNSIGNED.getValue()));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), instanceOf(InsertStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), instanceOf(MySQLInsertStatement.class));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(connectionId);
    }
    
    private int getColumnDefinitionFlag(final MySQLColumnDefinition41Packet packet) {
        ByteBuf byteBuf = Unpooled.buffer(22, 22);
        packet.write(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8));
        return byteBuf.getUnsignedShortLE(17);
    }
    
    @Test
    void assertPrepareInsertStatementWithTooManyPlaceholders() {
        String sql = createTooManyPlaceholdersSQL();
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        int connectionId = 2;
        when(connectionSession.getConnectionId()).thenReturn(connectionId);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("foo_db");
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
        String sql = "update user set name = ?, age = ? where id = ?";
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(connectionSession.getDefaultDatabaseName()).thenReturn("foo_db");
        MySQLStatementIdGenerator.getInstance().registerConnection(1);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualIterator = new MySQLComStmtPrepareExecutor(packet, connectionSession).execute().iterator();
        assertThat(actualIterator.next(), instanceOf(MySQLComStmtPrepareOKPacket.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLColumnDefinition41Packet.class));
        assertThat(actualIterator.next(), instanceOf(MySQLEofPacket.class));
        assertFalse(actualIterator.hasNext());
        MySQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(1);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), instanceOf(UpdateStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), instanceOf(MySQLUpdateStatement.class));
        MySQLStatementIdGenerator.getInstance().unregisterConnection(1);
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
        CacheOption cacheOption = new CacheOption(1024, 1024);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class))
                .thenReturn(new SQLParserRule(new SQLParserRuleConfiguration(cacheOption, cacheOption)));
        when(result.getMetaDataContexts().getMetaData().getDatabase(connectionSession.getDatabaseName()).getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ShardingSphereTable table = new ShardingSphereTable("user", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, false, false, true, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, false, false, false, false),
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.getTables().put("user", table);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", TypedSPILoader.getService(DatabaseType.class, "MySQL"),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singletonMap("foo_db", schema));
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        return result;
    }
}
