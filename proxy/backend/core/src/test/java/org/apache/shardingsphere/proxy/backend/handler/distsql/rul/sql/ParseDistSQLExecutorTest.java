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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.FormatStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.ParseStatement;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.RULBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
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
    
    @Mock
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(sqlParserRule)));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertGetRowDataForMySQL() throws SQLException {
        String sql = "SELECT * FROM t_order";
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        RULBackendHandler<FormatStatement> handler = new SQLRULBackendHandler<>();
        handler.init(new ParseStatement(sql), connectionSession);
        handler.execute();
        handler.next();
        SQLStatement statement = sqlParserRule.getSQLParserEngine("MySQL").parse(sql, false);
        assertThat(new LinkedList<>(handler.getRowData().getData()).getFirst(), is("MySQLSelectStatement"));
        assertThat(JsonParser.parseString(new LinkedList<>(handler.getRowData().getData()).getLast().toString()), is(JsonParser.parseString(new Gson().toJson(statement))));
    }
    
    @Test
    void assertGetRowDataForPostgreSQL() throws SQLException {
        String sql = "SELECT * FROM t_order";
        when(connectionSession.getProtocolType()).thenReturn(new PostgreSQLDatabaseType());
        RULBackendHandler<FormatStatement> handler = new SQLRULBackendHandler<>();
        handler.init(new ParseStatement(sql), connectionSession);
        handler.execute();
        handler.next();
        SQLStatement statement = sqlParserRule.getSQLParserEngine("PostgreSQL").parse(sql, false);
        assertThat(JsonParser.parseString(new LinkedList<>(handler.getRowData().getData()).getLast().toString()), is(JsonParser.parseString(new Gson().toJson(statement))));
    }
    
    @Test
    void assertExecute() {
        String sql = "wrong sql";
        when(connectionSession.getProtocolType()).thenReturn(new MySQLDatabaseType());
        RULBackendHandler<FormatStatement> handler = new SQLRULBackendHandler<>();
        handler.init(new ParseStatement(sql), connectionSession);
        assertThrows(SQLParsingException.class, handler::execute);
    }
}
