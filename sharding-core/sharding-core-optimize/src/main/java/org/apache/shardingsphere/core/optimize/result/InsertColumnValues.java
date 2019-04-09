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

package org.apache.shardingsphere.core.optimize.result;

import lombok.Getter;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Insert column values.
 *
 * @author panjuan
 */
@Getter
public final class InsertColumnValues {
    
    private final DefaultKeyword type;
    
    private final Set<String> columnNames = new LinkedHashSet<>();
    
    private final List<InsertColumnValue> columnValues = new LinkedList<>();
    
    public InsertColumnValues(final DefaultKeyword type, final List<String> columnNames) {
        this.type = type;
        this.columnNames.addAll(columnNames);
    }
    
    /**
     * Add insert column value.
     *
     * @param columnValues column values
     * @param columnParameters column parameters
     */
    public void addInsertColumnValue(final SQLExpression[] columnValues, final Object[] columnParameters) {
        this.columnValues.add(new InsertColumnValue(columnValues, columnParameters));
    }
    
    @Getter
    public final class InsertColumnValue {
        
        private final SQLExpression[] values;
        
        private final Object[] parameters;
        
        private final List<DataNode> dataNodes = new LinkedList<>();
        
        public InsertColumnValue(final SQLExpression[] values, final Object[] parameters) {
            this.values = values;
            this.parameters = parameters;
        }
    
        /**
         * Add column value.
         * 
         * @param sqlExpression sql expression
         */
        public void addColumnValue(final SQLExpression sqlExpression) {
            values[getCurrentIndex(values)] = sqlExpression;
        }
    
        /**
         * Add column parameter.
         *
         * @param parameter parameter 
         */
        public void addColumnParameter(final Object parameter) {
            parameters[getCurrentIndex(parameters)] = parameter;
        }
        
        private int getCurrentIndex(final Object[] array) {
            int count = 0;
            for (Object each : array) {
                if (null != each) {
                    count++;
                }
            }
            return count;
        }
        
        /**
         * Set column value.
         *
         * @param columnName column name
         * @param columnValue column value
         */
        public void setColumnValue(final String columnName, final Object columnValue) {
            SQLExpression sqlExpression = values[getColumnIndex(columnName)];
            if (sqlExpression instanceof SQLPlaceholderExpression) {
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
                } else if (each instanceof SQLPlaceholderExpression) {
                    result++;
                }
            }
            throw new ShardingException("Can not get parameter index.");
        }
        
        /**
         * Get column value.
         *
         * @param columnName column Name
         * @return column value
         */
        public Object getColumnValue(final String columnName) {
            SQLExpression sqlExpression = values[getColumnIndex(columnName)];
            if (sqlExpression instanceof SQLPlaceholderExpression) {
                return parameters[getParameterIndex(sqlExpression)];
            } else if (sqlExpression instanceof SQLTextExpression) {
                return ((SQLTextExpression) sqlExpression).getText();
            } else {
                return ((SQLNumberExpression) sqlExpression).getNumber();
            }
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (DefaultKeyword.SET == type) {
                fillResultBySet(result);
            } else {
                fillResultByValues(result);
            }
            return result.toString();
        }
        
        private void fillResultBySet(final StringBuilder result) {
            for (int i = 0; i < columnNames.size(); i++) {
                result.append(String.format("%s = %s", new ArrayList<>(columnNames).get(i), getColumnSQLExpressionValue(i))).append(", ");
            }
            result.delete(result.length() - 2, result.length());
        }
        
        private void fillResultByValues(final StringBuilder result) {
            result.append("(");
            for (int i = 0; i < columnNames.size(); i++) {
                result.append(getColumnSQLExpressionValue(i)).append(", ");
            }
            result.delete(result.length() - 2, result.length()).append(")");
        }
        
        private String getColumnSQLExpressionValue(final int columnValueIndex) {
            SQLExpression sqlExpression = values[columnValueIndex];
            if (sqlExpression instanceof SQLPlaceholderExpression) {
                return "?";
            } else if (sqlExpression instanceof SQLTextExpression) {
                return String.format("'%s'", ((SQLTextExpression) sqlExpression).getText());
            } else {
                return String.valueOf(((SQLNumberExpression) sqlExpression).getNumber());
            }
        }
    }
}
