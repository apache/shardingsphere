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

package org.apache.shardingsphere.sharding.route.engine.condition.generator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueBetweenOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueCompareOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueInOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RouteValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;

import java.util.List;
import java.util.Optional;

/**
 * Condition value generator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConditionValueGeneratorFactory {
    
    /**
     * Generate condition value.
     *
     * @param predicate predicate right value
     * @param column column
     * @param parameters SQL parameters
     * @return route value
     */
    public static Optional<RouteValue> generate(final ExpressionSegment predicate, final Column column, final List<Object> parameters) {
        if (predicate instanceof BinaryOperationExpression) {
            return new ConditionValueCompareOperatorGenerator().generate((BinaryOperationExpression) predicate, column, parameters);
        }
        if (predicate instanceof InExpression) {
            return new ConditionValueInOperatorGenerator().generate((InExpression) predicate, column, parameters);
        }
        if (predicate instanceof BetweenExpression) {
            return new ConditionValueBetweenOperatorGenerator().generate((BetweenExpression) predicate, column, parameters);
        }
        return Optional.empty();
    }
}
