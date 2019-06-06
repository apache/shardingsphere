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

package org.apache.shardingsphere.core.rewrite.rewriter.parameter;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;

/**
 * Parameter rewriter for sharding.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingParameterRewriter implements ParameterRewriter {
    
    private final SQLRouteResult sqlRouteResult;
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder) {
        if (sqlRouteResult.getSqlStatement() instanceof SelectStatement && null != sqlRouteResult.getPagination()) {
            if (null != sqlRouteResult.getPagination().getOffset()) {
                rewriteOffset(sqlRouteResult.getPagination().getOffset().getSegment(), parameterBuilder);
            }
            if (null != sqlRouteResult.getPagination().getRowCount()) {
                rewriteRowCount(sqlRouteResult.getPagination().getRowCount().getSegment(), parameterBuilder);
            }
        }
    }
    
    private void rewriteOffset(final PaginationValueSegment offsetSegment, final ParameterBuilder parameterBuilder) {
        if (offsetSegment instanceof ParameterMarkerPaginationValueSegment) {
            parameterBuilder.getReplacedIndexAndParameters().put(((ParameterMarkerPaginationValueSegment) offsetSegment).getParameterIndex(), 0);
        }
    }
    
    private void rewriteRowCount(final PaginationValueSegment rowCountSegment, final ParameterBuilder parameterBuilder) {
        if (rowCountSegment instanceof ParameterMarkerPaginationValueSegment) {
            parameterBuilder.getReplacedIndexAndParameters().put(((ParameterMarkerPaginationValueSegment) rowCountSegment).getParameterIndex(), 
                    sqlRouteResult.getPagination().getRevisedRowCount((SelectStatement) sqlRouteResult.getSqlStatement()));
        }
    }
}
