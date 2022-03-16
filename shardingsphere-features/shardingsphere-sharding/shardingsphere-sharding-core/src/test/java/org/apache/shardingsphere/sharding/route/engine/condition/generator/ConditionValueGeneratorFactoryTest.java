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

import org.apache.shardingsphere.infra.datetime.DatetimeService;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueBetweenOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueCompareOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueInOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.*;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConditionValueGeneratorFactoryTest {

    private final Column column = new Column("id", "tbl");

    @Before
    public void setup() {
        ShardingSphereServiceLoader.register(DatetimeService.class);
    }

    @Test
    public void assertGenerateBinaryOperationExpression() {
        ConditionValueCompareOperatorGenerator conditionValueCompareOperatorGenerator = new ConditionValueCompareOperatorGenerator();
        BinaryOperationExpression rightValue = new BinaryOperationExpression(0, 0, mock(ColumnSegment.class), new LiteralExpressionSegment(0, 0, 1), "=", null);
        Optional<ShardingConditionValue> expected = conditionValueCompareOperatorGenerator.generate(rightValue, column, new LinkedList<>());
        Optional<ShardingConditionValue> conditionValueGeneratorFactory = ConditionValueGeneratorFactory.generate(rightValue, column, new LinkedList<>());
        assertTrue(conditionValueGeneratorFactory.isPresent() && expected.isPresent());
        assertThat(expected.get().getTableName(), is(conditionValueGeneratorFactory.get().getTableName()));
        assertThat(expected.get().getColumnName(), is(conditionValueGeneratorFactory.get().getColumnName()));
    }

    @Test
    public void assertGenerateInOperationExpression() {
        ConditionValueInOperatorGenerator conditionValueInOperatorGenerator = new ConditionValueInOperatorGenerator();
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        Optional<ShardingConditionValue> expected = conditionValueInOperatorGenerator.generate(new InExpression(0, 0, left, right, false), column, Collections.singletonList(1));
        Optional<ShardingConditionValue> conditionValueGeneratorFactory = ConditionValueGeneratorFactory.generate(new InExpression(0, 0, left, right, false), column, Collections.singletonList(1));
        assertTrue(expected.isPresent() && conditionValueGeneratorFactory.isPresent());
        assertThat(expected.get().getColumnName(), is(conditionValueGeneratorFactory.get().getColumnName()));
        assertThat(expected.get().getTableName(), is(conditionValueGeneratorFactory.get().getTableName()));
    }

    @Test
    public void assertGenerateBetweenExpression() {
        ConditionValueBetweenOperatorGenerator conditionValueBetweenOperatorGenerator = new ConditionValueBetweenOperatorGenerator();
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, 1);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, 2);
        Optional<ShardingConditionValue> expected = conditionValueBetweenOperatorGenerator.generate(new BetweenExpression(0, 0, null, betweenSegment, andSegment, false), column, new LinkedList<>());
        Optional<ShardingConditionValue> conditionValGenFactory = ConditionValueGeneratorFactory.generate(new BetweenExpression(0, 0, null, betweenSegment, andSegment, false), column, new LinkedList<>());
        assertTrue(expected.isPresent() && conditionValGenFactory.isPresent());
        assertThat(expected.get().getColumnName(), is(conditionValGenFactory.get().getColumnName()));
        assertThat(expected.get().getTableName(), is(conditionValGenFactory.get().getTableName()));
    }
}
