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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml.DeleteStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Delete statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteStatementAssert {
    
    /**
     * Assert delete statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual delete statement
     * @param expected expected delete statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
//        TODO to support table assert
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        if (null != expected.getWhereClause()) {
            assertTrue(assertContext.getText("Actual where segment should exist."), actual.getWhere().isPresent());
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        } else {
            assertFalse(assertContext.getText("Actual where segment should not exist."), actual.getWhere().isPresent());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        if (null != expected.getOrderByClause()) {
            assertTrue(assertContext.getText("Actual order by segment should exist."), actual.getOrderBy().isPresent());
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().get(), expected.getOrderByClause());
        } else {
            assertFalse(assertContext.getText("Actual order by segment should not exist."), actual.getOrderBy().isPresent());
        }
    }

    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = actual.getLimit();
        if (null != expected.getLimitClause()) {
            assertTrue(assertContext.getText("Actual limit segment should exist."), limitSegment.isPresent());
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        } else {
            assertFalse(assertContext.getText("Actual limit segment should not exist."), limitSegment.isPresent());
        }
    }
}
