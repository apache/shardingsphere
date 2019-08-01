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

package org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Range;
import org.apache.shardingsphere.core.optimize.api.segment.Column;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator.ConditionValue;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.List;

/**
 * Condition value generator for between operator.
 *
 * @author zhangliang
 */
public final class ConditionValueBetweenOperatorGenerator implements ConditionValueGenerator<PredicateBetweenRightValue> {
    
    @Override
    public Optional<RouteValue> generate(final PredicateBetweenRightValue predicateRightValue, final Column column, final List<Object> parameters) {
        Optional<Comparable> betweenRouteValue = new ConditionValue(predicateRightValue.getBetweenExpression(), parameters).getValue();
        Optional<Comparable> andRouteValue = new ConditionValue(predicateRightValue.getAndExpression(), parameters).getValue();
        return betweenRouteValue.isPresent() && andRouteValue.isPresent()
                ? Optional.<RouteValue>of(new RangeRouteValue<>(column.getName(), column.getTableName(), Range.closed(betweenRouteValue.get(), andRouteValue.get()))) : Optional.<RouteValue>absent();
    }
}
