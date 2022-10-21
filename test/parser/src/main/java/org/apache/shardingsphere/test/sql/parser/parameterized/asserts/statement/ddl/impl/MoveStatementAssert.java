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
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.cursor.DirectionSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.value.IdentifierValueAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl.MoveStatementTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Move statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveStatementAssert {
    
    /**
     * Assert move statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual move statement
     * @param expected expected move statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MoveStatement actual, final MoveStatementTestCase expected) {
        assertCursorName(assertContext, actual, expected);
        assertDirection(assertContext, actual, expected);
    }
    
    private static void assertCursorName(final SQLCaseAssertContext assertContext, final MoveStatement actual, final MoveStatementTestCase expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getCursorName().getIdentifier(), expected.getCursorName(), "Move");
        SQLSegmentAssert.assertIs(assertContext, actual.getCursorName(), expected.getCursorName());
    }
    
    private static void assertDirection(final SQLCaseAssertContext assertContext, final MoveStatement actual, final MoveStatementTestCase expected) {
        if (null == expected.getDirection()) {
            assertFalse(assertContext.getText("Actual direction segment should not exist."), actual.getDirection().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual direction segment should exist."), actual.getDirection().isPresent());
            DirectionSegmentAssert.assertIs(assertContext, actual.getDirection().get(), expected.getDirection());
        }
    }
}
