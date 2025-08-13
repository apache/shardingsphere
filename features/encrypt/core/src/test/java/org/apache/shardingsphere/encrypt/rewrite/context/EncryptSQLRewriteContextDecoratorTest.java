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

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
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
}
