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

package org.apache.shardingsphere.core.route.pagination;

import lombok.Getter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitValueSegment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pagination.
 *
 * @author zhangliang
 * @author caohao
 * @author zhangyonglun
 */
@Getter
public final class Pagination {
    
    private final PaginationValue offset;
    
    private final PaginationValue rowCount;
    
    public Pagination(final PaginationValueSegment offsetSegment, final PaginationValueSegment rowCountSegment, final List<Object> parameters) {
        offset = null == offsetSegment ? null : createPaginationValue(offsetSegment, parameters);
        rowCount = null == rowCountSegment ? null : createPaginationValue(rowCountSegment, parameters);
    }
    
    private PaginationValue createPaginationValue(final PaginationValueSegment paginationValueSegment, final List<Object> parameters) {
        int segmentValue = paginationValueSegment instanceof ParameterMarkerPaginationValueSegment
                ? (int) parameters.get(((ParameterMarkerPaginationValueSegment) paginationValueSegment).getParameterIndex())
                : ((NumberLiteralPaginationValueSegment) paginationValueSegment).getValue();
        return new PaginationValue(paginationValueSegment, segmentValue);
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
     * @param isMaxRowCount is max row count
     * @return revised parameters and parameters' indexes
     */
    public Map<Integer, Object> getRevisedParameters(final boolean isMaxRowCount) {
        Map<Integer, Object> result = new HashMap<>(2, 1);
        if (null != offset && offset.getSegment() instanceof ParameterMarkerPaginationValueSegment) {
            result.put(((ParameterMarkerPaginationValueSegment) offset.getSegment()).getParameterIndex(), 0);
        }
        if (null != rowCount && rowCount.getSegment() instanceof ParameterMarkerPaginationValueSegment) {
            result.put(((ParameterMarkerPaginationValueSegment) rowCount.getSegment()).getParameterIndex(), isMaxRowCount ? Integer.MAX_VALUE : getRevisedRowCount());
        }
        return result;
    }
    
    /**
     * Get revised row count.
     * 
     * @return revised row count
     */
    public int getRevisedRowCount() {
        return rowCount.getSegment() instanceof LimitValueSegment ? getOffsetValue() + rowCount.getValue() : rowCount.getValue();
    }
}
