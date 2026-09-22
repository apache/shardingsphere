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
import org.apache.shardingsphere.sharding.route.engine.condition.AlwaysFalseShardingCondition;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("heterogeneousNumberArguments")
    void assertCreateShardingConditionsForHeterogeneousNumbers(final String name, final Comparable<?> firstValue, final Comparable<?> secondValue) {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        BinaryOperationExpression firstPredicate = new BinaryOperationExpression(0, 0, column, new ParameterMarkerExpressionSegment(0, 0, 0), "=", null);
        BinaryOperationExpression secondPredicate = new BinaryOperationExpression(0, 0, column, new ParameterMarkerExpressionSegment(0, 0, 1), "=", null);
        when(whereSegment.getExpr()).thenReturn(new BinaryOperationExpression(0, 0, firstPredicate, secondPredicate, "AND", null));
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<Object> params = Arrays.asList(firstValue, secondValue);
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, params);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getValues().size(), is(1));
        assertThat(actual.get(0).getValues().get(0), isA(ListShardingConditionValue.class));
        ListShardingConditionValue<?> actualValue = (ListShardingConditionValue<?>) actual.get(0).getValues().get(0);
        assertThat(actualValue.getValues().size(), is(1));
        Number actualNumber = (Number) actualValue.getValues().iterator().next();
        assertThat(new BigDecimal(actualNumber.toString()).compareTo(new BigDecimal("9007199254740993")), is(0));
    }
    
    private static Stream<Arguments> heterogeneousNumberArguments() {
        return Stream.of(
                Arguments.of("Long then BigDecimal", 9007199254740993L, new BigDecimal("9007199254740993")),
                Arguments.of("BigDecimal then Long", new BigDecimal("9007199254740993"), 9007199254740993L),
                Arguments.of("Long then scaled BigDecimal", 9007199254740993L, new BigDecimal("9007199254740993.0")),
                Arguments.of("Scaled BigDecimal then Long", new BigDecimal("9007199254740993.0"), 9007199254740993L));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("finiteAndInfiniteNumberArguments")
    void assertCreateAlwaysFalseShardingConditionForFiniteAndInfiniteNumbers(final String name, final Comparable<?> firstValue, final Comparable<?> secondValue) {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        BinaryOperationExpression firstPredicate = new BinaryOperationExpression(0, 0, column, new ParameterMarkerExpressionSegment(0, 0, 0), "=", null);
        BinaryOperationExpression secondPredicate = new BinaryOperationExpression(0, 0, column, new ParameterMarkerExpressionSegment(0, 0, 1), "=", null);
        when(whereSegment.getExpr()).thenReturn(new BinaryOperationExpression(0, 0, firstPredicate, secondPredicate, "AND", null));
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<Object> params = Arrays.asList(firstValue, secondValue);
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, params);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), isA(AlwaysFalseShardingCondition.class));
    }
    
    private static Stream<Arguments> finiteAndInfiniteNumberArguments() {
        BigDecimal largeDecimal = new BigDecimal("1E10000");
        BigInteger largeInteger = BigInteger.TEN.pow(10000);
        return Stream.of(
                Arguments.of("Positive BigDecimal then positive infinity", largeDecimal, Double.POSITIVE_INFINITY),
                Arguments.of("Positive infinity then positive BigDecimal", Double.POSITIVE_INFINITY, largeDecimal),
                Arguments.of("Negative BigDecimal then negative infinity", largeDecimal.negate(), Double.NEGATIVE_INFINITY),
                Arguments.of("Negative infinity then negative BigDecimal", Double.NEGATIVE_INFINITY, largeDecimal.negate()),
                Arguments.of("Positive BigInteger then positive infinity", largeInteger, Double.POSITIVE_INFINITY),
                Arguments.of("Positive infinity then positive BigInteger", Double.POSITIVE_INFINITY, largeInteger),
                Arguments.of("Negative BigInteger then negative infinity", largeInteger.negate(), Double.NEGATIVE_INFINITY),
                Arguments.of("Negative infinity then negative BigInteger", Double.NEGATIVE_INFINITY, largeInteger.negate()));
    }
    
    @Test
    void assertCreateEmptyShardingConditionsForBinaryOperatorRangeStatement() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ExpressionSegment binaryColumn = new UnaryOperationExpression(0, 0, left, "BINARY", "BINARY foo_sharding_col");
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, binaryColumn, new LiteralExpressionSegment(0, 0, "100"), ">", null);
        when(whereSegment.getExpr()).thenReturn(expression);
        when(rule.findShardingColumn("foo_sharding_col", "")).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertTrue(actual.isEmpty());
    }
}
