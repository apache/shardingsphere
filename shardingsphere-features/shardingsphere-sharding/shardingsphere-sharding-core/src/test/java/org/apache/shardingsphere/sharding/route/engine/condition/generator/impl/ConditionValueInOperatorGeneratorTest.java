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

package org.apache.shardingsphere.sharding.route.engine.condition.generator.impl;

import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ConditionValueInOperatorGeneratorTest {
    
    private final ConditionValueInOperatorGenerator generator = new ConditionValueInOperatorGenerator();
    
    private final Column column = new Column("id", "tbl");
    
    @Test
    public void assertNowExpression() {
        ListExpression listExpression = new ListExpression(0, 0);
        listExpression.getItems().add(new CommonExpressionSegment(0, 0, "now()"));
        InExpression inExpression = new InExpression(0, 0, null, listExpression, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(inExpression, column, new LinkedList<>());
        assertTrue(shardingConditionValue.isPresent());
        assertThat(((ListShardingConditionValue<?>) shardingConditionValue.get()).getValues().iterator().next(), instanceOf(Date.class));
        assertTrue(shardingConditionValue.get().getParameterMarkerIndexes().isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateConditionValueWithParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        InExpression predicate = new InExpression(0, 0, left, right, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ListShardingConditionValue.class));
        ListShardingConditionValue<Integer> conditionValue = (ListShardingConditionValue<Integer>) actual.get();
        assertThat(conditionValue.getTableName(), is("tbl"));
        assertThat(conditionValue.getColumnName(), is("id"));
        assertThat(conditionValue.getValues(), is(Collections.singletonList(1)));
        assertThat(conditionValue.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    public void assertGenerateConditionValueWithoutParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        InExpression predicate = new InExpression(0, 0, left, right, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, new LinkedList<>());
        assertFalse(actual.isPresent());
    }
}
