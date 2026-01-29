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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CancelBuildIndexStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.index.CancelBuildIndexStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cancel build index statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CancelBuildIndexStatementAssert {
    
    /**
     * Assert cancel build index statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cancel build index statement
     * @param expected expected cancel build index statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CancelBuildIndexStatement actual, final CancelBuildIndexStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertJobIds(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CancelBuildIndexStatement actual, final CancelBuildIndexStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertJobIds(final SQLCaseAssertContext assertContext, final CancelBuildIndexStatement actual, final CancelBuildIndexStatementTestCase expected) {
        if (null == expected.getJobIds() || expected.getJobIds().isEmpty()) {
            assertTrue(actual.getJobIds().isEmpty(), assertContext.getText("Actual job IDs should not exist."));
        } else {
            assertThat(assertContext.getText("Job IDs size assertion error: "),
                    actual.getJobIds().size(), is(expected.getJobIds().size()));
            for (int i = 0; i < expected.getJobIds().size(); i++) {
                assertThat(assertContext.getText(String.format("Job ID assertion error at index %d: ", i)),
                        actual.getJobIds().get(i), is(expected.getJobIds().get(i)));
            }
        }
    }
}
