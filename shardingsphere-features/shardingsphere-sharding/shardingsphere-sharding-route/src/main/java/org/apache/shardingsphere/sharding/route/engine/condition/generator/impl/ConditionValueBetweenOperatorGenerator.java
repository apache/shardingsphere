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
import org.apache.shardingsphere.sharding.strategy.value.RangeRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.ExpressionConditionUtils;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.sharding.route.spi.SPITimeService;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.util.SafeNumberOperationUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Condition value generator for between operator.
 */
public final class ConditionValueBetweenOperatorGenerator implements ConditionValueGenerator<PredicateBetweenRightValue> {
    
    @Override
    public Optional<RouteValue> generate(final PredicateBetweenRightValue predicateRightValue, final Column column, final List<Object> parameters) {
        Optional<Comparable<?>> betweenRouteValue = new ConditionValue(predicateRightValue.getBetweenExpression(), parameters).getValue();
        Optional<Comparable<?>> andRouteValue = new ConditionValue(predicateRightValue.getAndExpression(), parameters).getValue();
        if (betweenRouteValue.isPresent() && andRouteValue.isPresent()) {
            return Optional.of(new RangeRouteValue<>(column.getName(), column.getTableName(), SafeNumberOperationUtils.safeClosed(betweenRouteValue.get(), andRouteValue.get())));
        }
        Date date = new SPITimeService().getTime();
        if (!betweenRouteValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicateRightValue.getBetweenExpression())) {
            betweenRouteValue = Optional.of(date);
        }
        if (!andRouteValue.isPresent() && ExpressionConditionUtils.isNowExpression(predicateRightValue.getAndExpression())) {
            andRouteValue = Optional.of(date);
        }
        return betweenRouteValue.isPresent() && andRouteValue.isPresent()
                ? Optional.of(new RangeRouteValue<>(column.getName(), column.getTableName(), Range.closed(betweenRouteValue.get(), andRouteValue.get())))
                : Optional.empty();
    }
}
