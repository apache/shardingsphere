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

package org.apache.shardingsphere.shadow.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Shadow condition.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class ShadowCondition {
    
    private final String columnName;
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    private final Map<Integer, Object> positionValueMap = new LinkedHashMap<>();
    
    public ShadowCondition(final String columnName, final int startIndex, final int stopIndex, final ExpressionSegment expressionSegment) {
        this.columnName = columnName;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        putPositionMap(expressionSegment);
    }
    
    private void putPositionMap(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            positionIndexMap.put(0, ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            positionValueMap.put(0, ((LiteralExpressionSegment) expressionSegment).getLiterals());
        }
    }
    
    /**
     * Get values.
     *
     * @param parameters SQL parameters
     * @return values
     */
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
