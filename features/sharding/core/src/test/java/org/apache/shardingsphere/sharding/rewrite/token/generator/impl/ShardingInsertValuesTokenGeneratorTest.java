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

import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInsertValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingInsertValuesTokenGeneratorTest {
    
    private final ShardingInsertValuesTokenGenerator generator = new ShardingInsertValuesTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotInsertStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyInsertValues() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getValues().isEmpty()).thenReturn(true);
        assertFalse(generator.isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        assertTrue(generator.isGenerateSQLToken(mock(InsertStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertGenerateSQLTokenWithEmptyDataNode() {
        generator.setRouteContext(new RouteContext());
        InsertValuesToken actual = generator.generateSQLToken(mockInsertStatementContext());
        assertThat(actual.getInsertValues().size(), is(1));
        assertThat(actual.getInsertValues().get(0).getValues().size(), is(1));
    }
    
    @Test
    void assertGenerateSQLTokenWithDataNodes() {
        RouteContext routeContext = new RouteContext();
        routeContext.getOriginalDataNodes().add(Collections.singleton(new DataNode("foo_ds", (String) null, "foo_tbl")));
        generator.setRouteContext(routeContext);
        InsertValuesToken actual = generator.generateSQLToken(mockInsertStatementContext());
        ShardingInsertValue actualInsertValue = (ShardingInsertValue) actual.getInsertValues().get(0);
        assertThat((new ArrayList<>(actualInsertValue.getDataNodes())).get(0), is(new DataNode("foo_ds.foo_tbl")));
    }
    
    private InsertStatementContext mockInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        List<ExpressionSegment> assignments = Collections.singletonList(new LiteralExpressionSegment(0, 0, "foo"));
        when(result.getInsertValueContexts()).thenReturn(Collections.singletonList(new InsertValueContext(assignments, Collections.emptyList(), 4)));
        when(result.getSqlStatement().getValues()).thenReturn(Collections.singleton(new InsertValuesSegment(1, 2, assignments)));
        return result;
    }
}
