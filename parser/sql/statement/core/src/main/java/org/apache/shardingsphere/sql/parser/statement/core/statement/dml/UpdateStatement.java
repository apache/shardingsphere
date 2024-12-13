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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Optional;

/**
 * Update statement.
 */
@Getter
@Setter
public abstract class UpdateStatement extends AbstractSQLStatement implements DMLStatement {
    
    private TableSegment table;
    
    private SetAssignmentSegment setAssignment;
    
    private WhereSegment where;
    
    /**
     * Get where.
     *
     * @return where segment
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get assignment segment.
     *
     * @return assignment segment
     */
    public Optional<SetAssignmentSegment> getAssignmentSegment() {
        return Optional.ofNullable(setAssignment);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.empty();
    }
    
    /**
     * Get limit segment.
     *
     * @return limit segment
     */
    public Optional<LimitSegment> getLimit() {
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
     * Get delete where segment.
     *
     * @return delete where segment
     */
    public Optional<WhereSegment> getDeleteWhere() {
        return Optional.empty();
    }
    
    /**
     * Get option hint segment.
     *
     * @return option hint segment
     */
    public Optional<OptionHintSegment> getOptionHintSegment() {
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
     * Set with segment.
     *
     * @param withSegment with segment
     */
    public void setWithSegment(final WithSegment withSegment) {
    }
    
    /**
     * Set delete where segment.
     *
     * @param deleteWhereSegment delete where segment
     */
    public void setDeleteWhere(final WhereSegment deleteWhereSegment) {
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
     * Set output segment.
     *
     * @param outputSegment output segment
     */
    public void setOutputSegment(final OutputSegment outputSegment) {
    }
    
    /**
     * Get from segment.
     *
     * @return from segment
     */
    public Optional<TableSegment> getFrom() {
        return Optional.empty();
    }
    
    /**
     * Set from segment.
     *
     * @param from from segment
     */
    public void setFrom(final TableSegment from) {
    }
    
    /**
     * Get returning segment of update statement.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturningSegment() {
        return Optional.empty();
    }
    
    /**
     * Set returning segment of update statement.
     *
     * @param returningSegment returning segment
     */
    public void setReturningSegment(final ReturningSegment returningSegment) {
    }
}
