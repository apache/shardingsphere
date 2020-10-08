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
import org.apache.shardingsphere.sharding.route.datatime.TimeServiceFactory;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.util.SafeNumberOperationUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Condition value generator for between operator.
 */
public final class ConditionValueBetweenOperatorGenerator implements ConditionValueGenerator<BetweenExpression> {
    
    @Override
    public Optional<ShardingConditionValue> generate(final BetweenExpression predicate, final Column column, final List<Object> parameters) {
        Optional<Comparable<?>> betweenConditionValue = new ConditionValue(predicate.getBetweenExpr(), parameters).getValue();
        Optional<Comparable<?>> andConditionValue = new ConditionValue(predicate.getAndExpr(), parameters).getValue();
        if (betweenConditionValue.isPresent() && andConditionValue.isPresent()) {
            return Optional.of(new RangeShardingConditionValue<>(column.getName(), column.getTableName(), SafeNumberOperationUtils.safeClosed(betweenConditionValue.get(), andConditionValue.get())));
        }
        Date date = TimeServiceFactory.newInstance().getTime();
        if (!betweenConditionValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicate.getBetweenExpr())) {
            betweenConditionValue = Optional.of(date);
        }
        if (!andConditionValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicate.getAndExpr())) {
            andConditionValue = Optional.of(date);
        }
        return betweenConditionValue.isPresent() && andConditionValue.isPresent()
                ? Optional.of(new RangeShardingConditionValue<>(column.getName(), column.getTableName(), Range.closed(betweenConditionValue.get(), andConditionValue.get())))
                : Optional.empty();
    }
}
