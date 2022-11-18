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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.insert.InsertColumnsClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.insert.InsertMultiTableElementAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.insert.OnDuplicateKeyColumnsAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.statement.dml.InsertStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertSelectSubqueryClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(assertContext.getText("Actual table should not exist."), actual.getTable());
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertInsertColumnsClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getInsertColumnsClause()) {
            assertFalse(assertContext.getText("Actual insert columns segment should not exist."), actual.getInsertColumns().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual insert columns segment should exist."), actual.getInsertColumns().isPresent());
            InsertColumnsClauseAssert.assertIs(assertContext, actual.getInsertColumns().get(), expected.getInsertColumnsClause());
        }
    }
    
    private static void assertInsertValuesClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getInsertValuesClause()) {
            assertTrue(assertContext.getText("Actual insert values segment should not exist."), actual.getValues().isEmpty());
        } else {
            assertFalse(assertContext.getText("Actual insert values segment should exist."), actual.getValues().isEmpty());
            InsertValuesClauseAssert.assertIs(assertContext, actual.getValues(), expected.getInsertValuesClause());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<SetAssignmentSegment> setAssignmentSegment = InsertStatementHandler.getSetAssignmentSegment(actual);
        if (null == expected.getSetClause()) {
            assertFalse(assertContext.getText("Actual set assignment segment should not exist."), setAssignmentSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual set assignment segment should exist."), setAssignmentSegment.isPresent());
            SetClauseAssert.assertIs(assertContext, setAssignmentSegment.get(), expected.getSetClause());
        }
    }
    
    private static void assertInsertSelectClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null == expected.getSelectTestCase()) {
            assertFalse(assertContext.getText("Actual insert select segment should not exist."), actual.getInsertSelect().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual insert select segment should exist."), actual.getInsertSelect().isPresent());
            SelectStatementAssert.assertIs(assertContext, actual.getInsertSelect().get().getSelect(), expected.getSelectTestCase());
        }
    }
    
    private static void assertOnDuplicateKeyColumns(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = InsertStatementHandler.getOnDuplicateKeyColumnsSegment(actual);
        if (null == expected.getOnDuplicateKeyColumns()) {
            assertFalse(assertContext.getText("Actual on duplicate key columns segment should not exist."), onDuplicateKeyColumnsSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual on duplicate key columns segment should exist."), onDuplicateKeyColumnsSegment.isPresent());
            OnDuplicateKeyColumnsAssert.assertIs(assertContext, onDuplicateKeyColumnsSegment.get(), expected.getOnDuplicateKeyColumns());
        }
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<WithSegment> withSegment = InsertStatementHandler.getWithSegment(actual);
        if (null == expected.getWithClause()) {
            assertFalse(assertContext.getText("Actual with segment should not exist."), withSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual with segment should exist."), withSegment.isPresent());
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertOutputClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = InsertStatementHandler.getOutputSegment(actual);
        if (null == expected.getOutputClause()) {
            assertFalse(assertContext.getText("Actual output segment should not exist."), outputSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual output segment should exist."), outputSegment.isPresent());
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
    
    private static void assertInsertMultiTableElement(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<InsertMultiTableElementSegment> insertTableElementSegment = InsertStatementHandler.getInsertMultiTableElementSegment(actual);
        if (null == expected.getInsertTableElement()) {
            assertFalse(assertContext.getText("Actual insert multi table element segment should not exist."), insertTableElementSegment.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual insert multi table element segment should exist."), insertTableElementSegment.isPresent());
            InsertMultiTableElementAssert.assertIs(assertContext, insertTableElementSegment.get(), expected.getInsertTableElement());
        }
    }
    
    private static void assertSelectSubqueryClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        Optional<SubquerySegment> selectSubquery = InsertStatementHandler.getSelectSubquery(actual);
        if (null == expected.getSelectSubquery()) {
            assertFalse(assertContext.getText("Actual select subquery segment should not exist."), selectSubquery.isPresent());
        } else {
            assertTrue(assertContext.getText("Actual select subquery segment should exist."), selectSubquery.isPresent());
            SelectStatementAssert.assertIs(assertContext, selectSubquery.get().getSelect(), expected.getSelectSubquery());
        }
    }
}
