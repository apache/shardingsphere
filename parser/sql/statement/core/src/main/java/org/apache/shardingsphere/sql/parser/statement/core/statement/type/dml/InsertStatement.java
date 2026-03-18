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

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ValueReferenceSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.WithSQLStatementAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Insert statement.
 */
@Getter
public final class InsertStatement extends DMLStatement {
    
    private final SimpleTableSegment table;
    
    private final InsertColumnsSegment insertColumns;
    
    private final SubquerySegment insertSelect;
    
    private final SetAssignmentSegment setAssignment;
    
    private final OnDuplicateKeyColumnsSegment onDuplicateKeyColumns;
    
    private final ValueReferenceSegment valueReference;
    
    private final ReturningSegment returning;
    
    private final OutputSegment output;
    
    private final WithSegment with;
    
    private final MultiTableInsertType multiTableInsertType;
    
    private final MultiTableInsertIntoSegment multiTableInsertInto;
    
    private final MultiTableConditionalIntoSegment multiTableConditionalInto;
    
    private final WhereSegment where;
    
    private final ExecSegment exec;
    
    private final WithTableHintSegment withTableHint;
    
    private final FunctionSegment rowSetFunction;
    
    private final boolean ignore;
    
    private final boolean replace;
    
    private final Collection<InsertValuesSegment> values;
    
    private final Collection<ColumnSegment> derivedInsertColumns;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private InsertStatement(final DatabaseType databaseType, final SimpleTableSegment table, final InsertColumnsSegment insertColumns,
                            final SubquerySegment insertSelect, final SetAssignmentSegment setAssignment, final OnDuplicateKeyColumnsSegment onDuplicateKeyColumns,
                            final ValueReferenceSegment valueReference, final ReturningSegment returning, final OutputSegment output, final WithSegment with,
                            final MultiTableInsertType multiTableInsertType, final MultiTableInsertIntoSegment multiTableInsertInto,
                            final MultiTableConditionalIntoSegment multiTableConditionalInto, final WhereSegment where, final ExecSegment exec,
                            final WithTableHintSegment withTableHint, final FunctionSegment rowSetFunction, final boolean ignore, final boolean replace,
                            final Collection<InsertValuesSegment> values, final Collection<ColumnSegment> derivedInsertColumns) {
        super(databaseType);
        this.table = table;
        this.insertColumns = insertColumns;
        this.insertSelect = insertSelect;
        this.setAssignment = setAssignment;
        this.onDuplicateKeyColumns = onDuplicateKeyColumns;
        this.valueReference = valueReference;
        this.returning = returning;
        this.output = output;
        this.with = with;
        this.multiTableInsertType = multiTableInsertType;
        this.multiTableInsertInto = multiTableInsertInto;
        this.multiTableConditionalInto = multiTableConditionalInto;
        this.where = where;
        this.exec = exec;
        this.withTableHint = withTableHint;
        this.rowSetFunction = rowSetFunction;
        this.ignore = ignore;
        this.replace = replace;
        this.values = null == values ? new LinkedList<>() : values;
        this.derivedInsertColumns = null == derivedInsertColumns ? new LinkedList<>() : derivedInsertColumns;
        attributes = new SQLStatementAttributes(new WithSQLStatementAttribute(with));
    }
    
    /**
     * Get table.
     *
     * @return table
     */
    public Optional<SimpleTableSegment> getTable() {
        return Optional.ofNullable(table);
    }
    
    /**
     * Get insert columns.
     *
     * @return insert columns
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
     * Get insert select.
     *
     * @return insert select
     */
    public Optional<SubquerySegment> getInsertSelect() {
        return Optional.ofNullable(insertSelect);
    }
    
    /**
     * Get On duplicate key columns.
     *
     * @return on duplicate key columns
     */
    public Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumns() {
        return Optional.ofNullable(onDuplicateKeyColumns);
    }
    
    /**
     * Get value reference.
     *
     * @return value reference
     */
    public Optional<ValueReferenceSegment> getValueReference() {
        return Optional.ofNullable(valueReference);
    }
    
    /**
     * Get set assignment.
     *
     * @return set assignment
     */
    public Optional<SetAssignmentSegment> getSetAssignment() {
        return Optional.ofNullable(setAssignment);
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
     * Get output.
     *
     * @return output
     */
    public Optional<OutputSegment> getOutput() {
        return Optional.ofNullable(output);
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
     * Get multi table insert into.
     *
     * @return multi table insert into
     */
    public Optional<MultiTableInsertIntoSegment> getMultiTableInsertInto() {
        return Optional.ofNullable(multiTableInsertInto);
    }
    
    /**
     * Get multi table conditional into.
     *
     * @return multi table conditional into
     */
    public Optional<MultiTableConditionalIntoSegment> getMultiTableConditionalInto() {
        return Optional.ofNullable(multiTableConditionalInto);
    }
    
    /**
     * Get returning.
     *
     * @return returning
     */
    public Optional<ReturningSegment> getReturning() {
        return Optional.ofNullable(returning);
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
     * Get execute.
     *
     * @return execute
     */
    public Optional<ExecSegment> getExec() {
        return Optional.ofNullable(exec);
    }
    
    /**
     * Get with table hint.
     *
     * @return with table hint
     */
    public Optional<WithTableHintSegment> getWithTableHint() {
        return Optional.ofNullable(withTableHint);
    }
    
    /**
     * Get row set function.
     *
     * @return row set function
     */
    public Optional<FunctionSegment> getRowSetFunction() {
        return Optional.ofNullable(rowSetFunction);
    }
}
