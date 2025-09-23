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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ValueReferenceSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.hint.WithTableHintClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.DerivedInsertColumnsAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertColumnsClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertExecClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.MultiTableConditionalIntoClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.MultiTableInsertIntoClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.OnDuplicateKeyColumnsAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.assignment.ValueReferenceSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.parameter.ParameterMarkerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.returning.ReturningClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.InsertStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Insert statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertStatementAssert {
    
    /**
     * Assert insert statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual insert statement
     * @param expected expected insert statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertInsertColumnsClause(assertContext, actual, expected);
        assertDerivedInsertColumns(assertContext, actual, expected);
        assertInsertValuesClause(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
        assertInsertSelectClause(assertContext, actual, expected);
        assertOnDuplicateKeyColumns(assertContext, actual, expected);
        assertValueReference(assertContext, actual, expected);
        assertWithClause(assertContext, actual, expected);
        assertOutputClause(assertContext, actual, expected);
        assertMultiTableInsertType(assertContext, actual, expected);
        assertMultiTableInsertIntoClause(assertContext, actual, expected);
        assertMultiTableConditionalIntoClause(assertContext, actual, expected);
        assertReturningClause(assertContext, actual, expected);
        assertInsertExecClause(assertContext, actual, expected);
        assertWithTableHintClause(assertContext, actual, expected);
        assertRowSetFunctionClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertFalse(actual.getTable().isPresent(), assertContext.getText("Actual table should not exist."));
        } else {
            assertTrue(actual.getTable().isPresent(), assertContext.getText("Actual table should exist."));
            TableAssert.assertIs(assertContext, actual.getTable().get(), expected.getTable());
        }
    }
    
    private static void assertInsertColumnsClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getInsertColumnsClause()) {
            assertFalse(actual.getInsertColumns().isPresent(), assertContext.getText("Actual insert columns segment should not exist."));
        } else {
            assertTrue(actual.getInsertColumns().isPresent(), assertContext.getText("Actual insert columns segment should exist."));
            InsertColumnsClauseAssert.assertIs(assertContext, actual.getInsertColumns().get(), expected.getInsertColumnsClause());
        }
    }
    
    private static void assertDerivedInsertColumns(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getDerivedInsertColumns()) {
            assertTrue(actual.getDerivedInsertColumns().isEmpty(), assertContext.getText("Actual derived insert columns should not exist."));
        } else {
            assertFalse(actual.getDerivedInsertColumns().isEmpty(), assertContext.getText("Actual derived insert columns should exist."));
            DerivedInsertColumnsAssert.assertIs(assertContext, actual.getDerivedInsertColumns(), expected.getDerivedInsertColumns());
        }
    }
    
    private static void assertInsertValuesClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getInsertValuesClause()) {
            assertTrue(actual.getValues().isEmpty(), assertContext.getText("Actual insert values segment should not exist."));
        } else {
            assertFalse(actual.getValues().isEmpty(), assertContext.getText("Actual insert values segment should exist."));
            InsertValuesClauseAssert.assertIs(assertContext, actual.getValues(), expected.getInsertValuesClause());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<SetAssignmentSegment> setAssignmentSegment = actual.getSetAssignment();
        if (null == expected.getSetClause()) {
            assertFalse(setAssignmentSegment.isPresent(), assertContext.getText("Actual set assignment segment should not exist."));
        } else {
            assertTrue(setAssignmentSegment.isPresent(), assertContext.getText("Actual set assignment segment should exist."));
            SetClauseAssert.assertIs(assertContext, setAssignmentSegment.get(), expected.getSetClause());
        }
    }
    
    private static void assertInsertSelectClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getSelectTestCase()) {
            assertFalse(actual.getInsertSelect().isPresent(), assertContext.getText("Actual insert select segment should not exist."));
        } else {
            assertTrue(actual.getInsertSelect().isPresent(), assertContext.getText("Actual insert select segment should exist."));
            ParameterMarkerAssert.assertCount(assertContext, actual.getInsertSelect().get().getSelect().getParameterCount(), expected.getSelectTestCase().getParameters().size());
            SelectStatementAssert.assertIs(assertContext, actual.getInsertSelect().get().getSelect(), expected.getSelectTestCase());
        }
    }
    
    private static void assertOnDuplicateKeyColumns(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = actual.getOnDuplicateKeyColumns();
        if (null == expected.getOnDuplicateKeyColumns()) {
            assertFalse(onDuplicateKeyColumnsSegment.isPresent(), assertContext.getText("Actual on duplicate key columns segment should not exist."));
        } else {
            assertTrue(onDuplicateKeyColumnsSegment.isPresent(), assertContext.getText("Actual on duplicate key columns segment should exist."));
            OnDuplicateKeyColumnsAssert.assertIs(assertContext, onDuplicateKeyColumnsSegment.get(), expected.getOnDuplicateKeyColumns());
        }
    }
    
    private static void assertValueReference(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<ValueReferenceSegment> valueReferenceSegment = actual.getValueReference();
        if (null == expected.getValueReference()) {
            assertFalse(valueReferenceSegment.isPresent(), assertContext.getText("Actual value reference segment should not exist."));
        } else {
            assertTrue(valueReferenceSegment.isPresent(), assertContext.getText("Actual value reference segment should exist."));
            ValueReferenceSegmentAssert.assertIs(assertContext, valueReferenceSegment.get(), expected.getValueReference());
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<WithSegment> withSegment = actual.getWith();
        if (null == expected.getWithClause()) {
            assertFalse(withSegment.isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(withSegment.isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertOutputClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = actual.getOutput();
        if (null == expected.getOutputClause()) {
            assertFalse(outputSegment.isPresent(), assertContext.getText("Actual output segment should not exist."));
        } else {
            assertTrue(outputSegment.isPresent(), assertContext.getText("Actual output segment should exist."));
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
    
    private static void assertMultiTableInsertType(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<MultiTableInsertType> multiTableInsertType = actual.getMultiTableInsertType();
        if (null == expected.getMultiTableInsertType()) {
            assertFalse(multiTableInsertType.isPresent(), assertContext.getText("Actual multi table insert type should not exist."));
        } else {
            assertTrue(multiTableInsertType.isPresent(), assertContext.getText("Actual multi table insert type should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s multiTableInsertType assertion error: ", actual.getClass().getSimpleName())), multiTableInsertType.get().name(),
                    is(expected.getMultiTableInsertType().getMultiTableInsertType()));
        }
    }
    
    private static void assertMultiTableInsertIntoClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<MultiTableInsertIntoSegment> multiTableInsertIntoSegment = actual.getMultiTableInsertInto();
        if (null == expected.getMultiTableInsertInto()) {
            assertFalse(multiTableInsertIntoSegment.isPresent(), assertContext.getText("Actual multi table insert into segment should not exist."));
        } else {
            assertTrue(multiTableInsertIntoSegment.isPresent(), assertContext.getText("Actual multi table insert into segment should exist."));
            MultiTableInsertIntoClauseAssert.assertIs(assertContext, multiTableInsertIntoSegment.get(), expected.getMultiTableInsertInto());
        }
    }
    
    private static void assertMultiTableConditionalIntoClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<MultiTableConditionalIntoSegment> multiTableConditionalIntoSegment = actual.getMultiTableConditionalInto();
        if (null == expected.getMultiTableConditionalInto()) {
            assertFalse(multiTableConditionalIntoSegment.isPresent(), assertContext.getText("Actual multi table conditional into segment should not exist."));
        } else {
            assertTrue(multiTableConditionalIntoSegment.isPresent(), assertContext.getText("Actual multi table conditional into segment should exist."));
            MultiTableConditionalIntoClauseAssert.assertIs(assertContext, multiTableConditionalIntoSegment.get(), expected.getMultiTableConditionalInto());
        }
    }
    
    private static void assertReturningClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<ReturningSegment> returningSegment = actual.getReturning();
        if (null == expected.getReturningClause()) {
            assertFalse(returningSegment.isPresent(), assertContext.getText("Actual returning segment should not exist."));
        } else {
            assertTrue(returningSegment.isPresent(), assertContext.getText("Actual returning segment should exist."));
            ReturningClauseAssert.assertIs(assertContext, returningSegment.get(), expected.getReturningClause());
        }
    }
    
    private static void assertInsertExecClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<ExecSegment> execSegment = actual.getExec();
        if (null == expected.getExecClause()) {
            assertFalse(execSegment.isPresent(), assertContext.getText("Actual exec segment should not exist."));
        } else {
            assertTrue(execSegment.isPresent(), assertContext.getText("Actual exec segment should exist."));
            InsertExecClauseAssert.assertIs(assertContext, execSegment.get(), expected.getExecClause());
        }
    }
    
    private static void assertWithTableHintClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<WithTableHintSegment> withTableHintSegment = actual.getWithTableHint();
        if (null == expected.getExpectedWithTableHintClause()) {
            assertFalse(withTableHintSegment.isPresent(), assertContext.getText("Actual with table hint should not exist."));
        } else {
            assertTrue(withTableHintSegment.isPresent(), assertContext.getText("Actual with table hint segment should exist."));
            WithTableHintClauseAssert.assertIs(assertContext, withTableHintSegment.get(), expected.getExpectedWithTableHintClause());
        }
    }
    
    private static void assertRowSetFunctionClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<FunctionSegment> rowSetFunctionSegment = actual.getRowSetFunction();
        if (null == expected.getExpectedRowSetFunctionClause()) {
            assertFalse(rowSetFunctionSegment.isPresent(), assertContext.getText("Actual row set function should not exist."));
        } else {
            assertTrue(rowSetFunctionSegment.isPresent(), assertContext.getText("Actual row set function should exist."));
            ExpressionAssert.assertFunction(assertContext, rowSetFunctionSegment.get(), expected.getExpectedRowSetFunctionClause());
        }
    }
}
