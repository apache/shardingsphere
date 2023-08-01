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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertColumnsClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertMultiTableElementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.insert.OnDuplicateKeyColumnsAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.returning.ReturningClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.InsertStatementTestCase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertInsertValuesClause(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
        assertInsertSelectClause(assertContext, actual, expected);
        assertOnDuplicateKeyColumns(assertContext, actual, expected);
        assertWithClause(assertContext, actual, expected);
        assertOutputClause(assertContext, actual, expected);
        assertInsertMultiTableElement(assertContext, actual, expected);
        assertReturningClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
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
    
    private static void assertInsertValuesClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getInsertValuesClause()) {
            assertTrue(actual.getValues().isEmpty(), assertContext.getText("Actual insert values segment should not exist."));
        } else {
            assertFalse(actual.getValues().isEmpty(), assertContext.getText("Actual insert values segment should exist."));
            InsertValuesClauseAssert.assertIs(assertContext, actual.getValues(), expected.getInsertValuesClause());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(actual);
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
            SelectStatementAssert.assertIs(assertContext, actual.getInsertSelect().get().getSelect(), expected.getSelectTestCase());
        }
    }
    
    private static void assertOnDuplicateKeyColumns(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(actual);
        if (null == expected.getOnDuplicateKeyColumns()) {
            assertFalse(onDuplicateKeyColumnsSegment.isPresent(), assertContext.getText("Actual on duplicate key columns segment should not exist."));
        } else {
            assertTrue(onDuplicateKeyColumnsSegment.isPresent(), assertContext.getText("Actual on duplicate key columns segment should exist."));
            OnDuplicateKeyColumnsAssert.assertIs(assertContext, onDuplicateKeyColumnsSegment.get(), expected.getOnDuplicateKeyColumns());
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(actual);
        if (null == expected.getWithClause()) {
            assertFalse(withSegment.isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(withSegment.isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertOutputClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = InsertStatementHandler.getOutputSegment(actual);
        if (null == expected.getOutputClause()) {
            assertFalse(outputSegment.isPresent(), assertContext.getText("Actual output segment should not exist."));
        } else {
            assertTrue(outputSegment.isPresent(), assertContext.getText("Actual output segment should exist."));
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
    
    private static void assertInsertMultiTableElement(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<InsertMultiTableElementSegment> insertTableElementSegment = InsertStatementHandler.getInsertMultiTableElementSegment(actual);
        if (null == expected.getInsertTableElement()) {
            assertFalse(insertTableElementSegment.isPresent(), assertContext.getText("Actual insert multi table element segment should not exist."));
        } else {
            assertTrue(insertTableElementSegment.isPresent(), assertContext.getText("Actual insert multi table element segment should exist."));
            InsertMultiTableElementAssert.assertIs(assertContext, insertTableElementSegment.get(), expected.getInsertTableElement());
        }
    }
    
    private static void assertReturningClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<ReturningSegment> returningSegment = InsertStatementHandler.getReturningSegment(actual);
        if (null == expected.getReturningClause()) {
            assertFalse(returningSegment.isPresent(), assertContext.getText("Actual returning segment should not exist."));
        } else {
            assertTrue(returningSegment.isPresent(), assertContext.getText("Actual returning segment should exist."));
            ReturningClauseAssert.assertIs(assertContext, returningSegment.get(), expected.getReturningClause());
        }
    }
}
