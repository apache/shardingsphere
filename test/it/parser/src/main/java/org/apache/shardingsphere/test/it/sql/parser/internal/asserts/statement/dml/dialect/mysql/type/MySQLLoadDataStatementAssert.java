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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.mysql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.ColNameOrUserVarSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.mysql.MySQLLoadDataStatementTestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Load data statement assert for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLLoadDataStatementAssert {
    
    /**
     * Assert load data statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual load data statement
     * @param expected expected load data statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        assertLocal(assertContext, actual, expected);
        assertFileName(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
        assertColumnSeparator(assertContext, actual, expected);
        assertLineDelimiter(assertContext, actual, expected);
        assertIgnoreLines(assertContext, actual, expected);
        assertColumnList(assertContext, actual, expected);
        assertSetAssignments(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
    }
    
    private static void assertLocal(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText("LOCAL flag does not match: "), actual.isLocal(), CoreMatchers.is(null != expected.getLocal() && expected.getLocal()));
    }
    
    private static void assertFileName(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null != expected.getFileName()) {
            assertNotNull(actual.getFileName(), assertContext.getText("File name segment should not be null"));
            MatcherAssert.assertThat(assertContext.getText("File name value does not match: "), actual.getFileName().getValue(), CoreMatchers.is(expected.getFileName().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getFileName(), expected.getFileName());
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual table should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (!expected.getPartitions().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Partitions size does not match: "), actual.getPartitions().size(), CoreMatchers.is(expected.getPartitions().size()));
            int count = 0;
            for (PartitionSegment each : actual.getPartitions()) {
                PartitionAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
                count++;
            }
        }
    }
    
    private static void assertColumnSeparator(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null != expected.getColumnSeparator()) {
            assertNotNull(actual.getColumnSeparator().orElse(null), assertContext.getText("Column separator should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Column separator value does not match: "), actual.getColumnSeparator().get().getValue(),
                    CoreMatchers.is(expected.getColumnSeparator().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getColumnSeparator().get(), expected.getColumnSeparator());
        }
    }
    
    private static void assertLineDelimiter(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null != expected.getLineDelimiter()) {
            assertNotNull(actual.getLineDelimiter().orElse(null), assertContext.getText("Line delimiter should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Line delimiter value does not match: "), actual.getLineDelimiter().get().getValue(),
                    CoreMatchers.is(expected.getLineDelimiter().getValue()));
            SQLSegmentAssert.assertIs(assertContext, actual.getLineDelimiter().get(), expected.getLineDelimiter());
        }
    }
    
    private static void assertIgnoreLines(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null != expected.getIgnoreLines()) {
            assertNotNull(actual.getIgnoreLines().orElse(null), assertContext.getText("Ignore lines segment should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Ignore lines number does not match: "), actual.getIgnoreLines().get().getNumber(), CoreMatchers.is(expected.getIgnoreLines().getNumber()));
            MatcherAssert.assertThat(assertContext.getText("Ignore lines unit does not match: "), actual.getIgnoreLines().get().getUnit(), CoreMatchers.is(expected.getIgnoreLines().getUnit()));
            SQLSegmentAssert.assertIs(assertContext, actual.getIgnoreLines().get(), expected.getIgnoreLines());
        }
    }
    
    private static void assertColumnList(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (!expected.getColumns().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Column list size does not match: "), actual.getColumnList().size(), CoreMatchers.is(expected.getColumns().size()));
            int count = 0;
            for (ColNameOrUserVarSegment each : actual.getColumnList()) {
                ExpectedPartition exp = expected.getColumns().get(count);
                MatcherAssert.assertThat(assertContext.getText("Column name does not match: "), each.getIdentifier().getValue(), CoreMatchers.is(exp.getName()));
                SQLSegmentAssert.assertIs(assertContext, each, exp);
                count++;
            }
        }
    }
    
    private static void assertSetAssignments(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (null != expected.getSetAssignments()) {
            assertNotNull(actual.getSetAssignments().orElse(null), assertContext.getText("Set assignments segment should not be null"));
            SetClauseAssert.assertIs(assertContext, actual.getSetAssignments().get(), expected.getSetAssignments());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final MySQLLoadDataStatement actual, final MySQLLoadDataStatementTestCase expected) {
        if (!expected.getProperties().isEmpty()) {
            assertNotNull(actual.getProperties().orElse(null), assertContext.getText("Actual properties should exist."));
            MatcherAssert.assertThat(assertContext.getText("Properties size does not match: "), actual.getProperties().get().getProperties().size(), CoreMatchers.is(expected.getProperties().size()));
            for (int i = 0; i < expected.getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().get().getProperties().get(i), expected.getProperties().get(i));
            }
        } else if (actual.getProperties().isPresent() && !actual.getProperties().get().getProperties().isEmpty()) {
            assertFalse(expected.getProperties().isEmpty(), assertContext.getText("Expected properties should exist when actual has properties."));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), CoreMatchers.is(expected.getKey()));
        MatcherAssert.assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), CoreMatchers.is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
