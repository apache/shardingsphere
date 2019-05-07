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

package org.apache.shardingsphere.core.parse.antlr.sql.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLParameterMarkerExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Update statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
@Getter
@Setter
public final class UpdateStatement extends DMLStatement {
    
    private final Map<Column, SQLExpression> assignments = new LinkedHashMap<>();
    
    private int whereStartIndex;
    
    private int whereStopIndex;
    
    private int whereParameterStartIndex;
    
    private int whereParameterEndIndex;
    
    /**
     * Get column value.
     * 
     * @param column column
     * @param parameters parameters
     * @return column value
     */
    public Comparable<?> getColumnValue(final Column column, final List<Object> parameters) {
        SQLExpression sqlExpression = assignments.get(column);
        if (sqlExpression instanceof SQLParameterMarkerExpression) {
            return parameters.get(((SQLParameterMarkerExpression) sqlExpression).getIndex()).toString();
        }
        if (sqlExpression instanceof SQLTextExpression) {
            return ((SQLTextExpression) sqlExpression).getText();
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            return (Comparable) ((SQLNumberExpression) sqlExpression).getNumber();
        }
        throw new ShardingException("Can not find column value by %s.", column);
    }
}
