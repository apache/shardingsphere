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

package org.apache.shardingsphere.infra.binder.segment.insert.values;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Insert value context.
 */
@Getter
@ToString
public final class InsertValueContext {
    
    private final int parameterCount;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final List<ParameterMarkerExpressionSegment> parameterMarkerExpressions;
    
    private final List<Object> parameters;
    
    public InsertValueContext(final Collection<ExpressionSegment> assignments, final List<Object> parameters, final int parametersOffset) {
        valueExpressions = getValueExpressions(assignments);
        parameterMarkerExpressions = ExpressionExtractUtil.getParameterMarkerExpressions(assignments);
        parameterCount = parameterMarkerExpressions.size();
        this.parameters = getParameters(parameters, parametersOffset);
    }
    
    private List<ExpressionSegment> getValueExpressions(final Collection<ExpressionSegment> assignments) {
        List<ExpressionSegment> result = new ArrayList<>(assignments.size());
        result.addAll(assignments);
        return result;
    }
    
    private List<Object> getParameters(final List<Object> parameters, final int parametersOffset) {
        if (parameters.isEmpty() || 0 == parameterCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        result.addAll(parameters.subList(parametersOffset, parametersOffset + parameterCount));
        return result;
    }
    
    /**
     * Get literal value.
     *
     * @param index index
     * @return literal value
     */
    public Optional<Object> getLiteralValue(final int index) {
        ExpressionSegment valueExpression = valueExpressions.get(index);
        if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(parameters.get(getParameterIndex((ParameterMarkerExpressionSegment) valueExpression)));
        }
        if (valueExpression instanceof LiteralExpressionSegment) {
            return Optional.of(((LiteralExpressionSegment) valueExpression).getLiterals());
        }
        return Optional.empty();
    }
    
    private int getParameterIndex(final ParameterMarkerExpressionSegment parameterMarkerExpression) {
        int parameterIndex = parameterMarkerExpressions.indexOf(parameterMarkerExpression);
        Preconditions.checkArgument(parameterIndex >= 0, "Can not get parameter index.");
        return parameterIndex;
    }
    
    /**
     * Get parameter index via column index.
     *
     * @param index column index
     * @return parameter index
     */
    public int getParameterIndex(final int index) {
        ExpressionSegment valueExpression = valueExpressions.get(index);
        return valueExpression instanceof ParameterMarkerExpressionSegment ? getParameterIndex((ParameterMarkerExpressionSegment) valueExpression) : -1;
    }
}
