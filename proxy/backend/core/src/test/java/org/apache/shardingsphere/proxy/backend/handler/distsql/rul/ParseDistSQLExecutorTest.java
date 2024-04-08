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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul;

import org.apache.shardingsphere.distsql.statement.rul.sql.ParseStatement;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ParseDistSQLExecutorTest {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(sqlParserRule)));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertGetRowDataForMySQL() throws SQLException {
        String sql = "SELECT * FROM t_order";
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        DistSQLQueryBackendHandler handler = new DistSQLQueryBackendHandler(new ParseStatement(sql), connectionSession);
        handler.execute();
        handler.next();
        SQLStatement statement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL")).parse(sql, false);
        assertThat(new LinkedList<>(handler.getRowData().getData()).getFirst(), is("MySQLSelectStatement"));
        assertThat(new LinkedList<>(handler.getRowData().getData()).getLast().toString(), is(JsonUtils.toJsonString(statement)));
    }
    
    @Test
    void assertGetRowDataForPostgreSQL() throws SQLException {
        String sql = "SELECT * FROM t_order";
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        DistSQLQueryBackendHandler handler = new DistSQLQueryBackendHandler(new ParseStatement(sql), connectionSession);
        handler.execute();
        handler.next();
        SQLStatement statement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).parse(sql, false);
        assertThat(new LinkedList<>(handler.getRowData().getData()).getLast().toString(), is(JsonUtils.toJsonString(statement)));
    }
    
    @Test
    void assertExecute() {
        String sql = "wrong sql";
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        DistSQLQueryBackendHandler handler = new DistSQLQueryBackendHandler(new ParseStatement(sql), connectionSession);
        assertThrows(DialectSQLParsingException.class, handler::execute);
    }
}
