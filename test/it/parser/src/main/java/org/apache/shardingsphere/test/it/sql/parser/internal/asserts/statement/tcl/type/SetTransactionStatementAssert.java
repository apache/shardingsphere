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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.tcl.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OperationScope;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.tcl.SetTransactionStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set transaction statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SetTransactionStatementAssert {
    
    /**
     * Assert set transaction statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual set transaction statement
     * @param expected expected set transaction statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SetTransactionStatement actual, final SetTransactionStatementTestCase expected) {
        assertScope(assertContext, actual, expected);
        assertIsolationLevel(assertContext, actual, expected);
        assertAccessMode(assertContext, actual, expected);
    }
    
    private static void assertScope(final SQLCaseAssertContext assertContext, final SetTransactionStatement actual, final SetTransactionStatementTestCase expected) {
        if (null == expected.getScope()) {
            assertFalse(actual.containsScope(), assertContext.getText("Actual transaction scope should not exist."));
            return;
        }
        assertTrue(actual.isDesiredScope(OperationScope.valueOf(expected.getScope())), assertContext.getText("Transaction scope assertion error."));
    }
    
    private static void assertIsolationLevel(final SQLCaseAssertContext assertContext, final SetTransactionStatement actual, final SetTransactionStatementTestCase expected) {
        if (null == expected.getIsolationLevel()) {
            assertFalse(actual.getIsolationLevel().isPresent(), assertContext.getText("Actual transaction isolation level should not exist."));
            return;
        }
        assertTrue(actual.getIsolationLevel().isPresent(), assertContext.getText("Actual transaction isolation level should exist."));
        assertThat(assertContext.getText("Transaction isolation level assertion error."), actual.getIsolationLevel().get(),
                is(TransactionIsolationLevel.valueOf(expected.getIsolationLevel())));
    }
    
    private static void assertAccessMode(final SQLCaseAssertContext assertContext, final SetTransactionStatement actual, final SetTransactionStatementTestCase expected) {
        if (null == expected.getAccessMode()) {
            assertFalse(actual.isDesiredAccessMode(TransactionAccessType.READ_ONLY), assertContext.getText("Actual transaction access mode should not be read only."));
            assertFalse(actual.isDesiredAccessMode(TransactionAccessType.READ_WRITE), assertContext.getText("Actual transaction access mode should not be read write."));
            return;
        }
        assertTrue(actual.isDesiredAccessMode(TransactionAccessType.valueOf(expected.getAccessMode())), assertContext.getText("Transaction access mode assertion error."));
    }
}
