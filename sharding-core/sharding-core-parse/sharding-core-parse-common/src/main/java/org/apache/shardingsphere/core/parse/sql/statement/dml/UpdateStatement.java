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

package org.apache.shardingsphere.core.parse.sql.statement.dml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Update statement.
 *
 * @author zhangliang
 * @author panjuan
 */
@ToString(callSuper = true)
@Getter
@Setter
public final class UpdateStatement extends DMLStatement {
    
    private final Map<Column, ExpressionSegment> assignments = new LinkedHashMap<>();
    
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
        ExpressionSegment expressionSegment = assignments.get(column);
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return (Comparable<?>) parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return (Comparable<?>) ((LiteralExpressionSegment) expressionSegment).getLiterals();
        }
        throw new ShardingException("Can not find column value by %s.", column);
    }
}
