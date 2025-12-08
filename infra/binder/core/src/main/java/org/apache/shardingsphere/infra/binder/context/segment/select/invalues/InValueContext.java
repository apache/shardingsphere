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

package org.apache.shardingsphere.infra.binder.context.segment.select.invalues;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * IN value context.
 * Stores structural information about IN expression, similar to InsertValueContext.
 */
@Getter
@ToString
public final class InValueContext {
    
    private final InExpression inExpression;
    
    private final int parameterCount;
    
    private final int parametersOffset;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final List<ParameterMarkerExpressionSegment> parameterMarkerExpressions;
    
    private final List<Object> parameters;
    
    /**
     * Constructor for IN expression.
     *
     * @param inExpression IN expression segment
     * @param params all parameters
     * @param parametersOffset parameter offset
     */
    public InValueContext(final InExpression inExpression, final List<Object> params, final int parametersOffset) {
        this.inExpression = inExpression;
        this.parametersOffset = parametersOffset;
        this.valueExpressions = getValueExpressions(inExpression);
        this.parameterMarkerExpressions = ExpressionExtractor.getParameterMarkerExpressions(valueExpressions);
        this.parameterCount = parameterMarkerExpressions.size();
        this.parameters = getParameters(params, parametersOffset);
    }
    
    private List<ExpressionSegment> getValueExpressions(final InExpression inExpression) {
        ExpressionSegment right = inExpression.getRight();
        if (right instanceof ListExpression) {
            List<ExpressionSegment> result = new ArrayList<>(((ListExpression) right).getItems().size());
            result.addAll(((ListExpression) right).getItems());
            return result;
        }
        return Collections.emptyList();
    }
    
    private List<Object> getParameters(final List<Object> params, final int parametersOffset) {
        if (null == params || params.isEmpty() || 0 == parameterCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        result.addAll(params.subList(parametersOffset, parametersOffset + parameterCount));
        return result;
    }
    
    /**
     * Get grouped parameters for IN query (each IN value is a group).
     * Similar to InsertValueContext.getParameters().
     * Only returns parameterized values, not literal values.
     *
     * @return grouped parameters, each inner list contains one IN value
     */
    public List<List<Object>> getGroupedParameters() {
        List<List<Object>> result = new ArrayList<>(parameterCount);
        int paramIndex = 0;
        for (ExpressionSegment each : valueExpressions) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                if (paramIndex < parameters.size()) {
                    result.add(Collections.singletonList(parameters.get(paramIndex++)));
                }
            }
        }
        return result;
    }
}
