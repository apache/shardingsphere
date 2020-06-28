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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertColumnsClauseAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertValuesClauseAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.cases.domain.statement.dml.ReplaceStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.ReplaceStatement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Replace statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplaceStatementAssert {
    
    /**
     * Assert insert statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual insert statement
     * @param expected expected insert statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ReplaceStatement actual, final ReplaceStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertReplaceColumnsClause(assertContext, actual, expected);
        assertReplaceValuesClause(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final ReplaceStatement actual, final ReplaceStatementTestCase expected) {
        TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
    }
    
    private static void assertReplaceColumnsClause(final SQLCaseAssertContext assertContext, final ReplaceStatement actual, final ReplaceStatementTestCase expected) {
        if (null != expected.getReplaceColumnsClause()) {
            assertTrue(assertContext.getText("Actual insert columns segment should exist."), actual.getReplaceColumns().isPresent());
            InsertColumnsClauseAssert.assertIs(assertContext, actual.getReplaceColumns().get(), expected.getReplaceColumnsClause());    
        } else {
            assertFalse(assertContext.getText("Actual insert columns segment should not exist."), actual.getReplaceColumns().isPresent());
        }
    }
    
    private static void assertReplaceValuesClause(final SQLCaseAssertContext assertContext, final ReplaceStatement actual, final ReplaceStatementTestCase expected) {
        if (null != expected.getReplaceValuesClause()) {
            assertFalse(assertContext.getText("Actual insert values segment should exist."), actual.getValues().isEmpty());
            InsertValuesClauseAssert.assertIs(assertContext, actual.getValues(), expected.getReplaceValuesClause());
        } else {
            assertTrue(assertContext.getText("Actual insert values segment should not exist."), actual.getValues().isEmpty());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final ReplaceStatement actual, final ReplaceStatementTestCase expected) {
        if (null != expected.getSetClause()) {
            assertTrue(assertContext.getText("Actual set assignment segment should exist."), actual.getSetAssignment().isPresent());
            SetClauseAssert.assertIs(assertContext, actual.getSetAssignment().get(), expected.getSetClause());
        } else {
            assertFalse(assertContext.getText("Actual set assignment segment should not exist."), actual.getSetAssignment().isPresent());
        }
    }
}
