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

package org.apache.shardingsphere.core.optimize.result.insert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
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
public abstract class InsertOptimizeResultUnit {
    
    private final Collection<String> columnNames;
    
    private final SQLExpression[] values;
    
    private final Object[] parameters;
    
    private final int startIndexOfAppendedParameters;
    
    private final List<DataNode> dataNodes = new LinkedList<>();
    
    /**
     * Add column value.
     *
     * @param sqlExpression SQL expression
     */
    public final void addColumnValue(final SQLExpression sqlExpression) {
        values[getCurrentIndex(values, 0)] = sqlExpression;
    }
    
    /**
     * Add column parameter.
     *
     * @param parameter parameter
     */
    public final void addColumnParameter(final Object parameter) {
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
    
    /**
     * Set column value.
     *
     * @param columnName column name
     * @param columnValue column value
     */
    public final void setColumnValue(final String columnName, final Object columnValue) {
        SQLExpression sqlExpression = values[getColumnIndex(columnName)];
        if (sqlExpression instanceof SQLParameterMarkerExpression) {
            parameters[getParameterIndex(sqlExpression)] = columnValue;
        } else {
            SQLExpression columnExpression = String.class == columnValue.getClass() ? new SQLTextExpression(String.valueOf(columnValue)) : new SQLNumberExpression((Number) columnValue);
            values[getColumnIndex(columnName)] = columnExpression;
        }
    }
    
    private int getColumnIndex(final String columnName) {
        return new ArrayList<>(columnNames).indexOf(columnName);
    }
    
    private int getParameterIndex(final SQLExpression sqlExpression) {
        int result = 0;
        for (SQLExpression each : values) {
            if (sqlExpression == each) {
                return result;
            } else if (each instanceof SQLParameterMarkerExpression) {
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
    public final Object getColumnValue(final String columnName) {
        SQLExpression sqlExpression = values[getColumnIndex(columnName)];
        if (sqlExpression instanceof SQLParameterMarkerExpression) {
            return parameters[getParameterIndex(sqlExpression)];
        } else if (sqlExpression instanceof SQLTextExpression) {
            return ((SQLTextExpression) sqlExpression).getText();
        } else {
            return ((SQLNumberExpression) sqlExpression).getNumber();
        }
    }
    
    protected final String getColumnSQLExpressionValue(final int columnValueIndex) {
        SQLExpression sqlExpression = values[columnValueIndex];
        if (sqlExpression instanceof SQLParameterMarkerExpression) {
            return "?";
        } else if (sqlExpression instanceof SQLTextExpression) {
            return String.format("'%s'", ((SQLTextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof SQLIgnoreExpression) {
            return ((SQLIgnoreExpression) sqlExpression).getExpression();
        } else {
            return String.valueOf(((SQLNumberExpression) sqlExpression).getNumber());
        }
    }
}
