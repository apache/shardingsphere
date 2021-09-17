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

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.statement.InsertContextExpressSegmentUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Insert value context.
 */
@Getter
@ToString
public final class InsertValueContext {
    
    private final int parameterCount;
    
    private final List<ExpressionSegment> valueExpressions;

    private final List<ParameterMarkerExpressionSegment> parametersValueExpressions;
    
    private final List<Object> parameters;
    
    public InsertValueContext(final Collection<ExpressionSegment> assignments, final List<Object> parameters, final int parametersOffset) {
        valueExpressions = getValueExpressions(assignments);
        parametersValueExpressions = InsertContextExpressSegmentUtil.extractParameterMarkerExpressionSegment(assignments);
        parameterCount = parametersValueExpressions.size();
        this.parameters = getParameters(parameters, parametersOffset);
    }
    
    private List<ExpressionSegment> getValueExpressions(final Collection<ExpressionSegment> assignments) {
        List<ExpressionSegment> result = new ArrayList<>(assignments.size());
        result.addAll(assignments);
        return result;
    }
    
    private List<Object> getParameters(final List<Object> parameters, final int parametersOffset) {
        if (0 == parameterCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        result.addAll(parameters.subList(parametersOffset, parametersOffset + parameterCount));
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
        if (parametersValueExpressions.contains(valueExpression)) {
            return parameters.get(parametersValueExpressions.indexOf(valueExpression));
        } else {
            return ((LiteralExpressionSegment) valueExpression).getLiterals();
        }
    }

    /**
     * Get parameter index via column index.
     *
     * @param index column index
     * @return parameter index
     */
    public int getParameterIndex(final int index) {
        ExpressionSegment valueExpression = valueExpressions.get(index);
        return parametersValueExpressions.indexOf(valueExpression);
    }
}
