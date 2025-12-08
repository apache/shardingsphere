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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.context.segment.select.invalues.InValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInValuesToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingInValuesTokenGeneratorTest {
    
    private final ShardingInValuesTokenGenerator generator = new ShardingInValuesTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWhenNotNeedInValuesRewrite() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.isNeedInValuesRewrite()).thenReturn(false);
        assertFalse(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.isNeedInValuesRewrite()).thenReturn(true);
        assertTrue(generator.isGenerateSQLToken(selectStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokensWithEmptyDataNodes() {
        generator.setRouteContext(new RouteContext());
        SelectStatementContext selectStatementContext = mockSelectStatementContext();
        Collection<SQLToken> tokens = generator.generateSQLTokens(selectStatementContext);
        assertThat(tokens.size(), is(1));
        ShardingInValuesToken token = (ShardingInValuesToken) tokens.iterator().next();
        assertThat(token.getInValueItems().size(), is(2));
    }
    
    @Test
    void assertGenerateSQLTokensWithDataNodes() {
        RouteContext routeContext = new RouteContext();
        routeContext.getOriginalDataNodes().add(Collections.singleton(new DataNode("ds_0", (String) null, "t_user_0")));
        routeContext.getOriginalDataNodes().add(Collections.singleton(new DataNode("ds_1", (String) null, "t_user_1")));
        generator.setRouteContext(routeContext);
        SelectStatementContext selectStatementContext = mockSelectStatementContext();
        Collection<SQLToken> tokens = generator.generateSQLTokens(selectStatementContext);
        assertThat(tokens.size(), is(1));
        ShardingInValuesToken token = (ShardingInValuesToken) tokens.iterator().next();
        assertThat(token.getInValueItems().size(), is(2));
        assertThat(token.getInValueItems().iterator().next().getDataNodes().size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokensWithNullInValueContext() {
        generator.setRouteContext(new RouteContext());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getInValueContext()).thenReturn(null);
        Collection<SQLToken> tokens = generator.generateSQLTokens(selectStatementContext);
        assertTrue(tokens.isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithEmptyInValueItems() {
        generator.setRouteContext(new RouteContext());
        SelectStatementContext selectStatementContext = mockSelectStatementContextWithEmptyValues();
        Collection<SQLToken> tokens = generator.generateSQLTokens(selectStatementContext);
        assertTrue(tokens.isEmpty());
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        ListExpression listExpression = new ListExpression(10, 20);
        listExpression.getItems().add(new LiteralExpressionSegment(11, 13, 100));
        listExpression.getItems().add(new LiteralExpressionSegment(15, 17, 101));
        InExpression inExpression = mock(InExpression.class);
        when(inExpression.getRight()).thenReturn(listExpression);
        List<ExpressionSegment> valueExpressions = Arrays.asList(
                new LiteralExpressionSegment(11, 13, 100),
                new LiteralExpressionSegment(15, 17, 101));
        InValueContext inValueContext = mock(InValueContext.class);
        when(inValueContext.getInExpression()).thenReturn(inExpression);
        when(inValueContext.getValueExpressions()).thenReturn(valueExpressions);
        when(result.getInValueContext()).thenReturn(inValueContext);
        return result;
    }
    
    private SelectStatementContext mockSelectStatementContextWithEmptyValues() {
        SelectStatementContext result = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        ListExpression listExpression = new ListExpression(10, 20);
        InExpression inExpression = mock(InExpression.class);
        when(inExpression.getRight()).thenReturn(listExpression);
        InValueContext inValueContext = mock(InValueContext.class);
        when(inValueContext.getInExpression()).thenReturn(inExpression);
        when(inValueContext.getValueExpressions()).thenReturn(Collections.emptyList());
        when(result.getInValueContext()).thenReturn(inValueContext);
        return result;
    }
}
