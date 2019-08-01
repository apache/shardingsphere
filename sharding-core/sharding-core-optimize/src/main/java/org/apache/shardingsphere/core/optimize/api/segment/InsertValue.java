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
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;
import java.util.List;

/**
 * Insert value.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class InsertValue {
    
    private final Collection<ExpressionSegment> assignments;
    
    /**
     * Get parameters count.
     * 
     * @return parameters count
     */
    public int getParametersCount() {
        int result = 0;
        for (ExpressionSegment each : assignments) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Get values.
     * 
     * @param derivedColumnsCount derived columns count
     * @return values
     */
    public ExpressionSegment[] getValues(final int derivedColumnsCount) {
        ExpressionSegment[] result = new ExpressionSegment[assignments.size() + derivedColumnsCount];
        assignments.toArray(result);
        return result;
    }
    
    /**
     * Get parameters of this insert value segment.
     * 
     * @param parameters SQL parameters
     * @param parametersBeginIndex begin index on this insert value segment of parameters
     * @param derivedColumnsCount derived columns count
     * @return parameters of this insert value segment
     */
    public Object[] getParameters(final List<Object> parameters, final int parametersBeginIndex, final int derivedColumnsCount) {
        int parametersCount = getParametersCount();
        if (0 == parametersCount) {
            return new Object[0];
        }
        Object[] result = new Object[parametersCount + derivedColumnsCount];
        parameters.subList(parametersBeginIndex, parametersBeginIndex + parametersCount).toArray(result);
        return result;
    }
}
