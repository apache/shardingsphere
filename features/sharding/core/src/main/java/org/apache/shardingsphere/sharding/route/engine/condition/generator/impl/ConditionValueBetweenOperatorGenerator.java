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
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.util.SafeNumberOperationUtils;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Condition value generator for between operator.
 */
public final class ConditionValueBetweenOperatorGenerator implements ConditionValueGenerator<BetweenExpression> {
    
    @Override
    public Optional<ShardingConditionValue> generate(final BetweenExpression predicate, final HashColumn column, final List<Object> params, final TimestampServiceRule timestampServiceRule) {
        ConditionValue betweenConditionValue = new ConditionValue(predicate.getBetweenExpr(), params);
        ConditionValue andConditionValue = new ConditionValue(predicate.getAndExpr(), params);
        Optional<Comparable<?>> betweenValue = betweenConditionValue.getValue();
        Optional<Comparable<?>> andValue = andConditionValue.getValue();
        List<Integer> parameterMarkerIndexes = new ArrayList<>(2);
        betweenConditionValue.getParameterMarkerIndex().ifPresent(parameterMarkerIndexes::add);
        andConditionValue.getParameterMarkerIndex().ifPresent(parameterMarkerIndexes::add);
        if (betweenValue.isPresent() && andValue.isPresent()) {
            return Optional.of(new RangeShardingConditionValue<>(column.getName(), column.getTableName(), SafeNumberOperationUtils.safeClosed(betweenValue.get(), andValue.get()),
                    parameterMarkerIndexes));
        }
        Timestamp timestamp = timestampServiceRule.getTimestamp();
        if (!betweenValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicate.getBetweenExpr())) {
            betweenValue = Optional.of(timestamp);
        }
        if (!andValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicate.getAndExpr())) {
            andValue = Optional.of(timestamp);
        }
        if (!betweenValue.isPresent() || !andValue.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new RangeShardingConditionValue<>(column.getName(), column.getTableName(), Range.closed(betweenValue.get(), andValue.get()), parameterMarkerIndexes));
    }
}
