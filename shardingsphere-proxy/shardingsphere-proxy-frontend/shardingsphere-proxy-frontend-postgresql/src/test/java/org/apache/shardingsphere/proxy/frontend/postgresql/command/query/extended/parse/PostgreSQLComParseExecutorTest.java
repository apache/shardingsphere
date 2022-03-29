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

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComParseExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Mock
    private PostgreSQLComParsePacket parsePacket;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComParseExecutor executor;
    
    private ContextManager contextManagerBefore;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager mockedContextManager;
    
    @Before
    public void setup() {
        contextManagerBefore = ProxyContext.getInstance().getContextManager();
        ProxyContext.getInstance().init(mockedContextManager);
        PostgreSQLPreparedStatementRegistry.getInstance().register(CONNECTION_ID);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
    }
    
    @Test
    public void assertExecuteWithEmptySQL() {
        final String expectedSQL = "";
        final String statementId = "S_1";
        when(parsePacket.getSql()).thenReturn(expectedSQL);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        Collection<DatabasePacket<?>> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(PostgreSQLParseCompletePacket.getInstance()));
        PostgreSQLPreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementRegistry.getInstance().get(CONNECTION_ID, statementId);
        assertTrue(actualPreparedStatement.getSqlStatement() instanceof EmptyStatement);
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Collections.emptyList()));
    }
    
    @Test
    public void assertExecuteWithParameterizedSQL() {
        final String rawSQL = "/*$0*/insert into sbtest1 /* $1 */ -- $2 \n (id, k, c, pad) \r values \r\n($1, $2, 'apsbd$31a', '$99')/*$0*/ \n--$0";
        final String expectedSQL = "/*$0*/insert into sbtest1 /* $1 */ -- $2 \n (id, k, c, pad) \r values \r\n(?, ?, 'apsbd$31a', '$99')/*$0*/ \n--$0";
        final String statementId = "S_2";
        when(parsePacket.getSql()).thenReturn(rawSQL);
        when(parsePacket.getStatementId()).thenReturn(statementId);
        when(parsePacket.readParameterTypes()).thenReturn(Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4));
        when(connectionSession.getSchemaName()).thenReturn("schema");
        when(mockedContextManager.getMetaDataContexts().getMetaData("schema").getResource().getDatabaseType()).thenReturn(new PostgreSQLDatabaseType());
        when(mockedContextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class)).thenReturn(Optional.of(sqlParserRule));
        Collection<DatabasePacket<?>> actualPackets = executor.execute();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.iterator().next(), is(PostgreSQLParseCompletePacket.getInstance()));
        PostgreSQLPreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementRegistry.getInstance().get(CONNECTION_ID, statementId);
        assertTrue(actualPreparedStatement.getSqlStatement() instanceof PostgreSQLInsertStatement);
        assertThat(actualPreparedStatement.getSql(), is(expectedSQL));
        assertThat(actualPreparedStatement.getParameterTypes(), is(Arrays.asList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4, PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED)));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(contextManagerBefore);
    }
}
