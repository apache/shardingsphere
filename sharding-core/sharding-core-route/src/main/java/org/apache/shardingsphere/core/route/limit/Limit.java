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

package org.apache.shardingsphere.core.route.limit;

import lombok.Getter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.ParameterMarkerLimitValueSegment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Limit.
 *
 * @author zhangliang
 * @author caohao
 * @author zhangyonglun
 */
@Getter
public final class Limit {
    
    private final LimitValue offset;
    
    private final LimitValue rowCount;
    
    public Limit(final LimitValueSegment offsetSegment, final LimitValueSegment rowCountSegment, final List<Object> parameters) {
        offset = null == offsetSegment ? null : createLimitValue(offsetSegment, parameters);
        rowCount = null == rowCountSegment ? null : createLimitValue(rowCountSegment, parameters);
    }
    
    private LimitValue createLimitValue(final LimitValueSegment limitValueSegment, final List<Object> parameters) {
        int segmentValue = limitValueSegment instanceof ParameterMarkerLimitValueSegment
                ? (int) parameters.get(((ParameterMarkerLimitValueSegment) limitValueSegment).getParameterIndex()) : ((NumberLiteralLimitValueSegment) limitValueSegment).getValue();
        return new LimitValue(limitValueSegment, segmentValue);
    }
    
    /**
     * Get offset value.
     * 
     * @return offset value
     */
    public int getOffsetValue() {
        return null != offset ? offset.getValue() : 0;
    }
    
    /**
     * Get row count value.
     *
     * @return row count value
     */
    public int getRowCountValue() {
        return null != rowCount ? rowCount.getValue() : -1;
    }
    
    /**
     * Get revise parameters.
     * 
     * @param isFetchAll is fetch all data or not
     * @param databaseType database type
     * @return revised parameters and parameters' indexes
     */
    public Map<Integer, Object> getRevisedParameters(final boolean isFetchAll, final String databaseType) {
        Map<Integer, Object> result = new HashMap<>(2, 1);
        if (null != offset && offset.getLimitValueSegment() instanceof ParameterMarkerLimitValueSegment) {
            result.put(((ParameterMarkerLimitValueSegment) offset.getLimitValueSegment()).getParameterIndex(), 0);
        }
        if (null != rowCount && rowCount.getLimitValueSegment() instanceof ParameterMarkerLimitValueSegment) {
            result.put(((ParameterMarkerLimitValueSegment) rowCount.getLimitValueSegment()).getParameterIndex(), getRewriteRowCount(isFetchAll, databaseType));
        }
        return result;
    }
    
    private int getRewriteRowCount(final boolean isFetchAll, final String databaseType) {
        if (isFetchAll) {
            return Integer.MAX_VALUE;
        }
        return isNeedRewriteRowCount(databaseType) ? getOffsetValue() + rowCount.getValue() : rowCount.getValue();
    }
    
    /**
     * Judge is need rewrite row count or not.
     * 
     * @param databaseType database type
     * @return is need rewrite row count or not
     */
    public boolean isNeedRewriteRowCount(final String databaseType) {
        return "MySQL".equals(databaseType) || "PostgreSQL".equals(databaseType) || "H2".equals(databaseType);
    }
}
