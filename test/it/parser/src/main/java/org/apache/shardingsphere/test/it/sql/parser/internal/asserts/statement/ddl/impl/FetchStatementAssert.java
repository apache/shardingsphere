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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.cursor.DirectionSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.FetchStatementTestCase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fetch statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FetchStatementAssert {
    
    /**
     * Assert fetch statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual fetch statement
     * @param expected expected fetch statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final FetchStatement actual, final FetchStatementTestCase expected) {
        assertCursorName(assertContext, actual, expected);
        assertDirection(assertContext, actual, expected);
    }
    
    private static void assertCursorName(final SQLCaseAssertContext assertContext, final FetchStatement actual, final FetchStatementTestCase expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getCursorName().getIdentifier(), expected.getCursorName(), "Fetch");
        SQLSegmentAssert.assertIs(assertContext, actual.getCursorName(), expected.getCursorName());
    }
    
    private static void assertDirection(final SQLCaseAssertContext assertContext, final FetchStatement actual, final FetchStatementTestCase expected) {
        if (null == expected.getDirection()) {
            assertFalse(actual.getDirection().isPresent(), assertContext.getText("Actual direction segment should not exist."));
        } else {
            assertTrue(actual.getDirection().isPresent(), assertContext.getText("Actual direction segment should exist."));
            DirectionSegmentAssert.assertIs(assertContext, actual.getDirection().get(), expected.getDirection());
        }
    }
}
