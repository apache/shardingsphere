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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Insert statement.
 */
@Getter
@Setter
public class InsertStatement extends AbstractSQLStatement implements DMLStatement {
    
    private SimpleTableSegment table;
    
    private InsertColumnsSegment insertColumns;
    
    private SubquerySegment insertSelect;
    
    private SetAssignmentSegment setAssignment;
    
    private OnDuplicateKeyColumnsSegment onDuplicateKeyColumns;
    
    private ReturningSegment returningSegment;
    
    private OutputSegment outputSegment;
    
    private WithSegment withSegment;
    
    private MultiTableInsertType multiTableInsertType;
    
    private MultiTableInsertIntoSegment multiTableInsertIntoSegment;
    
    private MultiTableConditionalIntoSegment multiTableConditionalIntoSegment;
    
    private WhereSegment where;
    
    private ExecSegment execSegment;
    
    private WithTableHintSegment withTableHintSegment;
    
    private FunctionSegment rowSetFunctionSegment;
    
    private final Collection<InsertValuesSegment> values = new LinkedList<>();
    
    private final Collection<ColumnSegment> derivedInsertColumns = new LinkedList<>();
    
    /**
     * Get table.
     *
     * @return simple table segment
     */
    public Optional<SimpleTableSegment> getTable() {
        return Optional.ofNullable(table);
    }
    
    /**
     * Get insert columns segment.
     *
     * @return insert columns segment
     */
    public Optional<InsertColumnsSegment> getInsertColumns() {
        return Optional.ofNullable(insertColumns);
    }
    
    /**
     * Get columns.
     *
     * @return columns
     */
    public Collection<ColumnSegment> getColumns() {
        return null == insertColumns ? Collections.emptyList() : insertColumns.getColumns();
    }
    
    /**
     * Get insert select segment.
     *
     * @return insert select segment
     */
    public Optional<SubquerySegment> getInsertSelect() {
        return Optional.ofNullable(insertSelect);
    }
    
    /**
     * Get On duplicate key columns segment.
     *
     * @return on duplicate key columns segment
     */
    public Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
        return Optional.ofNullable(onDuplicateKeyColumns);
    }
    
    /**
     * Get set assignment segment.
     *
     * @return set assignment segment
     */
    public Optional<SetAssignmentSegment> getSetAssignment() {
        return Optional.ofNullable(setAssignment);
    }
    
    /**
     * Get with segment.
     *
     * @return with segment
     */
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }
    
    /**
     * Get output segment.
     *
     * @return output segment
     */
    public Optional<OutputSegment> getOutputSegment() {
        return Optional.ofNullable(outputSegment);
    }
    
    /**
     * Get multi table insert type.
     *
     * @return multi table insert type
     */
    public Optional<MultiTableInsertType> getMultiTableInsertType() {
        return Optional.ofNullable(multiTableInsertType);
    }
    
    /**
     * Get multi table insert into segment.
     *
     * @return multi table insert into segment
     */
    public Optional<MultiTableInsertIntoSegment> getMultiTableInsertIntoSegment() {
        return Optional.ofNullable(multiTableInsertIntoSegment);
    }
    
    /**
     * Get multi table conditional into segment.
     *
     * @return multi table conditional into segment
     */
    public Optional<MultiTableConditionalIntoSegment> getMultiTableConditionalIntoSegment() {
        return Optional.ofNullable(multiTableConditionalIntoSegment);
    }
    
    /**
     * Get returning segment of insert statement.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturningSegment() {
        return Optional.ofNullable(returningSegment);
    }
    
    /**
     * Get where segment.
     *
     * @return where segment
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get execute segment.
     *
     * @return execute segment
     */
    public Optional<ExecSegment> getExecSegment() {
        return Optional.ofNullable(execSegment);
    }
    
    /**
     * Get with table hint segment.
     *
     * @return with table hint segment
     */
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.ofNullable(withTableHintSegment);
    }
    
    /**
     * Get rowSet function segment.
     *
     * @return rowSet function segment
     */
    public Optional<FunctionSegment> getRowSetFunctionSegment() {
        return Optional.ofNullable(rowSetFunctionSegment);
    }
}
