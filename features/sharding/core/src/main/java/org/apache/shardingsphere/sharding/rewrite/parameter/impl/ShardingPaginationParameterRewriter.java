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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;

import java.util.List;

/**
 * Sharding pagination parameter rewriter.
 */
@RequiredArgsConstructor
public final class ShardingPaginationParameterRewriter implements ParameterRewriter {
    
    private final RouteContext routeContext;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getPaginationContext().isHasPagination() && !routeContext.isSingleRouting();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        PaginationContext pagination = ((SelectStatementContext) sqlStatementContext).getPaginationContext();
        pagination.getOffsetParameterIndex().ifPresent(optional -> rewriteOffset(pagination, optional, paramBuilder));
        pagination.getRowCountParameterIndex().ifPresent(optional -> rewriteRowCount(pagination, optional, paramBuilder, sqlStatementContext));
    }
    
    private void rewriteOffset(final PaginationContext pagination, final int offsetParamIndex, final ParameterBuilder paramBuilder) {
        if (paramBuilder instanceof StandardParameterBuilder) {
            ((StandardParameterBuilder) paramBuilder).addReplacedParameters(offsetParamIndex, pagination.getRevisedOffset());
            return;
        }
        rewriteOffset(pagination, offsetParamIndex, (GroupedParameterBuilder) paramBuilder);
    }
    
    private void rewriteOffset(final PaginationContext pagination, final int offsetParamIndex, final GroupedParameterBuilder paramBuilder) {
        if (paramBuilder.isContainsGroupedParams()) {
            paramBuilder.getAfterGenericParameterBuilder().addReplacedParameters(offsetParamIndex - paramBuilder.getAfterGenericParameterOffset(), pagination.getRevisedOffset());
        } else {
            paramBuilder.getBeforeGenericParameterBuilder().addReplacedParameters(offsetParamIndex, pagination.getRevisedOffset());
        }
    }
    
    private void rewriteRowCount(final PaginationContext pagination, final int rowCountParamIndex, final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext) {
        if (paramBuilder instanceof StandardParameterBuilder) {
            ((StandardParameterBuilder) paramBuilder).addReplacedParameters(rowCountParamIndex, pagination.getRevisedRowCount((SelectStatementContext) sqlStatementContext));
            return;
        }
        rewriteRowCount(pagination, rowCountParamIndex, (GroupedParameterBuilder) paramBuilder, (SelectStatementContext) sqlStatementContext);
    }
    
    private static void rewriteRowCount(final PaginationContext pagination, final int rowCountParamIndex, final GroupedParameterBuilder paramBuilder,
                                        final SelectStatementContext sqlStatementContext) {
        if (paramBuilder.isContainsGroupedParams()) {
            paramBuilder.getAfterGenericParameterBuilder().addReplacedParameters(rowCountParamIndex - paramBuilder.getAfterGenericParameterOffset(),
                    pagination.getRevisedRowCount(sqlStatementContext));
        } else {
            paramBuilder.getBeforeGenericParameterBuilder().addReplacedParameters(rowCountParamIndex, pagination.getRevisedRowCount(sqlStatementContext));
        }
    }
}
