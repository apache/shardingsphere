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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Pagination context engine.
 */
@RequiredArgsConstructor
public final class PaginationContextEngine {
    
    private final DialectPaginationOption option;
    
    /**
     * Create pagination context.
     *
     * @param selectStatement SQL statement
     * @param projectionsContext projections context
     * @param params SQL parameters
     * @param whereSegments where segments
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final SelectStatement selectStatement, final ProjectionsContext projectionsContext,
                                                     final List<Object> params, final Collection<WhereSegment> whereSegments) {
        Optional<LimitSegment> limitSegment = selectStatement.getLimit();
        if (limitSegment.isPresent()) {
            return new LimitPaginationContextEngine().createPaginationContext(limitSegment.get(), params);
        }
        Collection<ExpressionSegment> expressions = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            expressions.add(each.getExpr());
        }
        if (option.isContainsTop()) {
            Optional<TopProjectionSegment> topProjectionSegment = TopProjectionExtractor.findTopProjection(selectStatement);
            if (topProjectionSegment.isPresent()) {
                return new TopPaginationContextEngine().createPaginationContext(topProjectionSegment.get(), expressions, params);
            }
        }
        if (!expressions.isEmpty() && option.isContainsRowNumber()) {
            return new RowNumberPaginationContextEngine(option).createPaginationContext(expressions, projectionsContext, params);
        }
        return new PaginationContext(null, null, params);
    }
}
