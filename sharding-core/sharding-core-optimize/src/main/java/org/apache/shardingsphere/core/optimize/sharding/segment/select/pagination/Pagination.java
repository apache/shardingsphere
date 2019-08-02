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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitValueSegment;

import java.util.List;

/**
 * Pagination.
 *
 * @author zhangliang
 * @author caohao
 * @author zhangyonglun
 */
public final class Pagination {
    
    @Getter
    private final boolean hasPagination;
    
    private final PaginationValueSegment offsetSegment;
    
    private final PaginationValueSegment rowCountSegment;
    
    private final int actualOffset;
    
    private final Integer actualRowCount;
    
    public Pagination(final PaginationValueSegment offsetSegment, final PaginationValueSegment rowCountSegment, final List<Object> parameters) {
        hasPagination = null != offsetSegment || null != rowCountSegment;
        this.offsetSegment = offsetSegment;
        this.rowCountSegment = rowCountSegment;
        actualOffset = null == offsetSegment ? 0 : getValue(offsetSegment, parameters);
        actualRowCount = null == rowCountSegment ? null : getValue(rowCountSegment, parameters); 
    }
    
    private int getValue(final PaginationValueSegment paginationValueSegment, final List<Object> parameters) {
        return paginationValueSegment instanceof ParameterMarkerPaginationValueSegment
                ? (int) parameters.get(((ParameterMarkerPaginationValueSegment) paginationValueSegment).getParameterIndex())
                : ((NumberLiteralPaginationValueSegment) paginationValueSegment).getValue();
    }
    
    /**
     * Get offset segment.
     * 
     * @return offset segment
     */
    public Optional<PaginationValueSegment> getOffsetSegment() {
        return Optional.fromNullable(offsetSegment);
    }
    
    /**
     * Get row count segment.
     *
     * @return row count segment
     */
    public Optional<PaginationValueSegment> getRowCountSegment() {
        return Optional.fromNullable(rowCountSegment);
    }
    
    /**
     * Get actual offset.
     * 
     * @return actual offset
     */
    public int getActualOffset() {
        if (null == offsetSegment) {
            return 0;
        }
        return offsetSegment.isBoundOpened() ? actualOffset - 1 : actualOffset;
    }
    
    /**
     * Get actual row count.
     *
     * @return actual row count
     */
    public Optional<Integer> getActualRowCount() {
        if (null == rowCountSegment) {
            return Optional.absent();
        }
        return Optional.of(rowCountSegment.isBoundOpened() ? actualRowCount + 1 : actualRowCount);
    }
    
    /**
     * Get offset parameter index.
     *
     * @return offset parameter index
     */
    public Optional<Integer> getOffsetParameterIndex() {
        return offsetSegment instanceof ParameterMarkerPaginationValueSegment ? Optional.of(((ParameterMarkerPaginationValueSegment) offsetSegment).getParameterIndex()) : Optional.<Integer>absent();
    }
    
    /**
     * Get row count parameter index.
     *
     * @return row count parameter index
     */
    public Optional<Integer> getRowCountParameterIndex() {
        return rowCountSegment instanceof ParameterMarkerPaginationValueSegment
                ? Optional.of(((ParameterMarkerPaginationValueSegment) rowCountSegment).getParameterIndex()) : Optional.<Integer>absent();
    }
    
    /**
     * Get revised offset.
     *
     * @return revised offset
     */
    public int getRevisedOffset() {
        return 0;
    }
    
    /**
     * Get revised row count.
     * 
     * @param optimizedStatement optimized statement
     * @return revised row count
     */
    public int getRevisedRowCount(final ShardingSelectOptimizedStatement optimizedStatement) {
        if (isMaxRowCount(optimizedStatement)) {
            return Integer.MAX_VALUE;
        }
        return rowCountSegment instanceof LimitValueSegment ? actualOffset + actualRowCount : actualRowCount;
    }
    
    private boolean isMaxRowCount(final ShardingSelectOptimizedStatement optimizedStatement) {
        return (!optimizedStatement.getGroupBy().getItems().isEmpty()
                || !optimizedStatement.getSelectItems().getAggregationSelectItems().isEmpty()) && !optimizedStatement.isSameGroupByAndOrderByItems();
    }
}
