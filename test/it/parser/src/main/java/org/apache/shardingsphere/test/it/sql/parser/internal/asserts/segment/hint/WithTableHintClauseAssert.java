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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.hint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.hint.TableHintLimitedSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.hint.WithTableHintSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.hint.ExpectedTableHint;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.hint.ExpectedWithTableHintClause;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * With table hint clause assert.
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithTableHintClauseAssert {
    
    /**
     * Assert actual with table hint segment is correct with expected table hint clause.
     *
     * @param assertContext assert context
     * @param actual actual with table hint segment
     * @param expected expected with table hint clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final WithTableHintSegment actual, final ExpectedWithTableHintClause expected) {
        if (null == expected.getTableHint()) {
            assertThat(assertContext.getText("with table hint clause  assertion error: "), actual.getTableHintLimitedSegments().size(), CoreMatchers.is(expected.getTableHint().size()));
        } else {
            int count = 0;
            for (TableHintLimitedSegment each : actual.getTableHintLimitedSegments()) {
                assertTableHint(assertContext, each, expected.getTableHint().get(count));
                count++;
            }
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert table hint.
     *
     * @param assertContext assert context
     * @param actual actual table hint segment
     * @param expected expected table hint
     */
    public static void assertTableHint(final SQLCaseAssertContext assertContext, final TableHintLimitedSegment actual, final ExpectedTableHint expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual table hint should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual table hint should exist."));
            assertThat(assertContext.getText("table hint value assertion error."), actual.getValue(), is(expected.getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
}
