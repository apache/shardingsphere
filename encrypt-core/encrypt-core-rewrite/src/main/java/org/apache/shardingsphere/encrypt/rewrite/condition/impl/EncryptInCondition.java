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
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt condition.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class EncryptInCondition implements EncryptCondition {
    
    private final String columnName;
    
    private final String tableName;

    private final int startIndex;
    
    private final int stopIndex;
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    private final Map<Integer, Object> positionValueMap = new LinkedHashMap<>();
    
    public EncryptInCondition(final String columnName, final String tableName, final int startIndex, final int stopIndex, final List<ExpressionSegment> expressionSegments) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        int count = 0;
        for (ExpressionSegment each : expressionSegments) {
            putPositionMap(count, each);
            count++;
        }
    }
    
    private void putPositionMap(final int position, final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            positionIndexMap.put(position, ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            positionValueMap.put(position, ((LiteralExpressionSegment) expressionSegment).getLiterals());
        }
    }
    
    @Override
    public List<Object> getValues(final List<Object> parameters) {
        List<Object> result = new ArrayList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), parameter);
            } else {
                result.add(parameter);
            }
        }
        return result;
    }
}
