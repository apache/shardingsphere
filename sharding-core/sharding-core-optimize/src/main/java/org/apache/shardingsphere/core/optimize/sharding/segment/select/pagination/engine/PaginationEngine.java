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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.top.TopSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.Collections;
import java.util.List;

/**
 * Pagination engine.
 *
 * @author zhangliang
 */
public final class PaginationEngine {
    
    /**
     * Create pagination.
     * 
     * @param selectStatement SQL statement
     * @param selectItems select items
     * @param parameters SQL parameters
     * @return pagination
     */
    public Pagination createPagination(final SelectStatement selectStatement, final SelectItems selectItems, final List<Object> parameters) {
        Optional<LimitSegment> limitSegment = selectStatement.findSQLSegment(LimitSegment.class);
        if (limitSegment.isPresent()) {
            return new LimitPaginationEngine().createPagination(limitSegment.get(), parameters);
        }
        Optional<TopSegment> topSegment = selectStatement.findSQLSegment(TopSegment.class);
        Optional<WhereSegment> whereSegment = selectStatement.findSQLSegment(WhereSegment.class);
        if (topSegment.isPresent()) {
            return new TopPaginationEngine().createPagination(topSegment.get(), whereSegment.isPresent() ? whereSegment.get().getAndPredicates() : Collections.<AndPredicate>emptyList(), parameters);
        }
        if (whereSegment.isPresent()) {
            return new RowNumberPaginationEngine().createPagination(whereSegment.get().getAndPredicates(), selectItems, parameters);
        }
        return new Pagination(null, null, parameters);
    }
}
