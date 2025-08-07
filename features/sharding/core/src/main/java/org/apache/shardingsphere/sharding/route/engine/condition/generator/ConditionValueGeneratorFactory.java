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
import org.apache.shardingsphere.infra.metadata.database.schema.HashColumn;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueBetweenOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueCompareOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.impl.ConditionValueInOperatorGenerator;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;

import java.util.List;
import java.util.Optional;

/**
 * Condition value generator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConditionValueGeneratorFactory {
    
    private static final ConditionValueCompareOperatorGenerator COMPARE_OPERATOR_GENERATOR = new ConditionValueCompareOperatorGenerator();
    
    private static final ConditionValueInOperatorGenerator IN_OPERATOR_GENERATOR = new ConditionValueInOperatorGenerator();
    
    private static final ConditionValueBetweenOperatorGenerator BETWEEN_OPERATOR_GENERATOR = new ConditionValueBetweenOperatorGenerator();
    
    /**
     * Generate condition value.
     *
     * @param predicate predicate right value
     * @param column column
     * @param params SQL parameters
     * @param timestampServiceRule time service rule
     * @return route value
     */
    public static Optional<ShardingConditionValue> generate(final ExpressionSegment predicate, final HashColumn column, final List<Object> params,
                                                            final TimestampServiceRule timestampServiceRule) {
        if (predicate instanceof BinaryOperationExpression) {
            return COMPARE_OPERATOR_GENERATOR.generate((BinaryOperationExpression) predicate, column, params, timestampServiceRule);
        }
        if (predicate instanceof InExpression) {
            return IN_OPERATOR_GENERATOR.generate((InExpression) predicate, column, params, timestampServiceRule);
        }
        if (predicate instanceof BetweenExpression) {
            return BETWEEN_OPERATOR_GENERATOR.generate((BetweenExpression) predicate, column, params, timestampServiceRule);
        }
        return Optional.empty();
    }
}
