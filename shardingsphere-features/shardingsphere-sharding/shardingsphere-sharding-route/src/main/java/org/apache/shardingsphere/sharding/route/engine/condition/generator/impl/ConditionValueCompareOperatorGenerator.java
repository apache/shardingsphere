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

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.sharding.route.datatime.SPITimeService;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Condition value generator for compare operator.
 */
public final class ConditionValueCompareOperatorGenerator implements ConditionValueGenerator<BinaryOperationExpression> {
    
    private static final String EQUAL = "=";
    
    private static final String GREATER_THAN = ">";
    
    private static final String LESS_THAN = "<";
    
    private static final String AT_MOST = "<=";
    
    private static final String AT_LEAST = ">=";
    
    private static final List<String> OPERATORS = Arrays.asList(EQUAL, GREATER_THAN, LESS_THAN, AT_LEAST, AT_MOST);
    
    @Override
    public Optional<ShardingConditionValue> generate(final BinaryOperationExpression predicate, final Column column, final List<Object> parameters) {
        String operator = predicate.getOperator();
        if (!isSupportedOperator(operator)) {
            return Optional.empty();
        }
        Optional<Comparable<?>> conditionValue = new ConditionValue(predicate.getRight(), parameters).getValue();
        if (conditionValue.isPresent()) {
            return generate(conditionValue.get(), column, operator);
        }
        if (ExpressionConditionUtils.isNowExpression(predicate.getRight())) {
            return generate(new SPITimeService().getTime(), column, operator);
        }
        return Optional.empty();
    }
    
    private Optional<ShardingConditionValue> generate(final Comparable<?> comparable, final Column column, final String operator) {
        String columnName = column.getName();
        String tableName = column.getTableName();
        switch (operator) {
            case EQUAL:
                return Optional.of(new ListShardingConditionValue<>(columnName, tableName, Lists.newArrayList(comparable)));
            case GREATER_THAN:
                return Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.greaterThan(comparable)));
            case LESS_THAN:
                return Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.lessThan(comparable)));
            case AT_MOST:
                return Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.atMost(comparable)));
            case AT_LEAST:
                return Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.atLeast(comparable)));
            default:
                return Optional.empty();
        }
    }
    
    private boolean isSupportedOperator(final String operator) {
        return OPERATORS.contains(operator);
    }
}
