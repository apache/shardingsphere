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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhereClauseShardingConditionEngineTest {
    
    private WhereClauseShardingConditionEngine shardingConditionEngine;
    
    @Mock
    private ShardingRule rule;
    
    @Mock
    private SelectStatementContext sqlStatementContext;
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ShardingSphereTable table;
    
    @BeforeEach
    void setUp() {
        shardingConditionEngine = new WhereClauseShardingConditionEngine(database, rule, mock(TimestampServiceRule.class));
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment));
        when(database.containsSchema(new IdentifierValue(""))).thenReturn(true);
        when(database.getSchema(new IdentifierValue(""))).thenReturn(schema);
        when(schema.containsTable(new IdentifierValue(""))).thenReturn(true);
        when(schema.getTable(new IdentifierValue(""))).thenReturn(table);
        when(table.getColumn("foo_sharding_col")).thenReturn(mock(ShardingSphereColumn.class));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectRangeStatement() {
        int between = 1;
        int and = 100;
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, left, betweenSegment, andSegment, false);
        when(whereSegment.getExpr()).thenReturn(betweenExpression);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertThat(actual.get(0).getStartIndex(), is(0));
        assertThat(actual.get(0).getValues().get(0), isA(RangeShardingConditionValue.class));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectInStatement() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ListExpression right = new ListExpression(0, 0);
        LiteralExpressionSegment literalExpressionSegment = new LiteralExpressionSegment(0, 0, 5);
        right.getItems().add(literalExpressionSegment);
        InExpression inExpression = new InExpression(0, 0, left, right, false);
        when(whereSegment.getExpr()).thenReturn(inExpression);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertThat(actual.get(0).getStartIndex(), is(0));
        assertThat(actual.get(0).getValues().get(0), isA(ListShardingConditionValue.class));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectCompareWithCastedParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        TypeCastExpression cast = new TypeCastExpression(0, 0, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        BinaryOperationExpression predicate = new BinaryOperationExpression(0, 0, left, cast, "=", "foo_sharding_col = ?::int4");
        when(whereSegment.getExpr()).thenReturn(predicate);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.singletonList("42"));
        ListShardingConditionValue<?> value = (ListShardingConditionValue<?>) actual.get(0).getValues().get(0);
        assertThat(value.getValues(), is(Collections.singletonList(42)));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectInWithCastedParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new TypeCastExpression(0, 0, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4"));
        InExpression inExpression = new InExpression(0, 0, left, right, false);
        when(whereSegment.getExpr()).thenReturn(inExpression);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.singletonList("42"));
        ListShardingConditionValue<?> value = (ListShardingConditionValue<?>) actual.get(0).getValues().get(0);
        assertThat(value.getValues(), is(Collections.singletonList(42)));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectBetweenWithCastedParameters() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ExpressionSegment betweenCast = new TypeCastExpression(0, 0, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 0), "int4");
        ExpressionSegment andCast = new TypeCastExpression(0, 0, "?::int4", new ParameterMarkerExpressionSegment(0, 0, 1), "int4");
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, left, betweenCast, andCast, false);
        when(whereSegment.getExpr()).thenReturn(betweenExpression);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Arrays.asList("1", "100"));
        RangeShardingConditionValue<?> value = (RangeShardingConditionValue<?>) actual.get(0).getValues().get(0);
        assertThat(value.getValueRange().lowerEndpoint(), is(1));
        assertThat(value.getValueRange().upperEndpoint(), is(100));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectCompareWithUnsupportedTypmodCastDoesNotRoute() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        TypeCastExpression cast = new TypeCastExpression(0, 0, "?::numeric(3,1)", new ParameterMarkerExpressionSegment(0, 0, 0), "numeric(3,1)");
        BinaryOperationExpression predicate = new BinaryOperationExpression(0, 0, left, cast, "=", "foo_sharding_col = ?::numeric(3,1)");
        when(whereSegment.getExpr()).thenReturn(predicate);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.singletonList(new BigDecimal("1.55")));
        assertTrue(actual.isEmpty());
    }
}
