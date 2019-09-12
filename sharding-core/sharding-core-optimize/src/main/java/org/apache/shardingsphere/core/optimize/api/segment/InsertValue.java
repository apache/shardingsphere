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

package org.apache.shardingsphere.core.optimize.api.segment;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.api.segment.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.core.optimize.api.segment.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Insert value.
 *
 * @author panjuan
 * @author zhangliang
 */
@Getter
@ToString
public final class InsertValue {
    
    private final int parametersCount;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final List<Object> parameters;
    
    public InsertValue(final Collection<ExpressionSegment> assignments, final int derivedColumnsCount, final List<Object> parameters, final int parametersOffset) {
        parametersCount = calculateParametersCount(assignments);
        valueExpressions = getValueExpressions(assignments, derivedColumnsCount);
        this.parameters = getParameters(parameters, derivedColumnsCount, parametersOffset);
    }
    
    private int calculateParametersCount(final Collection<ExpressionSegment> assignments) {
        int result = 0;
        for (ExpressionSegment each : assignments) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }
    
    private List<ExpressionSegment> getValueExpressions(final Collection<ExpressionSegment> assignments, final int derivedColumnsCount) {
        List<ExpressionSegment> result = new ArrayList<>(assignments.size() + derivedColumnsCount);
        result.addAll(assignments);
        return result;
    }
    
    private List<Object> getParameters(final List<Object> parameters, final int derivedColumnsCount, final int parametersOffset) {
        if (0 == parametersCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parametersCount + derivedColumnsCount);
        result.addAll(parameters.subList(parametersOffset, parametersOffset + parametersCount));
        return result;
    }
    
    /**
     * Get value.
     *
     * @param index index
     * @return value
     */
    public Object getValue(final int index) {
        ExpressionSegment valueExpression = valueExpressions.get(index);
        return valueExpression instanceof ParameterMarkerExpressionSegment ? parameters.get(getParameterIndex(valueExpression)) : ((LiteralExpressionSegment) valueExpression).getLiterals();
    }
    
    /**
     * Add value.
     *
     * @param value value
     * @param type type of derived value
     */
    public void appendValue(final Object value, final String type) {
        if (parameters.isEmpty()) {
            valueExpressions.add(new DerivedLiteralExpressionSegment(value, type));
        } else {
            valueExpressions.add(new DerivedParameterMarkerExpressionSegment(parameters.size() - 1, type));
            parameters.add(value);
        }
    }
    
    /**
     * Set value.
     *
     * @param index index
     * @param value value
     */
    public void setValue(final int index, final Object value) {
        ExpressionSegment valueExpression = valueExpressions.get(index);
        if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            parameters.set(getParameterIndex(valueExpression), value);
        } else {
            valueExpressions.set(index, new LiteralExpressionSegment(valueExpression.getStartIndex(), valueExpression.getStopIndex(), value));
        }
    }
    
    private int getParameterIndex(final ExpressionSegment valueExpression) {
        int result = 0;
        for (ExpressionSegment each : valueExpressions) {
            if (valueExpression == each) {
                return result;
            }
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        throw new ShardingException("Can not get parameter index.");
    }
}
