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

import com.google.common.collect.Range;
import org.apache.shardingsphere.infra.metadata.database.schema.HashColumn;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    
    private static final String IS = "IS";
    
    private static final Collection<String> OPERATORS = new HashSet<>(Arrays.asList(EQUAL, GREATER_THAN, LESS_THAN, AT_LEAST, AT_MOST, IS));
    
    @Override
    public Optional<ShardingConditionValue> generate(final BinaryOperationExpression predicate, final HashColumn column, final List<Object> params,
                                                     final TimestampServiceRule timestampServiceRule) {
        String operator = predicate.getOperator().toUpperCase();
        if (!isSupportedOperator(operator)) {
            return Optional.empty();
        }
        ExpressionSegment valueExpression = predicate.getLeft() instanceof ColumnSegment ? predicate.getRight() : predicate.getLeft();
        ConditionValue conditionValue = new ConditionValue(valueExpression, params);
        if (conditionValue.isNull()) {
            return generate(null, column, operator, conditionValue.getParameterMarkerIndex().orElse(-1));
        }
        Optional<Comparable<?>> value = conditionValue.getValue();
        if (value.isPresent()) {
            return generate(value.get(), column, operator, conditionValue.getParameterMarkerIndex().orElse(-1));
        }
        if (ExpressionConditionUtils.isNowExpression(valueExpression)) {
            return generate(timestampServiceRule.getTimestamp(), column, operator, -1);
        }
        return Optional.empty();
    }
    
    private Optional<ShardingConditionValue> generate(final Comparable<?> comparable, final HashColumn column, final String operator, final int parameterMarkerIndex) {
        String columnName = column.getName();
        String tableName = column.getTableName();
        List<Integer> parameterMarkerIndexes = parameterMarkerIndex > -1 ? Collections.singletonList(parameterMarkerIndex) : Collections.emptyList();
        switch (operator) {
            case EQUAL:
                return Optional.of(new ListShardingConditionValue<>(columnName, tableName, new ArrayList<>(Collections.singleton(comparable)), parameterMarkerIndexes));
            case GREATER_THAN:
                return null == comparable ? Optional.empty() : Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.greaterThan(comparable), parameterMarkerIndexes));
            case LESS_THAN:
                return null == comparable ? Optional.empty() : Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.lessThan(comparable), parameterMarkerIndexes));
            case AT_MOST:
                return null == comparable ? Optional.empty() : Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.atMost(comparable), parameterMarkerIndexes));
            case AT_LEAST:
                return null == comparable ? Optional.empty() : Optional.of(new RangeShardingConditionValue<>(columnName, tableName, Range.atLeast(comparable), parameterMarkerIndexes));
            case IS:
                return "null".equalsIgnoreCase(String.valueOf(comparable))
                        ? Optional.of(new ListShardingConditionValue<>(columnName, tableName, new ArrayList<>(Collections.singleton(null)), parameterMarkerIndexes))
                        : Optional.empty();
            default:
                return Optional.empty();
        }
    }
    
    private boolean isSupportedOperator(final String operator) {
        return OPERATORS.contains(operator);
    }
}
