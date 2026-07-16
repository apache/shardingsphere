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

package org.apache.shardingsphere.encrypt.rewrite.context;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.changed.EncryptTableChangedProcessor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptSQLRewriteContextDecoratorTest {
    
    @Mock
    private EncryptRule rule;
    
    private EncryptSQLRewriteContextDecorator decorator;
    
    @BeforeEach
    void setUp() {
        decorator = (EncryptSQLRewriteContextDecorator) OrderedSPILoader.getServices(SQLRewriteContextDecorator.class, Collections.singleton(rule)).get(rule);
    }
    
    @Test
    void assertDecorateWithNotTableAvailable() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        decorator.decorate(rule, mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
    
    @Test
    void assertDecorateWithoutEncryptTable() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        decorator.decorate(rule, mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
    
    @Test
    void assertDecorateWithoutDroppedEncryptTable() {
        EncryptRuleConfiguration ruleConfig = getEncryptRuleConfiguration(true);
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.singleton(
                new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_encrypt")))));
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(insertStatementContext);
        decorator.decorate(new EncryptRule("foo_db", ruleConfig), mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        verify(sqlRewriteContext, never()).addSQLTokenGenerators(any());
    }
    
    @Test
    void assertDecorateWithOpenQueryEncryptTable() {
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        when(sqlRewriteContext.getParameters()).thenReturn(Collections.emptyList());
        UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class, RETURNS_DEEP_STUBS);
        when(updateStatementContext.getTablesContext().getSimpleTables()).thenReturn(Collections.emptyList());
        when(updateStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        when(updateStatementContext.getWhereSegments()).thenReturn(Collections.emptyList());
        UpdateStatement updateStatement = mock(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(createOpenQueryTableSegment());
        when(updateStatementContext.getSqlStatement()).thenReturn(updateStatement);
        when(sqlRewriteContext.getSqlStatementContext()).thenReturn(updateStatementContext);
        when(sqlRewriteContext.getDatabase()).thenReturn(mock(ShardingSphereDatabase.class));
        EncryptRule encryptRule = new EncryptRule("foo_db", getEncryptRuleConfiguration(false));
        decorator.decorate(encryptRule, mock(ConfigurationProperties.class), sqlRewriteContext, mock(RouteContext.class));
        verify(sqlRewriteContext).addSQLTokenGenerators(any());
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration(final boolean dropEncryptTable) {
        EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration("pwd", new EncryptColumnItemRuleConfiguration("pwd_cipher", "standard_encryptor"));
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(columnConfig));
        EncryptRuleConfiguration result = new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(tableConfig)),
                Collections.singletonMap("standard_encryptor", new AlgorithmConfiguration("CORE.FIXTURE", new Properties())));
        if (dropEncryptTable) {
            new EncryptTableChangedProcessor().dropRuleItemConfiguration("t_encrypt", result);
        }
        return result;
    }
    
    private FunctionTableSegment createOpenQueryTableSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "OPENQUERY", "OPENQUERY (foo_server, 'SELECT pwd FROM foo_schema.t_encrypt')");
        functionSegment.getParameters().add(new ColumnSegment(0, 0, new IdentifierValue("foo_server")));
        functionSegment.getParameters().add(new LiteralExpressionSegment(0, 0, "SELECT pwd FROM foo_schema.t_encrypt"));
        return new FunctionTableSegment(0, 0, functionSegment);
    }
}
