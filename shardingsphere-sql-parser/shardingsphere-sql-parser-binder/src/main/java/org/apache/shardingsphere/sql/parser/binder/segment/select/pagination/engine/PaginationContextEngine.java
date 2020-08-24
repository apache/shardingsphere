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

package org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine;

import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pagination context engine.
 */
public final class PaginationContextEngine {
    
    /**
     * Create pagination context.
     * 
     * @param selectStatement SQL statement
     * @param projectionsContext projections context
     * @param parameters SQL parameters
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final SelectStatement selectStatement, final ProjectionsContext projectionsContext, final List<Object> parameters) {
        Optional<LimitSegment> limitSegment = selectStatement.getLimit();
        if (limitSegment.isPresent()) {
            return new LimitPaginationContextEngine().createPaginationContext(limitSegment.get(), parameters);
        }
        Optional<TopProjectionSegment> topProjectionSegment = findTopProjection(selectStatement);
        Optional<WhereSegment> whereSegment = selectStatement.getWhere();
        if (topProjectionSegment.isPresent()) {
            return new TopPaginationContextEngine().createPaginationContext(
                    topProjectionSegment.get(), whereSegment.isPresent() ? whereSegment.get().getAndPredicates() : Collections.emptyList(), parameters);
        }
        if (whereSegment.isPresent()) {
            return new RowNumberPaginationContextEngine().createPaginationContext(whereSegment.get().getAndPredicates(), projectionsContext, parameters);
        }
        return new PaginationContext(null, null, parameters);
    }
    
    private Optional<TopProjectionSegment> findTopProjection(final SelectStatement selectStatement) {
        for (TableReferenceSegment tableReferenceSegment : selectStatement.getTableReferences()) {
            if (!(tableReferenceSegment.getTableFactor().getTable() instanceof SubqueryTableSegment)) {
                continue;
            }
            SelectStatement subquerySelect = ((SubqueryTableSegment) tableReferenceSegment.getTableFactor().getTable()).getSubquery().getSelect();
            for (ProjectionSegment each : subquerySelect.getProjections().getProjections()) {
                if (each instanceof TopProjectionSegment) {
                    return Optional.of((TopProjectionSegment) each);
                }
            }   
        }
        return Optional.empty();
    }
}
