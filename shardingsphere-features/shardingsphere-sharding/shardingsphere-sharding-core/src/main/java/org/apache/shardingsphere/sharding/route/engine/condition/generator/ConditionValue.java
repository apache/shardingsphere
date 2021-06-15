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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;
import java.util.Optional;

/**
 * Condition value.
 */
public final class ConditionValue {
    
    private final Comparable<?> value;
    
    public ConditionValue(final ExpressionSegment expressionSegment, final List<Object> parameters) {
        value = getValue(expressionSegment, parameters);
    }
    
    private Comparable<?> getValue(final ExpressionSegment expressionSegment, final List<Object> parameters) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return getValue((ParameterMarkerExpressionSegment) expressionSegment, parameters);
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return getValue((LiteralExpressionSegment) expressionSegment);
        }
        return null;
    }
    
    private Comparable<?> getValue(final ParameterMarkerExpressionSegment expressionSegment, final List<Object> parameters) {
        Object result = parameters.get(expressionSegment.getParameterMarkerIndex());
        Preconditions.checkArgument(result instanceof Comparable, "Sharding value must implements Comparable.");
        return (Comparable<?>) result;
    }
    
    private Comparable<?> getValue(final LiteralExpressionSegment expressionSegment) {
        Object result = expressionSegment.getLiterals();
        Preconditions.checkArgument(result instanceof Comparable, "Sharding value must implements Comparable.");
        return (Comparable<?>) result;
    }
    
    /**
     * Get condition value.
     * 
     * @return condition value
     */
    public Optional<Comparable<?>> getValue() {
        return Optional.ofNullable(value);
    }
}
