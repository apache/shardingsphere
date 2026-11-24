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

package org.apache.shardingsphere.shadow.rewrite.token.generator;

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.rewrite.token.pojo.ShadowInsertValuesToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShadowInsertValuesTokenGeneratorTest {
    
    @Test
    void assertGenerateSQLToken() {
        InsertStatementContext sqlStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        List<InsertValueContext> insertValueContexts = Arrays.asList(createInsertValueContext(1, "foo"), createInsertValueContext(2, "bar"));
        when(sqlStatementContext.getInsertValueContexts()).thenReturn(insertValueContexts);
        when(sqlStatementContext.getSqlStatement().getValues()).thenReturn(Collections.singletonList(new InsertValuesSegment(10, 30, Collections.emptyList())));
        RouteUnit firstRouteUnit = new RouteUnit(new RouteMapper("logic_ds", "ds_0"), Collections.emptyList());
        RouteUnit secondRouteUnit = new RouteUnit(new RouteMapper("logic_ds", "ds_1"), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnitInsertValueIndexes().put(firstRouteUnit, Collections.singletonList(0));
        routeContext.getRouteUnitInsertValueIndexes().put(secondRouteUnit, Collections.singletonList(1));
        ShadowInsertValuesTokenGenerator generator = new ShadowInsertValuesTokenGenerator(routeContext);
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
        ShadowInsertValuesToken actual = (ShadowInsertValuesToken) generator.generateSQLToken(sqlStatementContext);
        assertThat(actual.toString(firstRouteUnit), is("(1, 'foo')"));
        assertThat(actual.toString(secondRouteUnit), is("(2, 'bar')"));
        assertThat(actual.toString(), is("(1, 'foo'), (2, 'bar')"));
    }
    
    private InsertValueContext createInsertValueContext(final Object orderId, final String cipher) {
        InsertValueContext result = mock(InsertValueContext.class);
        when(result.getValueExpressions()).thenReturn(Arrays.asList(new LiteralExpressionSegment(0, 0, orderId), new LiteralExpressionSegment(0, 0, cipher)));
        return result;
    }
}
