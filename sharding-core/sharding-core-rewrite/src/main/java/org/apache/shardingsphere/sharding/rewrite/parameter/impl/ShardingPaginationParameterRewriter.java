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

package org.apache.shardingsphere.sharding.rewrite.parameter.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sharding.rewrite.aware.ShardingRouteContextAware;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.rewriter.ParameterRewriter;

import java.util.List;

/**
 * Sharding pagination parameter rewriter.
 *
 * @author zhangliang
 */
@Setter
public final class ShardingPaginationParameterRewriter implements ParameterRewriter, ShardingRouteContextAware {
    
    private ShardingRouteContext shardingRouteContext;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectSQLStatementContext
                && ((SelectSQLStatementContext) sqlStatementContext).getPaginationContext().isHasPagination() && !shardingRouteContext.getRouteResult().isSingleRouting();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        PaginationContext pagination = ((SelectSQLStatementContext) sqlStatementContext).getPaginationContext();
        Optional<Integer> offsetParameterIndex = pagination.getOffsetParameterIndex();
        if (offsetParameterIndex.isPresent()) {
            rewriteOffset(pagination, offsetParameterIndex.get(), (StandardParameterBuilder) parameterBuilder);
        }
        Optional<Integer> rowCountParameterIndex = pagination.getRowCountParameterIndex();
        if (rowCountParameterIndex.isPresent()) {
            rewriteRowCount(pagination, rowCountParameterIndex.get(), (StandardParameterBuilder) parameterBuilder, sqlStatementContext);
        }
    }
    
    private void rewriteOffset(final PaginationContext pagination, final int offsetParameterIndex, final StandardParameterBuilder parameterBuilder) {
        parameterBuilder.addReplacedParameters(offsetParameterIndex, pagination.getRevisedOffset());
    }
    
    private void rewriteRowCount(final PaginationContext pagination, 
                                 final int rowCountParameterIndex, final StandardParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext) {
        parameterBuilder.addReplacedParameters(rowCountParameterIndex, pagination.getRevisedRowCount((SelectSQLStatementContext) sqlStatementContext));
    }
}
