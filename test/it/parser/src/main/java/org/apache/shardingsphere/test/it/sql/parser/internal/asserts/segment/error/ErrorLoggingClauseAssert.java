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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ErrorLoggingSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.error.ExpectedErrorLoggingClause;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Error logging clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorLoggingClauseAssert {
    
    /**
     * Assert optional error logging segment is correct with expected error logging clause.
     *
     * @param assertContext assert context
     * @param actual actual optional error logging segment
     * @param expected expected error logging clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Optional<ErrorLoggingSegment> actual, final ExpectedErrorLoggingClause expected) {
        if (null == expected) {
            assertFalse(actual.isPresent(), assertContext.getText("Actual error logging segment should not exist."));
        } else {
            assertTrue(actual.isPresent(), assertContext.getText("Actual error logging segment should exist."));
            assertIs(assertContext, actual.get(), expected);
        }
    }
    
    /**
     * Assert error logging segment is correct with expected error logging clause.
     *
     * @param assertContext assert context
     * @param actual actual error logging segment
     * @param expected expected error logging clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ErrorLoggingSegment actual, final ExpectedErrorLoggingClause expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual error logging table should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
        if (null == expected.getTag()) {
            assertNull(actual.getTag(), assertContext.getText("Actual error logging tag should not exist."));
        } else {
            assertTrue(null != actual.getTag(), assertContext.getText("Actual error logging tag should exist."));
            ExpressionAssert.assertExpression(assertContext, actual.getTag(), expected.getTag());
        }
        assertThat(assertContext.getText("Error logging reject limit assertion error: "), actual.getRejectLimit(), is(expected.getRejectLimit()));
    }
}
