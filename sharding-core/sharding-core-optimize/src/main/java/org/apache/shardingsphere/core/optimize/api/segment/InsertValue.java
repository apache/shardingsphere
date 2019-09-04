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
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert value.
 *
 * @author panjuan
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString(exclude = "dataNodes")
public final class InsertValue {
    
    private final List<String> columnNames;
    
    private final int parametersCount;
    
    private final List<ExpressionSegment> valueExpressions;
    
    private final List<Object> parameters;
    
    private final List<DataNode> dataNodes = new LinkedList<>();
    
    public InsertValue(final List<String> columnNames, final Collection<ExpressionSegment> assignments, final int derivedColumnsCount, final List<Object> parameters, final int parametersOffset) {
        this.columnNames = columnNames;
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
     * @param columnName column name
     * @return value
     */
    public Object getValue(final String columnName) {
        ExpressionSegment valueExpression = valueExpressions.get(columnNames.indexOf(columnName));
        return valueExpression instanceof ParameterMarkerExpressionSegment ? parameters.get(getParameterIndex(valueExpression)) : ((LiteralExpressionSegment) valueExpression).getLiterals();
    }
    
    /**
     * Get value expression.
     *
     * @param columnName column name
     * @return column sql expression
     */
    public ExpressionSegment getValueExpression(final String columnName) {
        return valueExpressions.get(columnNames.indexOf(columnName));
    }
    
    /**
     * Append value.
     * 
     * @param value value
     * @param parameters SQL parameters
     */
    @Deprecated
    public void appendValue(final Comparable<?> value, final List<Object> parameters) {
        if (parameters.isEmpty()) {
            // TODO fix start index and stop index
            valueExpressions.add(new LiteralExpressionSegment(0, 0, value));
        } else {
            // TODO fix start index and stop index
            valueExpressions.add(new ParameterMarkerExpressionSegment(0, 0, parameters.size() - 1));
            this.parameters.add(value);
        }
    }
    
    /**
     * Add value.
     *
     * @param value value
     */
    public void appendValue(final Object value) {
        if (parameters.isEmpty()) {
            // TODO fix start index and stop index
            valueExpressions.add(new LiteralExpressionSegment(0, 0, value));
        } else {
            // TODO fix start index and stop index
            valueExpressions.add(new ParameterMarkerExpressionSegment(0, 0, parameters.size() - 1));
            parameters.add(value);
        }
    }
    
    /**
     * Set value.
     *
     * @param columnName column name
     * @param value value
     */
    public void setValue(final String columnName, final Object value) {
        ExpressionSegment expressionSegment = valueExpressions.get(columnNames.indexOf(columnName));
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            parameters.set(getParameterIndex(expressionSegment), value);
        } else {
            valueExpressions.set(columnNames.indexOf(columnName), new LiteralExpressionSegment(expressionSegment.getStartIndex(), expressionSegment.getStopIndex(), value));
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
