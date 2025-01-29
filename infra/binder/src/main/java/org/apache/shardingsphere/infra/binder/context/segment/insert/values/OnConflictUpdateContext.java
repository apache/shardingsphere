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
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * On conflict context.
 */
@Getter
public final class OnConflictUpdateContext {
    
    private final int parameterCount;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final Collection<WhereSegment> whereSegments = new LinkedList<>();
    
    private final Collection<ColumnSegment> columnSegments;
    
    private final List<ParameterMarkerExpressionSegment> parameterMarkerExpressions;
    
    private final List<Object> parameters;
    
    public OnConflictUpdateContext(final Collection<ColumnAssignmentSegment> assignments, final List<Object> params, final int parametersOffset, final Optional<WhereSegment> segment) {
        List<ExpressionSegment> expressionSegments = assignments.stream().map(ColumnAssignmentSegment::getValue).collect(Collectors.toList());
        segment.ifPresent(whereSegments::add);
        for (WhereSegment each : whereSegments) {
            expressionSegments.add(each.getExpr());
        }
        columnSegments = assignments.stream().map(each -> each.getColumns().get(0)).collect(Collectors.toList());
        ColumnExtractor.extractColumnSegments(columnSegments, whereSegments);
        valueExpressions = new ArrayList<>(expressionSegments);
        parameterMarkerExpressions = ExpressionExtractor.getParameterMarkerExpressions(expressionSegments);
        parameterCount = parameterMarkerExpressions.size();
        parameters = getParameters(params, parametersOffset);
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
}
