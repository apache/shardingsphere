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

package org.apache.shardingsphere.infra.binder.context.segment.insert.values;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public final class OnDuplicateUpdateContext {
    
    private final int parameterCount;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final List<ParameterMarkerExpressionSegment> parameterMarkerExpressions;
    
    private final List<Object> parameters;
    
    private final List<ColumnSegment> columns;
    
    public OnDuplicateUpdateContext(final Collection<ColumnAssignmentSegment> assignments, final List<Object> params, final int parametersOffset,
                                    final Collection<ParameterMarkerSegment> parameterMarkers) {
        List<ExpressionSegment> expressionSegments = assignments.stream().map(ColumnAssignmentSegment::getValue).collect(Collectors.toList());
        valueExpressions = getValueExpressions(expressionSegments);
        parameterMarkerExpressions = getParameterMarkerExpressions(expressionSegments, assignments, parameterMarkers);
        parameterCount = parameterMarkerExpressions.size();
        parameters = getParameters(params, parametersOffset);
        columns = assignments.stream().map(each -> each.getColumns().get(0)).collect(Collectors.toList());
    }
    
    private List<ExpressionSegment> getValueExpressions(final Collection<ExpressionSegment> assignments) {
        List<ExpressionSegment> result = new ArrayList<>(assignments.size());
        result.addAll(assignments);
        return result;
    }
    
    private List<ParameterMarkerExpressionSegment> getParameterMarkerExpressions(final Collection<ExpressionSegment> expressionSegments,
                                                                                 final Collection<ColumnAssignmentSegment> assignments,
                                                                                 final Collection<ParameterMarkerSegment> parameterMarkers) {
        List<ParameterMarkerExpressionSegment> result = ExpressionExtractor.getParameterMarkerExpressions(expressionSegments);
        for (ParameterMarkerSegment each : parameterMarkers) {
            if (isInAssignments(each, assignments) && !containsParameterMarker(result, each)) {
                result.add(each instanceof ParameterMarkerExpressionSegment
                        ? (ParameterMarkerExpressionSegment) each
                        : new ParameterMarkerExpressionSegment(each.getStartIndex(), each.getStopIndex(), each.getParameterIndex()));
            }
        }
        result.sort(Comparator.comparingInt(ParameterMarkerExpressionSegment::getParameterMarkerIndex));
        return result;
    }
    
    private boolean isInAssignments(final ParameterMarkerSegment parameterMarkerSegment, final Collection<ColumnAssignmentSegment> assignments) {
        for (ColumnAssignmentSegment each : assignments) {
            if (each.getStartIndex() <= parameterMarkerSegment.getStartIndex() && each.getStopIndex() >= parameterMarkerSegment.getStopIndex()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsParameterMarker(final Collection<ParameterMarkerExpressionSegment> parameterMarkerExpressions, final ParameterMarkerSegment parameterMarkerSegment) {
        for (ParameterMarkerExpressionSegment each : parameterMarkerExpressions) {
            if (each.getParameterMarkerIndex() == parameterMarkerSegment.getParameterIndex()) {
                return true;
            }
        }
        return false;
    }
    
    private List<Object> getParameters(final List<Object> params, final int paramsOffset) {
        if (params.isEmpty() || 0 == parameterCount) {
            return Collections.emptyList();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        result.addAll(params.subList(paramsOffset, paramsOffset + parameterCount));
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
        if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            return parameters.get(getParameterIndex((ParameterMarkerExpressionSegment) valueExpression));
        }
        if (valueExpression instanceof FunctionSegment) {
            return valueExpression;
        }
        return ((LiteralExpressionSegment) valueExpression).getLiterals();
    }
    
    private int getParameterIndex(final ParameterMarkerExpressionSegment paramMarkerExpression) {
        int result = parameterMarkerExpressions.indexOf(paramMarkerExpression);
        Preconditions.checkArgument(result >= 0, "Can not get parameter index.");
        return result;
    }
    
    /**
     * Get on duplicate key update column by index of this clause.
     *
     * @param index index
     * @return columnSegment
     */
    public ColumnSegment getColumn(final int index) {
        return columns.get(index);
    }
}
