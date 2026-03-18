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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dal.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisShowFunctionsStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.database.DatabaseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.DorisShowFunctionsStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Show functions statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisShowFunctionsStatementAssert {
    
    /**
     * Assert show functions statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual show functions statement
     * @param expected expected show functions statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        assertGlobal(assertContext, actual, expected);
        assertFull(assertContext, actual, expected);
        assertBuiltin(assertContext, actual, expected);
        assertFromDatabase(assertContext, actual, expected);
        assertLike(assertContext, actual, expected);
    }
    
    private static void assertGlobal(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        if (null != expected.getGlobal()) {
            assertThat(assertContext.getText("Global flag assertion error: "), actual.isGlobal(), is(expected.getGlobal()));
        }
    }
    
    private static void assertFull(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        if (null != expected.getFull()) {
            assertThat(assertContext.getText("Full flag assertion error: "), actual.isFull(), is(expected.getFull()));
        }
    }
    
    private static void assertBuiltin(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        if (null != expected.getBuiltin()) {
            assertThat(assertContext.getText("Builtin flag assertion error: "), actual.isBuiltin(), is(expected.getBuiltin()));
        }
    }
    
    private static void assertFromDatabase(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        if (null != expected.getFromDatabase()) {
            assertTrue(actual.getFromDatabase().isPresent(), assertContext.getText("Actual from database should exist."));
            DatabaseAssert.assertIs(assertContext, actual.getFromDatabase().get().getDatabase(), expected.getFromDatabase().getDatabase());
            SQLSegmentAssert.assertIs(assertContext, actual.getFromDatabase().get(), expected.getFromDatabase());
        }
    }
    
    private static void assertLike(final SQLCaseAssertContext assertContext, final DorisShowFunctionsStatement actual, final DorisShowFunctionsStatementTestCase expected) {
        if (null != expected.getLike()) {
            assertTrue(actual.getLike().isPresent(), assertContext.getText("Actual like segment should exist."));
            assertThat(assertContext.getText("Like pattern assertion error: "), actual.getLike().get().getPattern(), is(expected.getLike().getPattern()));
            SQLSegmentAssert.assertIs(assertContext, actual.getLike().get(), expected.getLike());
        }
    }
}
