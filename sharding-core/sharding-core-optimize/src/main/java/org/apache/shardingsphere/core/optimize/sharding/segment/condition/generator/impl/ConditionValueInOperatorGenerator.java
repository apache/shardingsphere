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
import org.apache.shardingsphere.core.optimize.api.segment.Column;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator.ConditionValue;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.generator.ConditionValueGenerator;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Condition value generator for in operator.
 *
 * @author zhangliang
 */
public final class ConditionValueInOperatorGenerator implements ConditionValueGenerator<PredicateInRightValue> {
    
    @Override
    public Optional<RouteValue> generate(final PredicateInRightValue predicateRightValue, final Column column, final List<Object> parameters) {
        List<Comparable> routeValues = new LinkedList<>();
        for (ExpressionSegment each : predicateRightValue.getSqlExpressions()) {
            Optional<Comparable> routeValue = new ConditionValue(each, parameters).getValue();
            if (routeValue.isPresent()) {
                routeValues.add(routeValue.get());
            }
        }
        return routeValues.isEmpty() ? Optional.<RouteValue>absent() : Optional.<RouteValue>of(new ListRouteValue<>(column.getName(), column.getTableName(), routeValues));
    }
}
