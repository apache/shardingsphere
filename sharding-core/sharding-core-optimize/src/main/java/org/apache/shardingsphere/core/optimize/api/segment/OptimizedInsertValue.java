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

import java.util.LinkedList;
import java.util.List;

/**
 * Optimized insert value.
 *
 * @author panjuan
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString(exclude = "dataNodes")
public final class OptimizedInsertValue {
    
    private final List<String> columnNames;
    
    private final ExpressionSegment[] valueExpressions;
    
    private final Object[] parameters;
    
    private final int startIndexOfAppendedParameters;
    
    private final List<DataNode> dataNodes = new LinkedList<>();
    
    /**
     * Get value.
     *
     * @param columnName column name
     * @return value
     */
    public Object getValue(final String columnName) {
        ExpressionSegment valueExpression = valueExpressions[columnNames.indexOf(columnName)];
        return valueExpression instanceof ParameterMarkerExpressionSegment ? parameters[getParameterIndex(valueExpression)] : ((LiteralExpressionSegment) valueExpression).getLiterals();
    }
    
    /**
     * Get value expression.
     *
     * @param columnName column name
     * @return column sql expression
     */
    public ExpressionSegment getValueExpression(final String columnName) {
        return valueExpressions[columnNames.indexOf(columnName)];
    }
    
    /**
     * Append value.
     * 
     * @param value value
     * @param parameters SQL parameters
     */
    public void appendValue(final Comparable<?> value, final List<Object> parameters) {
        if (parameters.isEmpty()) {
            // TODO fix start index and stop index
            appendValueExpression(new LiteralExpressionSegment(0, 0, value));
        } else {
            // TODO fix start index and stop index
            appendValueExpression(new ParameterMarkerExpressionSegment(0, 0, parameters.size() - 1));
            appendParameter(value);
        }
    }
    
    private void appendValueExpression(final ExpressionSegment expressionSegment) {
        valueExpressions[getNullIndex(valueExpressions, 0)] = expressionSegment;
    }
    
    private void appendParameter(final Object parameter) {
        parameters[getNullIndex(parameters, startIndexOfAppendedParameters)] = parameter;
    }
    
    private int getNullIndex(final Object[] array, final int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (null == array[i]) {
                return i;
            }
        }
        throw new ShardingException("Index Out Of Bounds For InsertOptimizeResultUnit.");
    }
    
    /**
     * Set value.
     *
     * @param columnName column name
     * @param value value
     */
    public void setValue(final String columnName, final Object value) {
        ExpressionSegment expressionSegment = valueExpressions[columnNames.indexOf(columnName)];
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            parameters[getParameterIndex(expressionSegment)] = value;
        } else {
            valueExpressions[columnNames.indexOf(columnName)] = new LiteralExpressionSegment(expressionSegment.getStartIndex(), expressionSegment.getStopIndex(), value);
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
