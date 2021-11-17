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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.skipped;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.skipped.ParseSkippedStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.skipped.ParseSkippedStatementHandler;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.skipped.ParseSkippedStatementTestCase;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Parse skipped statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParseSkippedStatementAssert {
    
    /**
     * Assert parse skipped statement is correct with expected parser result.
     * 
     * @param assertContext assert context
     * @param actual actual parse skipped statement
     * @param expected expected parse skipped statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ParseSkippedStatement actual, final ParseSkippedStatementTestCase expected) {
        assertSql(assertContext, actual, expected);
    }
    
    private static void assertSql(final SQLCaseAssertContext assertContext, final ParseSkippedStatement actual, final ParseSkippedStatementTestCase expected) {
        Optional<String> sql = ParseSkippedStatementHandler.getSql(actual);
        if (null != expected.getSql()) {
            assertTrue(assertContext.getText("Actual sql should exist."), sql.isPresent());
            assertEquals(sql.get(), expected.getSql());
        } else {
            assertFalse(assertContext.getText("Actual sql should not exist."), sql.isPresent());
        }
    }
}
