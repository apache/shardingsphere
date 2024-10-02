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

package org.apache.shardingsphere.sql.parser.statement.core.statement.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Optional;

/**
 * Delete statement.
 */
@Setter
public abstract class DeleteStatement extends AbstractSQLStatement implements DMLStatement {
    
    @Getter
    private TableSegment table;
    
    private WhereSegment where;
    
    /**
     * Get where.
     *
     * @return where
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get order by.
     *
     * @return order by
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.empty();
    }
    
    /**
     * Get limit.
     *
     * @return limit
     */
    public Optional<LimitSegment> getLimit() {
        return Optional.empty();
    }
    
    /**
     * Get output segment.
     *
     * @return output segment
     */
    public Optional<OutputSegment> getOutputSegment() {
        return Optional.empty();
    }
    
    /**
     * Get with segment.
     *
     * @return with segment
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.empty();
    }
    
    /**
     * Set order by segment.
     *
     * @param orderBySegment order by segment
     */
    public void setOrderBy(final OrderBySegment orderBySegment) {
    }
    
    /**
     * Set limit segment.
     *
     * @param limitSegment limit segment
     */
    public void setLimit(final LimitSegment limitSegment) {
    }
    
    /**
     * Set output segment.
     *
     * @param outputSegment output segment
     */
    public void setOutputSegment(final OutputSegment outputSegment) {
    }
    
    /**
     * Set with segment.
     *
     * @param withSegment with segment
     */
    public void setWithSegment(final WithSegment withSegment) {
    }
    
    /**
     * Get returning segment of delete statement.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturningSegment() {
        return Optional.empty();
    }
}
