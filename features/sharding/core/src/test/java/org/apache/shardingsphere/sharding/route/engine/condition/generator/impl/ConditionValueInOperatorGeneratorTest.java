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

import org.apache.shardingsphere.infra.metadata.database.schema.HashColumn;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionValueInOperatorGeneratorTest {
    
    private final ConditionValueInOperatorGenerator generator = new ConditionValueInOperatorGenerator();
    
    private final HashColumn column = new HashColumn("id", "tbl");
    
    private final TimestampServiceRule timestampServiceRule = new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties()));
    
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Test
    void assertNowExpression() {
        ListExpression listExpression = new ListExpression(0, 0);
        listExpression.getItems().add(new CommonExpressionSegment(0, 0, "now()"));
        InExpression inExpression = new InExpression(0, 0, null, listExpression, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(inExpression, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        assertThat(((ListShardingConditionValue<?>) shardingConditionValue.get()).getValues().iterator().next(), isA(Date.class));
        assertTrue(shardingConditionValue.get().getParameterMarkerIndexes().isEmpty());
    }
    
    @Test
    void assertNullExpression() {
        ListExpression listExpression = new ListExpression(0, 0);
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, null));
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, null));
        InExpression inExpression = new InExpression(0, 0, null, listExpression, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(inExpression, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        assertThat(((ListShardingConditionValue<?>) shardingConditionValue.get()).getValues(), is(Arrays.asList(null, null)));
        assertTrue(shardingConditionValue.get().getParameterMarkerIndexes().isEmpty());
        assertThat(shardingConditionValue.get().toString(), is("tbl.id in (,)"));
    }
    
    @Test
    void assertNullAndCommonExpression() {
        ListExpression listExpression = new ListExpression(0, 0);
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, "test1"));
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, null));
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, null));
        listExpression.getItems().add(new LiteralExpressionSegment(0, 0, "test2"));
        InExpression inExpression = new InExpression(0, 0, null, listExpression, false);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(inExpression, column, new LinkedList<>(), timestampServiceRule);
        assertTrue(shardingConditionValue.isPresent());
        assertThat(((ListShardingConditionValue<?>) shardingConditionValue.get()).getValues(), is(Arrays.asList("test1", null, null, "test2")));
        assertTrue(shardingConditionValue.get().getParameterMarkerIndexes().isEmpty());
        assertThat(shardingConditionValue.get().toString(), is("tbl.id in (test1,,,test2)"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateConditionValueWithParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        InExpression predicate = new InExpression(0, 0, left, right, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, Collections.singletonList(1), timestampServiceRule);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ListShardingConditionValue.class));
        ListShardingConditionValue<Integer> conditionValue = (ListShardingConditionValue<Integer>) actual.get();
        assertThat(conditionValue.getTableName(), is("tbl"));
        assertThat(conditionValue.getColumnName(), is("id"));
        assertThat(conditionValue.getValues(), is(Collections.singletonList(1)));
        assertThat(conditionValue.getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    void assertGenerateConditionValueWithoutParameter() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        InExpression predicate = new InExpression(0, 0, left, right, false);
        Optional<ShardingConditionValue> actual = generator.generate(predicate, column, new LinkedList<>(), timestampServiceRule);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertNotInExpression() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("id"));
        ListExpression right = new ListExpression(0, 0);
        right.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 0));
        InExpression inExpression = new InExpression(0, 0, left, right, true);
        Optional<ShardingConditionValue> shardingConditionValue = generator.generate(inExpression, column, Collections.singletonList(1), timestampServiceRule);
        assertFalse(shardingConditionValue.isPresent());
    }
}
