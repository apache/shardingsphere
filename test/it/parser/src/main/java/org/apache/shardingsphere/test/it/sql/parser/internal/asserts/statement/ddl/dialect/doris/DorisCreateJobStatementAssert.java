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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobCommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobScheduleSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.job.ExpectedJobSchedule;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateJobStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateJobStatementAssert {
    
    /**
     * Assert create job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create job statement
     * @param expected expected create job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateJobStatement actual, final DorisCreateJobStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertSchedule(assertContext, actual, expected);
        assertComment(assertContext, actual, expected);
        assertInsertStatement(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisCreateJobStatement actual, final DorisCreateJobStatementTestCase expected) {
        assertNotNull(actual.getJobName(), assertContext.getText("Job name should exist."));
        assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().getIdentifier().getValue(), is(expected.getJobName().getName()));
        SQLSegmentAssert.assertIs(assertContext, actual.getJobName(), expected.getJobName());
    }
    
    private static void assertSchedule(final SQLCaseAssertContext assertContext, final DorisCreateJobStatement actual, final DorisCreateJobStatementTestCase expected) {
        assertNotNull(actual.getSchedule(), assertContext.getText("Schedule should exist."));
        assertNotNull(expected.getSchedule(), assertContext.getText("Expected schedule should exist."));
        JobScheduleSegment actualSchedule = actual.getSchedule();
        ExpectedJobSchedule expectedSchedule = expected.getSchedule();
        SQLSegmentAssert.assertIs(assertContext, actualSchedule, expectedSchedule);
        assertThat(assertContext.getText("Every schedule flag does not match: "), actualSchedule.isEverySchedule(), is(expectedSchedule.isEverySchedule()));
        if (null != expectedSchedule.getAtTimestamp()) {
            assertTrue(actualSchedule.getAtTimestamp().isPresent(), assertContext.getText("At timestamp should exist."));
            assertThat(assertContext.getText("At timestamp value does not match: "), actualSchedule.getAtTimestamp().get().getValue(), is(expectedSchedule.getAtTimestamp().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actualSchedule.getAtTimestamp().get(), expectedSchedule.getAtTimestamp());
        }
        if (null != expectedSchedule.getInterval()) {
            assertTrue(actualSchedule.getInterval().isPresent(), assertContext.getText("Interval should exist."));
            assertThat(assertContext.getText("Interval value does not match: "), actualSchedule.getInterval().get().getValue(), is(expectedSchedule.getInterval().getValue()));
            assertThat(assertContext.getText("Interval unit does not match: "), actualSchedule.getInterval().get().getUnit(), is(expectedSchedule.getInterval().getUnit()));
            SQLSegmentAssert.assertIs(assertContext, actualSchedule.getInterval().get(), expectedSchedule.getInterval());
        }
        if (null != expectedSchedule.getStartsTimestamp()) {
            assertTrue(actualSchedule.getStartsTimestamp().isPresent(), assertContext.getText("Starts timestamp should exist."));
            assertThat(assertContext.getText("Starts timestamp value does not match: "), actualSchedule.getStartsTimestamp().get().getValue(), is(expectedSchedule.getStartsTimestamp().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actualSchedule.getStartsTimestamp().get(), expectedSchedule.getStartsTimestamp());
        }
        if (null != expectedSchedule.getEndsTimestamp()) {
            assertTrue(actualSchedule.getEndsTimestamp().isPresent(), assertContext.getText("Ends timestamp should exist."));
            assertThat(assertContext.getText("Ends timestamp value does not match: "), actualSchedule.getEndsTimestamp().get().getValue(), is(expectedSchedule.getEndsTimestamp().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actualSchedule.getEndsTimestamp().get(), expectedSchedule.getEndsTimestamp());
        }
    }
    
    private static void assertComment(final SQLCaseAssertContext assertContext, final DorisCreateJobStatement actual, final DorisCreateJobStatementTestCase expected) {
        if (null != expected.getComment()) {
            assertTrue(actual.getComment().isPresent(), assertContext.getText("Comment should exist."));
            JobCommentSegment actualComment = actual.getComment().get();
            assertThat(assertContext.getText("Comment value does not match: "), actualComment.getValue(), is(expected.getComment().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actualComment, expected.getComment());
        }
    }
    
    private static void assertInsertStatement(final SQLCaseAssertContext assertContext, final DorisCreateJobStatement actual, final DorisCreateJobStatementTestCase expected) {
        assertNotNull(actual.getInsertStatement(), assertContext.getText("Insert statement should exist."));
        if (null != expected.getInsertTable()) {
            assertTrue(actual.getInsertStatement().getTable().isPresent(), assertContext.getText("Insert target table should exist."));
            TableAssert.assertIs(assertContext, actual.getInsertStatement().getTable().get(), expected.getInsertTable());
        }
        if (null != expected.getInsertSelectTable()) {
            assertTrue(actual.getInsertStatement().getInsertSelect().isPresent(), assertContext.getText("Insert select should exist."));
            SimpleTableSegment fromTable = (SimpleTableSegment) actual.getInsertStatement().getInsertSelect().get().getSelect().getFrom().orElse(null);
            assertNotNull(fromTable, assertContext.getText("Insert select from table should exist."));
            TableAssert.assertIs(assertContext, fromTable, expected.getInsertSelectTable());
        }
        if (null != expected.getInsertSelectWhere()) {
            assertTrue(actual.getInsertStatement().getInsertSelect().isPresent(), assertContext.getText("Insert select should exist."));
            assertTrue(actual.getInsertStatement().getInsertSelect().get().getSelect().getWhere().isPresent(), assertContext.getText("Insert select where should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getInsertStatement().getInsertSelect().get().getSelect().getWhere().get(), expected.getInsertSelectWhere());
        }
    }
}
