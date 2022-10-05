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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.value.IdentifierValueAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.CloseStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Close statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloseStatementAssert {
    
    /**
     * Assert close statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual close statement
     * @param expected expected close statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CloseStatement actual, final CloseStatementTestCase expected) {
        assertCursorName(assertContext, actual, expected);
        assertCloseAll(assertContext, actual, expected);
    }
    
    private static void assertCursorName(final SQLCaseAssertContext assertContext, final CloseStatement actual, final CloseStatementTestCase expected) {
        if (null == expected.getCursorName()) {
            assertFalse(assertContext.getText("Actual cursor name should not exist."), actual.getCursorName().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual cursor name should exist."), actual.getCursorName().isPresent());
            IdentifierValueAssert.assertIs(assertContext, actual.getCursorName().get().getIdentifier(), expected.getCursorName(), "Close");
            SQLSegmentAssert.assertIs(assertContext, actual.getCursorName().get(), expected.getCursorName());
        }
    }
    
    private static void assertCloseAll(final SQLCaseAssertContext assertContext, final CloseStatement actual, final CloseStatementTestCase expected) {
        assertThat(assertContext.getText("Cursor's close all assertion error: "), actual.isCloseAll(), is(expected.isCloseAll()));
    }
}
