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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.DeleteStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.InsertStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.UpdateStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.query.ExpectedPrepareStatementQuery;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertFalse(actual.getSelect().isPresent(), assertContext.getText("Actual select statement should not exist."));
        } else {
            assertTrue(actual.getSelect().isPresent(), assertContext.getText("Actual select statement should exist."));
            SelectStatementAssert.assertIs(assertContext, actual.getSelect().get(), expected.getSelectClause());
        }
    }
    
    private static void assertInsert(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getInsertClause()) {
            assertFalse(actual.getInsert().isPresent(), assertContext.getText("Actual insert statement should not exist."));
        } else {
            assertTrue(actual.getInsert().isPresent(), assertContext.getText("Actual insert statement should exist."));
            InsertStatementAssert.assertIs(assertContext, actual.getInsert().get(), expected.getInsertClause());
        }
    }
    
    private static void assertUpdate(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getUpdateClause()) {
            assertFalse(actual.getUpdate().isPresent(), assertContext.getText("Actual update statement should not exist."));
        } else {
            assertTrue(actual.getUpdate().isPresent(), assertContext.getText("Actual update statement should exist."));
            UpdateStatementAssert.assertIs(assertContext, actual.getUpdate().get(), expected.getUpdateClause());
        }
    }
    
    private static void assertDelete(final SQLCaseAssertContext assertContext, final PrepareStatementQuerySegment actual, final ExpectedPrepareStatementQuery expected) {
        if (null == expected.getDeleteClause()) {
            assertFalse(actual.getDelete().isPresent(), assertContext.getText("Actual delete statement should not exist."));
        } else {
            assertTrue(actual.getDelete().isPresent(), assertContext.getText("Actual delete statement should exist."));
            DeleteStatementAssert.assertIs(assertContext, actual.getDelete().get(), expected.getDeleteClause());
        }
    }
}
