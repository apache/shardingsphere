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

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.Pagination;
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
        if (isNeedRewritePagination()) {
            Pagination pagination = ((ShardingSelectOptimizedStatement) sqlRouteResult.getOptimizedStatement()).getPagination();
            Optional<Integer> offsetParameterIndex = pagination.getOffsetParameterIndex();
            if (offsetParameterIndex.isPresent()) {
                rewriteOffset(parameterBuilder, pagination, offsetParameterIndex.get());
            }
            Optional<Integer> rowCountParameterIndex = pagination.getRowCountParameterIndex();
            if (rowCountParameterIndex.isPresent()) {
                rewriteRowCount(parameterBuilder, pagination, rowCountParameterIndex.get());
            }
        }
    }
    
    private boolean isNeedRewritePagination() {
        return sqlRouteResult.getOptimizedStatement() instanceof ShardingSelectOptimizedStatement
                && null != ((ShardingSelectOptimizedStatement) sqlRouteResult.getOptimizedStatement()).getPagination() && !sqlRouteResult.getRoutingResult().isSingleRouting();
    }
    
    private void rewriteOffset(final ParameterBuilder parameterBuilder, final Pagination pagination, final int offsetParameterIndex) {
        parameterBuilder.getReplacedIndexAndParameters().put(offsetParameterIndex, pagination.getRevisedOffset());
    }
    
    private void rewriteRowCount(final ParameterBuilder parameterBuilder, final Pagination pagination, final int rowCountParameterIndex) {
        parameterBuilder.getReplacedIndexAndParameters().put(rowCountParameterIndex, pagination.getRevisedRowCount((SelectStatement) sqlRouteResult.getOptimizedStatement().getSQLStatement()));
    }
}
