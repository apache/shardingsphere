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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.DeleteStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.InsertStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.UpdateStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.query.ExpectedPrepareStatementQuery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Prepare statement query assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrepareStatementQueryAssert {
    
    /**
     * Assert actual prepare statement query segment is correct with expected prepare statement query.
     *
     * @param assertContext assert context
     * @param actual actual prepare statement query segment
     * @param expected expected prepare statement query
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        assertSelect(assertContext, actual, expected);
        assertInsert(assertContext, actual, expected);
        assertUpdate(assertContext, actual, expected);
        assertDelete(assertContext, actual, expected);
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertSelect(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getSelectClause()) {
            assertFalse(assertContext.getText("Actual select statement should not exist."), actual.getSelect().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual select statement should exist."), actual.getSelect().isPresent());
            SelectStatementAssert.assertIs(assertContext, actual.getSelect().get(), expected.getSelectClause());
        }
    }
    
    private static void assertInsert(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getInsertClause()) {
            assertFalse(assertContext.getText("Actual insert statement should not exist."), actual.getInsert().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual insert statement should exist."), actual.getInsert().isPresent());
            InsertStatementAssert.assertIs(assertContext, actual.getInsert().get(), expected.getInsertClause());
        }
    }
    
    private static void assertUpdate(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getUpdateClause()) {
            assertFalse(assertContext.getText("Actual update statement should not exist."), actual.getUpdate().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual update statement should exist."), actual.getUpdate().isPresent());
            UpdateStatementAssert.assertIs(assertContext, actual.getUpdate().get(), expected.getUpdateClause());
        }
    }
    
    private static void assertDelete(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getDeleteClause()) {
            assertFalse(assertContext.getText("Actual delete statement should not exist."), actual.getDelete().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual delete statement should exist."), actual.getDelete().isPresent());
            DeleteStatementAssert.assertIs(assertContext, actual.getDelete().get(), expected.getDeleteClause());
        }
    }
}
