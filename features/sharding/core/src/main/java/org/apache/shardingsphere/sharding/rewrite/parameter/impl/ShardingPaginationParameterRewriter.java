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

import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;

import java.util.List;

/**
 * Sharding pagination parameter rewriter.
 */
@Setter
public final class ShardingPaginationParameterRewriter implements ParameterRewriter, RouteContextAware {
    
    private RouteContext routeContext;
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getPaginationContext().isHasPagination() && !routeContext.isSingleRouting();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        PaginationContext pagination = ((SelectStatementContext) sqlStatementContext).getPaginationContext();
        pagination.getOffsetParameterIndex().ifPresent(optional -> rewriteOffset(pagination, optional, (StandardParameterBuilder) paramBuilder));
        pagination.getRowCountParameterIndex().ifPresent(optional -> rewriteRowCount(pagination, optional, (StandardParameterBuilder) paramBuilder, sqlStatementContext));
    }
    
    private void rewriteOffset(final PaginationContext pagination, final int offsetParamIndex, final StandardParameterBuilder paramBuilder) {
        paramBuilder.addReplacedParameters(offsetParamIndex, pagination.getRevisedOffset());
    }
    
    private void rewriteRowCount(final PaginationContext pagination, final int rowCountParamIndex, final StandardParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext) {
        paramBuilder.addReplacedParameters(rowCountParamIndex, pagination.getRevisedRowCount((SelectStatementContext) sqlStatementContext));
    }
}
