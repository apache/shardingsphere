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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.WithSQLStatementAttribute;

import java.util.Optional;

/**
 * Update statement.
 */
@Getter
@Setter
public final class UpdateStatement extends DMLStatement {
    
    private TableSegment table;
    
    private SetAssignmentSegment setAssignment;
    
    private WhereSegment where;
    
    private OrderBySegment orderBy;
    
    private LimitSegment limit;
    
    private TableSegment from;
    
    private WhereSegment deleteWhere;
    
    private WithSegment with;
    
    private ReturningSegment returning;
    
    private WithTableHintSegment withTableHint;
    
    private OptionHintSegment optionHint;
    
    private OutputSegment output;
    
    public UpdateStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get where.
     *
     * @return where
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get assignment.
     *
     * @return assignment
     */
    public Optional<SetAssignmentSegment> getAssignment() {
        return Optional.ofNullable(setAssignment);
    }
    
    /**
     * Get order by.
     *
     * @return order by
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.ofNullable(orderBy);
    }
    
    /**
     * Get limit.
     *
     * @return limit
     */
    public Optional<LimitSegment> getLimit() {
        return Optional.ofNullable(limit);
    }
    
    /**
     * Get option hint.
     *
     * @return option hint
     */
    public Optional<OptionHintSegment> getOptionHint() {
        return Optional.ofNullable(optionHint);
    }
    
    /**
     * Get output.
     *
     * @return output
     */
    public Optional<OutputSegment> getOutput() {
        return Optional.ofNullable(output);
    }
    
    /**
     * Get from.
     *
     * @return from
     */
    public Optional<TableSegment> getFrom() {
        return Optional.ofNullable(from);
    }
    
    /**
     * Get delete where.
     *
     * @return delete where
     */
    public Optional<WhereSegment> getDeleteWhere() {
        return Optional.ofNullable(deleteWhere);
    }
    
    /**
     * Get with.
     *
     * @return with
     */
    public Optional<WithSegment> getWith() {
        return Optional.ofNullable(with);
    }
    
    /**
     * Get returning.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturning() {
        return Optional.ofNullable(returning);
    }
    
    /**
     * Get with table hint.
     *
     * @return with table hint.
     */
    public Optional<WithTableHintSegment> getWithTableHint() {
        return Optional.ofNullable(withTableHint);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new WithSQLStatementAttribute(with));
    }
}
