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

package org.apache.shardingsphere.shadow.rewrite.context;

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.rewrite.token.pojo.ShadowInsertValuesToken;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowSQLRewriteContextDecoratorTest {
    
    @Test
    void assertDecorateWithInsertValueIndexes() {
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext();
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnitInsertValueIndexes().put(new RouteUnit(new RouteMapper("logic_ds", "ds_0"), Collections.emptyList()), Collections.singletonList(0));
        ShadowSQLRewriteContextDecorator decorator = new ShadowSQLRewriteContextDecorator();
        decorator.decorate(mock(ShadowRule.class), new ConfigurationProperties(new Properties()), sqlRewriteContext, routeContext);
        sqlRewriteContext.generateSQLTokens();
        assertThat(sqlRewriteContext.getSqlTokens().size(), is(1));
        assertTrue(sqlRewriteContext.getSqlTokens().get(0) instanceof ShadowInsertValuesToken);
        assertThat(decorator.getTypeClass().getName(), is(ShadowRule.class.getName()));
    }
    
    @Test
    void assertDecorateWithoutInsertValueIndexes() {
        SQLRewriteContext sqlRewriteContext = createSQLRewriteContext();
        new ShadowSQLRewriteContextDecorator().decorate(mock(ShadowRule.class), new ConfigurationProperties(new Properties()), sqlRewriteContext, new RouteContext());
        sqlRewriteContext.generateSQLTokens();
        assertTrue(sqlRewriteContext.getSqlTokens().isEmpty());
    }
    
    private SQLRewriteContext createSQLRewriteContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getValueExpressions()).thenReturn(Arrays.asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, "foo")));
        when(statementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(statementContext.getSqlStatement().getValues()).thenReturn(Collections.singletonList(new InsertValuesSegment(10, 30, Collections.emptyList())));
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("");
        when(queryContext.getParameters()).thenReturn(Collections.emptyList());
        when(queryContext.getHintValueContext()).thenReturn(new HintValueContext());
        return new SQLRewriteContext(mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS), queryContext);
    }
}
