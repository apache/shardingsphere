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

package org.apache.shardingsphere.encrypt.rewrite.condition.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt condition for equal.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class EncryptBinaryCondition implements EncryptCondition {
    
    private final ColumnSegment columnSegment;
    
    private final String tableName;
    
    private final String operator;
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final ExpressionSegment expressionSegment;
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    private final Map<Integer, Object> positionValueMap = new LinkedHashMap<>();
    
    public EncryptBinaryCondition(final ColumnSegment columnSegment, final String tableName, final String operator, final int startIndex, final int stopIndex,
                                  final ExpressionSegment expressionSegment) {
        this.columnSegment = columnSegment;
        this.tableName = tableName;
        this.operator = operator;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.expressionSegment = expressionSegment;
        putPositionMap(0, expressionSegment);
    }
    
    private int putPositionMap(final int index, final ExpressionSegment expressionSegment) {
        int parameterIndex = index;
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            positionIndexMap.put(parameterIndex, ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
            return parameterIndex + 1;
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            positionValueMap.put(parameterIndex, ((LiteralExpressionSegment) expressionSegment).getLiterals());
            return parameterIndex + 1;
        } else if (expressionSegment instanceof FunctionSegment && "CONCAT".equalsIgnoreCase(((FunctionSegment) expressionSegment).getFunctionName())) {
            for (ExpressionSegment each : ((FunctionSegment) expressionSegment).getParameters()) {
                parameterIndex = putPositionMap(parameterIndex, each);
            }
        }
        return parameterIndex;
    }
}
