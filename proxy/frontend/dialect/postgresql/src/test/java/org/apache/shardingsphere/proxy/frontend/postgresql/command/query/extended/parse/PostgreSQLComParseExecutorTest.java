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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.DistSQLStatementContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLComParseExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Mock
    private PostgreSQLComParsePacket parsePacket;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComParseExecutor executor;
    
    @BeforeEach
    void setup() {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        when(connectionSession.getUsedDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
    }
    
    @Test
    void assertExecuteWithEmptySQL() {
        final String expectedSQL = "";
        final String statementId = "S_1";
        when(parsePacket.getSQL()).thenReturn(expectedSQL);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.getHintValueContext()).thenReturn(new HintValueContext());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(PostgreSQLParseCompletePacket.getInstance()));
        PostgreSQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(CommonSQLStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(EmptyStatement.class));
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Collections.emptyList()));
    }
    
    @Test
    void assertExecuteWithParameterizedSQL() throws ReflectiveOperationException {
        final String rawSQL = "/*$0*/insert into sbtest1 /* $1 */ -- $2 \n (id, k, c, pad) \r values \r\n($1, $2, 'apsbd$31a', '$99')/*$0*/ \n--$0";
        final String expectedSQL = "/*$0*/insert into sbtest1 /* $1 */ -- $2 \n (id, k, c, pad) \r values \r\n(?, ?, 'apsbd$31a', '$99')/*$0*/ \n--$0";
        final String statementId = "S_2";
        when(parsePacket.getSQL()).thenReturn(rawSQL);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.readParameterTypes()).thenReturn(Collections.singletonList(PostgreSQLColumnType.INT4));
        when(parsePacket.getHintValueContext()).thenReturn(new HintValueContext());
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        Plugins.getMemberAccessor().set(PostgreSQLComParseExecutor.class.getDeclaredField("connectionSession"), executor, connectionSession);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(PostgreSQLParseCompletePacket.getInstance()));
        PostgreSQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(InsertStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(InsertStatement.class));
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLColumnType.INT4, PostgreSQLColumnType.UNSPECIFIED)));
    }
    
    @Test
    void assertExecuteWithNonOrderedParameterizedSQL() throws ReflectiveOperationException {
        final String rawSQL = "UPDATE t_test SET name=$2 WHERE id=$1";
        final String expectedSQL = "UPDATE t_test SET name=? WHERE id=?";
        final String statementId = "S_2";
        when(parsePacket.getSQL()).thenReturn(rawSQL);
        when(parsePacket.getHintValueContext()).thenReturn(new HintValueContext());
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.readParameterTypes()).thenReturn(Arrays.asList(PostgreSQLColumnType.JSON, PostgreSQLColumnType.INT4));
        Plugins.getMemberAccessor().set(PostgreSQLComParseExecutor.class.getDeclaredField("connectionSession"), executor, connectionSession);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.execute();
        PostgreSQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLColumnType.JSON, PostgreSQLColumnType.INT4)));
        assertThat(actualPreparedStatement.getActualParameterMarkerIndexes(), is(Arrays.asList(1, 0)));
    }
    
    @Test
    void assertExecuteWithQuestionOperator() throws ReflectiveOperationException {
        final String rawSQL = "UPDATE t_test SET enabled = $1 WHERE name ?& $2";
        final String expectedSQL = "UPDATE t_test SET enabled = ? WHERE name ??& ?";
        final String statementId = "S_2";
        when(parsePacket.getSQL()).thenReturn(rawSQL);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.getHintValueContext()).thenReturn(new HintValueContext());
        Plugins.getMemberAccessor().set(PostgreSQLComParseExecutor.class.getDeclaredField("connectionSession"), executor, connectionSession);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        executor.execute();
        PostgreSQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
    }
    
    @Test
    void assertExecuteWithDistSQL() {
        String sql = "SHOW DIST VARIABLE WHERE NAME = sql_show";
        String statementId = "";
        when(parsePacket.getSQL()).thenReturn(sql);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<DatabasePacket> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(PostgreSQLParseCompletePacket.getInstance()));
        PostgreSQLServerPreparedStatement actualPreparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        assertThat(actualPreparedStatement.getSql(), is(sql));
        assertThat(actualPreparedStatement.getSqlStatementContext(), isA(DistSQLStatementContext.class));
        assertThat(actualPreparedStatement.getSqlStatementContext().getSqlStatement(), isA(ShowDistVariableStatement.class));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Collections.emptyList()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db").getProtocolType()).thenReturn(databaseType);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new RuleMetaData(Collections.singleton(new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()))));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.singleton(createSchema()));
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        return result;
    }
    
    private ShardingSphereSchema createSchema() {
        ShardingSphereTable testTable = new ShardingSphereTable("t_test", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, "bigint", false, false, true, false),
                new ShardingSphereColumn("name", Types.VARCHAR, false, false, "varchar", false, false, false, false),
                new ShardingSphereColumn("age", Types.SMALLINT, false, false, "smallint", false, false, true, false),
                new ShardingSphereColumn("enabled", Types.VARCHAR, false, false, "varchar", false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable sbTestTable = new ShardingSphereTable("sbtest1", Arrays.asList(new ShardingSphereColumn("id", Types.BIGINT, true, false, "bigint", false, false, true, false),
                new ShardingSphereColumn("k", Types.VARCHAR, false, false, "varchar", false, false, false, false),
                new ShardingSphereColumn("c", Types.VARCHAR, false, false, "varchar", false, false, true, false),
                new ShardingSphereColumn("pad", Types.VARCHAR, false, false, "varchar", false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        return new ShardingSphereSchema("public", Arrays.asList(testTable, sbTestTable), Collections.emptyList());
    }
}
