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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.alter.CancelAlterTableStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.alter.CancelAlterTableStatementTestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;

/**
 * Cancel alter table statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CancelAlterTableStatementAssert {
    
    /**
     * Assert cancel alter table statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual cancel alter table statement
     * @param expected expected cancel alter table statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CancelAlterTableStatement actual, final CancelAlterTableStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertAlterType(assertContext, actual, expected);
        assertJobIds(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final CancelAlterTableStatement actual, final CancelAlterTableStatementTestCase expected) {
        if (null == expected.getTable()) {
            Assertions.assertNull(actual.getTable(), assertContext.getText("Actual table segment should not exist."));
        } else {
            Assertions.assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertAlterType(final SQLCaseAssertContext assertContext, final CancelAlterTableStatement actual, final CancelAlterTableStatementTestCase expected) {
        Assertions.assertNotNull(expected.getAlterType(), assertContext.getText("Expected alter type should exist."));
        Assertions.assertNotNull(actual.getAlterType(), assertContext.getText("Actual alter type should exist."));
        MatcherAssert.assertThat(assertContext.getText("Alter type assertion error: "), actual.getAlterType(), Matchers.is(expected.getAlterType()));
    }
    
    private static void assertJobIds(final SQLCaseAssertContext assertContext, final CancelAlterTableStatement actual, final CancelAlterTableStatementTestCase expected) {
        if (null == expected.getJobIds() || expected.getJobIds().isEmpty()) {
            Assertions.assertTrue(actual.getJobIds().isEmpty(), assertContext.getText("Actual job IDs should not exist."));
        } else {
            MatcherAssert.assertThat(assertContext.getText("Job IDs size assertion error: "), actual.getJobIds().size(), Matchers.is(expected.getJobIds().size()));
            for (int i = 0; i < expected.getJobIds().size(); i++) {
                MatcherAssert.assertThat(assertContext.getText(String.format("Job ID assertion error at index %d: ", i)), actual.getJobIds().get(i), Matchers.is(expected.getJobIds().get(i)));
            }
        }
    }
}
