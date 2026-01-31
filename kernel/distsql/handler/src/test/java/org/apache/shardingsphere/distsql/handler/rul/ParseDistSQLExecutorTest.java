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

package org.apache.shardingsphere.distsql.handler.rul;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParseDistSQLExecutorTest {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DistSQLConnectionContext connectionContext;
    
    @BeforeEach
    void setUp() {
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(sqlParserRule)));
    }
    
    @Test
    void assertGetRowDataForMySQL() {
        String sql = "SELECT * FROM t_order";
        when(connectionContext.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ParseDistSQLExecutor executor = new ParseDistSQLExecutor();
        executor.setConnectionContext(connectionContext);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL")).parse(sql, false);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ParseStatement(sql), contextManager);
        assertThat(new LinkedList<>(actual).getFirst().getCell(1), is("SelectStatement"));
        assertThat(new LinkedList<>(actual).getFirst().getCell(2).toString(), is(JsonUtils.toJsonString(sqlStatement)));
    }
    
    @Test
    void assertGetRowDataForPostgreSQL() {
        String sql = "SELECT * FROM t_order";
        when(connectionContext.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        ParseDistSQLExecutor executor = new ParseDistSQLExecutor();
        executor.setConnectionContext(connectionContext);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).parse(sql, false);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ParseStatement(sql), contextManager);
        assertThat(new LinkedList<>(actual).getFirst().getCell(2).toString(), is(JsonUtils.toJsonString(sqlStatement)));
    }
    
    @Test
    void assertExecute() {
        String sql = "wrong sql";
        when(connectionContext.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ParseDistSQLExecutor executor = new ParseDistSQLExecutor();
        executor.setConnectionContext(connectionContext);
        assertThrows(DialectSQLParsingException.class, () -> executor.getRows(new ParseStatement(sql), contextManager));
    }
}
