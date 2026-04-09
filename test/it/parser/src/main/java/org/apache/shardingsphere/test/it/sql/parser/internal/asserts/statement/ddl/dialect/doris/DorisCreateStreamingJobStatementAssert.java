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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateStreamingJobStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateStreamingJobStatementTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create streaming job statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateStreamingJobStatementAssert {
    
    /**
     * Assert create streaming job statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create streaming job statement
     * @param expected expected create streaming job statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertComment(assertContext, actual, expected);
        assertSourceType(assertContext, actual, expected);
        assertTargetDatabase(assertContext, actual, expected);
        assertSourceProperties(assertContext, actual, expected);
        assertTargetProperties(assertContext, actual, expected);
        assertJobProperties(assertContext, actual, expected);
        assertInsertStatement(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        assertNotNull(actual.getJobName(), assertContext.getText("Job name should exist."));
        if (null != expected.getJobName()) {
            assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().getIdentifier().getValue(), is(expected.getJobName().getName()));
            SQLSegmentAssert.assertIs(assertContext, actual.getJobName(), expected.getJobName());
        }
    }
    
    private static void assertComment(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (null != expected.getComment()) {
            assertTrue(actual.getComment().isPresent(), assertContext.getText("Comment should exist."));
            assertThat(assertContext.getText("Comment value does not match: "), actual.getComment().get().getValue(), is(expected.getComment().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getComment().get(), expected.getComment());
        }
    }
    
    private static void assertSourceType(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (null != expected.getSourceType()) {
            assertTrue(actual.getSourceType().isPresent(), assertContext.getText("Source type should exist."));
            assertThat(assertContext.getText("Source type does not match: "), actual.getSourceType().get(), is(expected.getSourceType()));
        }
    }
    
    private static void assertTargetDatabase(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (null != expected.getTargetDatabase()) {
            assertTrue(actual.getTargetDatabase().isPresent(), assertContext.getText("Target database should exist."));
            assertThat(assertContext.getText("Target database does not match: "), actual.getTargetDatabase().get(), is(expected.getTargetDatabase()));
        }
    }
    
    private static void assertSourceProperties(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (!expected.getSourceProperties().isEmpty()) {
            assertTrue(actual.getSourceProperties().isPresent(), assertContext.getText("Source properties should exist."));
            assertProperties(assertContext, actual.getSourceProperties().get().getProperties(), expected.getSourceProperties(), "Source");
        }
    }
    
    private static void assertTargetProperties(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (!expected.getTargetProperties().isEmpty()) {
            assertTrue(actual.getTargetProperties().isPresent(), assertContext.getText("Target properties should exist."));
            assertProperties(assertContext, actual.getTargetProperties().get().getProperties(), expected.getTargetProperties(), "Target");
        }
    }
    
    private static void assertJobProperties(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (!expected.getJobProperties().isEmpty()) {
            assertTrue(actual.getJobProperties().isPresent(), assertContext.getText("Job properties should exist."));
            assertProperties(assertContext, actual.getJobProperties().get().getProperties(), expected.getJobProperties(), "Job");
        }
    }
    
    private static void assertInsertStatement(final SQLCaseAssertContext assertContext, final DorisCreateStreamingJobStatement actual, final DorisCreateStreamingJobStatementTestCase expected) {
        if (null != expected.getInsertTable()) {
            assertNotNull(actual.getInsertStatement(), assertContext.getText("Insert statement should exist."));
            assertTrue(actual.getInsertStatement().getTable().isPresent(), assertContext.getText("Insert target table should exist."));
            TableAssert.assertIs(assertContext, actual.getInsertStatement().getTable().get(), expected.getInsertTable());
        }
        if (null != expected.getInsertSelectTable()) {
            assertNotNull(actual.getInsertStatement(), assertContext.getText("Insert statement should exist."));
            assertTrue(actual.getInsertStatement().getInsertSelect().isPresent(), assertContext.getText("Insert select should exist."));
            SimpleTableSegment fromTable = (SimpleTableSegment) actual.getInsertStatement().getInsertSelect().get().getSelect().getFrom().orElse(null);
            assertNotNull(fromTable, assertContext.getText("Insert select from table should exist."));
            TableAssert.assertIs(assertContext, fromTable, expected.getInsertSelectTable());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final List<PropertySegment> actual, final List<PropertyTestCase> expected, final String type) {
        assertThat(assertContext.getText(type + " properties size does not match: "), actual.size(), is(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            assertProperty(assertContext, actual.get(i), expected.get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
