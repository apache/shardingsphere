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

package org.apache.shardingsphere.core.optimizer.result;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
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
@RequiredArgsConstructor
public final class InsertColumnValues {
    
    private final DefaultKeyword type;
    
    private final Set<String> columnNames = new LinkedHashSet<>();
    
    private final List<InsertColumnValue> columnValues = new LinkedList<>();
    
    /**
     * Add insert column value.
     *
     * @param columnValues column values
     * @param columnParameters  column parameters
     */
    public void addInsertColumnValue(final List<SQLExpression> columnValues, final List<Object> columnParameters) {
        InsertColumnValue result = new InsertColumnValue();
        result.values.addAll(columnValues);
        result.parameters.addAll(columnParameters);
        this.columnValues.add(result);
    }
    
    /**
     * get column name.
     *
     * @param index index
     * @return column name
     */
    public String getColumnName(final int index) {
        return new ArrayList<>(columnNames).get(index);
    }
    
    @Getter
    public final class InsertColumnValue {
        
        private final List<SQLExpression> values = new LinkedList<>();
        
        private final List<Object> parameters = new LinkedList<>();
        
        private final List<DataNode> dataNodes = new LinkedList<>();
        
        /**
         * Update column value.
         *
         * @param columnName column name
         * @param columnValue column value
         */
        public void setColumnValue(final String columnName, final Object columnValue) {
            SQLExpression sqlExpression = values.get(getColumnIndex(columnName));
            if (sqlExpression instanceof SQLPlaceholderExpression) {
                parameters.set(getParameterIndex(sqlExpression), columnValue);
            } else {
                SQLExpression columnExpression = String.class == columnValue.getClass() ? new SQLTextExpression(String.valueOf(columnValue)) : new SQLNumberExpression((Number) columnValue);
                values.set(getColumnIndex(columnName), columnExpression);
            }
        }
    
        private int getColumnIndex(final String columnName) {
            return new ArrayList<>(columnNames).indexOf(columnName);
        }
    
        private int getParameterIndex(final SQLExpression sqlExpression) {
            List<SQLExpression> sqlPlaceholderExpressions = new ArrayList<>(Collections2.filter(values, new Predicate<SQLExpression>() {
                
                @Override
                public boolean apply(final SQLExpression input) {
                    return input instanceof SQLPlaceholderExpression;
                }
            }));
            return sqlPlaceholderExpressions.indexOf(sqlExpression);
        }
        
        /**
         * Get column value.
         *
         * @param columnName column Name
         * @return column value
         */
        public Object getColumnValue(final String columnName) {
            return getColumnValue(getColumnIndex(columnName));
        }
        
        /**
         * Get column value.
         *
         * @param columnValueIndex column value index
         * @return column value
         */
        public Object getColumnValue(final int columnValueIndex) {
            SQLExpression sqlExpression = values.get(columnValueIndex);
            if (sqlExpression instanceof SQLPlaceholderExpression) {
                return parameters.get(getParameterIndex(sqlExpression));
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
                result.append(String.format("%s = %s", getColumnName(i), getColumnSQLExpressionValue(i))).append(", ");
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
            SQLExpression sqlExpression = values.get(columnValueIndex);
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
