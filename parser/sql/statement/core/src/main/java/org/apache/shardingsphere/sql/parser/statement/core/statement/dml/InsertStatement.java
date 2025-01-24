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
public abstract class InsertStatement extends AbstractSQLStatement implements DMLStatement {
    
    private SimpleTableSegment table;
    
    private InsertColumnsSegment insertColumns;
    
    private SubquerySegment insertSelect;
    
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
        return Optional.empty();
    }
    
    /**
     * Set on duplicate key columns segment.
     *
     * @param onDuplicateKeyColumns on duplicate key columns segment
     */
    public void setOnDuplicateKeyColumns(final OnDuplicateKeyColumnsSegment onDuplicateKeyColumns) {
    }
    
    /**
     * Get set assignment segment.
     *
     * @return set assignment segment
     */
    public Optional<SetAssignmentSegment> getSetAssignment() {
        return Optional.empty();
    }
    
    /**
     * Set set assignment segment.
     *
     * @param setAssignment set assignment segment
     */
    public void setSetAssignment(final SetAssignmentSegment setAssignment) {
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
     * Set with segment.
     *
     * @param withSegment with segment
     */
    public void setWithSegment(final WithSegment withSegment) {
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
     * Get multi table insert type.
     *
     * @return multi table insert type
     */
    public Optional<MultiTableInsertType> getMultiTableInsertType() {
        return Optional.empty();
    }
    
    /**
     * Set multi table insert type.
     *
     * @param multiTableInsertType multi table insert type
     */
    public void setMultiTableInsertType(final MultiTableInsertType multiTableInsertType) {
    }
    
    /**
     * Get multi table insert into segment.
     *
     * @return multi table insert into segment
     */
    public Optional<MultiTableInsertIntoSegment> getMultiTableInsertIntoSegment() {
        return Optional.empty();
    }
    
    /**
     * Set multi table insert into segment.
     *
     * @param multiTableInsertIntoSegment multi table insert into segment
     */
    public void setMultiTableInsertIntoSegment(final MultiTableInsertIntoSegment multiTableInsertIntoSegment) {
    }
    
    /**
     * Get multi table conditional into segment.
     *
     * @return multi table conditional into segment
     */
    public Optional<MultiTableConditionalIntoSegment> getMultiTableConditionalIntoSegment() {
        return Optional.empty();
    }
    
    /**
     * Set multi table conditional into segment.
     *
     * @param multiTableConditionalIntoSegment multi table conditional into segment
     */
    public void setMultiTableConditionalIntoSegment(final MultiTableConditionalIntoSegment multiTableConditionalIntoSegment) {
    }
    
    /**
     * Get returning segment of insert statement.
     *
     * @return returning segment
     */
    public Optional<ReturningSegment> getReturningSegment() {
        return Optional.empty();
    }
    
    /**
     * Set returning segment of insert statement.
     *
     * @param returningSegment returning segment
     */
    public void setReturningSegment(final ReturningSegment returningSegment) {
    }
    
    /**
     * Get where segment.
     *
     * @return where segment
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.empty();
    }
    
    /**
     * Set where segment.
     *
     * @param whereSegment where segment
     */
    public void setWhere(final WhereSegment whereSegment) {
    }
    
    /**
     * Get execute segment.
     *
     * @return execute segment
     */
    public Optional<ExecSegment> getExecSegment() {
        return Optional.empty();
    }
    
    /**
     * Set execute segment.
     *
     * @param execSegment execute segment
     */
    public void setExecSegment(final ExecSegment execSegment) {
    }
    
    /**
     * Get with table hint segment.
     *
     * @return with table hint segment
     */
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.empty();
    }
    
    /**
     * Get rowSet function segment.
     *
     * @return rowSet function segment
     */
    public Optional<FunctionSegment> getRowSetFunctionSegment() {
        return Optional.empty();
    }
}
