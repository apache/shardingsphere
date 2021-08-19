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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.ShowTablesStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Show tables statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowTablesStatementAssert {
    
    /**
     * Assert show tables statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual show tables statement
     * @param expected expected show tables statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLShowTablesStatement actual, final ShowTablesStatementTestCase expected) {
        assertSchema(assertContext, actual, expected);
        assertLike(assertContext, actual, expected);
        assertWhere(assertContext, actual, expected);
    }
    
    private static void assertSchema(final SQLCaseAssertContext assertContext, final MySQLShowTablesStatement actual, final ShowTablesStatementTestCase expected) {
        if (null != expected.getSchema()) {
            assertTrue(assertContext.getText("Actual schema should exist."), actual.getFromSchema().isPresent());
            SQLSegmentAssert.assertIs(assertContext, actual.getFromSchema().get(), expected.getSchema());
        } else {
            assertFalse(assertContext.getText("Actual schema should not exist."), actual.getFromSchema().isPresent());
        }
    }
    
    private static void assertLike(final SQLCaseAssertContext assertContext, final MySQLShowTablesStatement actual, final ShowTablesStatementTestCase expected) {
        if (null != expected.getLike()) {
            assertTrue(assertContext.getText("Actual like should exist."), actual.getLike().isPresent());
            SQLSegmentAssert.assertIs(assertContext, actual.getLike().get(), expected.getLike());
        } else {
            assertFalse(assertContext.getText("Actual like should not exist."), actual.getLike().isPresent());
        }
    }
    
    private static void assertWhere(final SQLCaseAssertContext assertContext, final MySQLShowTablesStatement actual, final ShowTablesStatementTestCase expected) {
        if (null != expected.getWhere()) {
            assertTrue(assertContext.getText("Actual where should exist."), actual.getWhere().isPresent());
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhere());
        } else {
            assertFalse(assertContext.getText("Actual where should not exist."), actual.getWhere().isPresent());
        }
    }
}
