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
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Condition value generator for in operator.
 */
public final class ConditionValueInOperatorGenerator implements ConditionValueGenerator<InExpression> {
    
    @Override
    public Optional<ShardingConditionValue> generate(final InExpression predicate, final HashColumn column, final List<Object> params, final TimestampServiceRule timestampServiceRule) {
        if (predicate.isNot()) {
            return Optional.empty();
        }
        Collection<ExpressionSegment> expressionSegments = predicate.getExpressionList();
        List<Integer> parameterMarkerIndexes = new ArrayList<>(expressionSegments.size());
        List<Comparable<?>> shardingConditionValues = new LinkedList<>();
        for (ExpressionSegment each : expressionSegments) {
            ConditionValue conditionValue = new ConditionValue(each, params);
            Optional<Comparable<?>> value = conditionValue.getValue();
            if (conditionValue.isNull()) {
                shardingConditionValues.add(null);
                conditionValue.getParameterMarkerIndex().ifPresent(parameterMarkerIndexes::add);
                continue;
            }
            if (value.isPresent()) {
                shardingConditionValues.add(value.get());
                conditionValue.getParameterMarkerIndex().ifPresent(parameterMarkerIndexes::add);
                continue;
            }
            if (ExpressionConditionUtils.isNowExpression(each)) {
                shardingConditionValues.add(timestampServiceRule.getTimestamp());
            }
        }
        return shardingConditionValues.isEmpty() ? Optional.empty()
                : Optional.of(new ListShardingConditionValue<>(column.getName(), column.getTableName(), shardingConditionValues, parameterMarkerIndexes));
    }
}
