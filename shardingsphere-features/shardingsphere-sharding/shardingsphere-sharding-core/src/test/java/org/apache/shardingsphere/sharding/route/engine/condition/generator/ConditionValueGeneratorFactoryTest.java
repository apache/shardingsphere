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

package org.apache.shardingsphere.sharding.route.engine.condition.generator;

import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueBetweenOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueCompareOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueInOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConditionValueGeneratorFactoryTest {
    
    private final Column column = new Column("id", "tbl");
    
    @Test
    public void assertGenerateBinaryOperationExpression() {
        ConditionValueCompareOperatorGenerator conditionValueCompareOperatorGenerator = new ConditionValueCompareOperatorGenerator();
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), "=", null);
        Optional<ShardingConditionValue> actual = conditionValueCompareOperatorGenerator.generate(rightValue, column, new LinkedList<>());
        Optional<ShardingConditionValue> expected = ConditionValueGeneratorFactory.generate(rightValue, column, new LinkedList<>());
        assertTrue(actual.isPresent() && expected.isPresent());
        assertThat(actual.get().getTableName(), is(expected.get().getTableName()));
        assertThat(actual.get().getColumnName(), is(expected.get().getColumnName()));
    }
    
    @Test
    public void assertGenerateInOperationExpression() {
        ConditionValueInOperatorGenerator conditionValueInOperatorGenerator = new ConditionValueInOperatorGenerator();
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        Optional<ShardingConditionValue> actual = conditionValueInOperatorGenerator.generate(new InExpression(0, 0, left, right, false), column, Collections.singletonList(1));
        Optional<ShardingConditionValue> expected = ConditionValueGeneratorFactory.generate(new InExpression(0, 0, left, right, false), column, Collections.singletonList(1));
        assertTrue(actual.isPresent() && expected.isPresent());
        assertThat(actual.get().getColumnName(), is(expected.get().getColumnName()));
        assertThat(actual.get().getTableName(), is(expected.get().getTableName()));
    }
    
    @Test
    public void assertGenerateBetweenExpression() {
        ConditionValueBetweenOperatorGenerator conditionValueBetweenOperatorGenerator = new ConditionValueBetweenOperatorGenerator();
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, 1);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, 2);
        Optional<ShardingConditionValue> actual = conditionValueBetweenOperatorGenerator.generate(new BetweenExpression(0, 0, null, betweenSegment, andSegment, false), column, new LinkedList<>());
        Optional<ShardingConditionValue> expected = ConditionValueGeneratorFactory.generate(new BetweenExpression(0, 0, null, betweenSegment, andSegment, false), column, new LinkedList<>());
        assertTrue(actual.isPresent() && expected.isPresent());
        assertThat(actual.get().getColumnName(), is(expected.get().getColumnName()));
        assertThat(actual.get().getTableName(), is(expected.get().getTableName()));
    }
}
