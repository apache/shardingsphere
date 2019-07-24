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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert;

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
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize result.
 *
 * @author panjuan
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString(exclude = "dataNodes")
public final class InsertOptimizeResultUnit {
    
    private final Collection<String> columnNames;
    
    private final ExpressionSegment[] values;
    
    private final Object[] parameters;
    
    private final int startIndexOfAppendedParameters;
    
    private final List<DataNode> dataNodes = new LinkedList<>();
    
    /**
     * Set column value.
     *
     * @param columnName column name
     * @param columnValue column value
     */
    public void setColumnValue(final String columnName, final Object columnValue) {
        ExpressionSegment expressionSegment = values[getColumnIndex(columnName)];
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            parameters[getParameterIndex(expressionSegment)] = columnValue;
        } else {
            values[getColumnIndex(columnName)] = new LiteralExpressionSegment(expressionSegment.getStartIndex(), expressionSegment.getStopIndex(), columnValue);
        }
    }
    
    private int getColumnIndex(final String columnName) {
        return new ArrayList<>(columnNames).indexOf(columnName);
    }
    
    private int getParameterIndex(final ExpressionSegment expressionSegment) {
        int result = 0;
        for (ExpressionSegment each : values) {
            if (expressionSegment == each) {
                return result;
            } else if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        throw new ShardingException("Can not get parameter index.");
    }
    
    /**
     * Get column value.
     *
     * @param columnName column name
     * @return column value
     */
    public Object getColumnValue(final String columnName) {
        ExpressionSegment expressionSegment = values[getColumnIndex(columnName)];
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return parameters[getParameterIndex(expressionSegment)];
        }
        return ((LiteralExpressionSegment) expressionSegment).getLiterals();
    }
    
    /**
     * Get column sql expression.
     *
     * @param columnName column name
     * @return column sql expression
     */
    public ExpressionSegment getColumnSQLExpression(final String columnName) {
        return values[getColumnIndex(columnName)];
    }
    
    /**
     * Add insert value.
     * 
     * @param insertValue insert value
     * @param parameters SQL parameters
     */
    public void addInsertValue(final Comparable<?> insertValue, final List<Object> parameters) {
        if (parameters.isEmpty()) {
            // TODO fix start index and stop index
            addColumnValue(new LiteralExpressionSegment(0, 0, insertValue));
        } else {
            // TODO fix start index and stop index
            addColumnValue(new ParameterMarkerExpressionSegment(0, 0, parameters.size() - 1));
            addColumnParameter(insertValue);
        }
    }
    
    private void addColumnValue(final ExpressionSegment expressionSegment) {
        values[getCurrentIndex(values, 0)] = expressionSegment;
    }
    
    private void addColumnParameter(final Object parameter) {
        parameters[getCurrentIndex(parameters, startIndexOfAppendedParameters)] = parameter;
    }
    
    private int getCurrentIndex(final Object[] array, final int startIndex) {
        for (int i = startIndex; i < array.length; i++) {
            if (null == array[i]) {
                return i;
            }
        }
        throw new ShardingException("Index Out Of Bounds For InsertOptimizeResultUnit.");
    }
}
