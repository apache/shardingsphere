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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.dialect.doris;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.BrokerLoadDataDescSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisBrokerLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.assignment.AssignmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.BrokerLoadDataDescTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisBrokerLoadStatementTestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Broker load statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisBrokerLoadStatementAssert {
    
    /**
     * Assert broker load statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual broker load statement
     * @param expected expected broker load statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        assertLoadLabel(assertContext, actual, expected);
        assertOwner(assertContext, actual, expected);
        assertDataDescs(assertContext, actual, expected);
        assertBrokerType(assertContext, actual, expected);
        assertBrokerName(assertContext, actual, expected);
        assertBrokerProperties(assertContext, actual, expected);
        assertLoadProperties(assertContext, actual, expected);
        assertComment(assertContext, actual, expected);
    }
    
    private static void assertLoadLabel(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (null != expected.getLoadLabel()) {
            MatcherAssert.assertThat(assertContext.getText("Load label does not match: "), actual.getLoadLabel(), CoreMatchers.is(expected.getLoadLabel()));
        }
    }
    
    private static void assertOwner(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (null != expected.getOwner()) {
            DatabaseSegment database = actual.getDatabase().orElse(null);
            assertNotNull(database, assertContext.getText("Database should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Database name does not match: "), database.getIdentifier().getValue(), CoreMatchers.is(expected.getOwner().getName()));
            SQLSegmentAssert.assertIs(assertContext, database, expected.getOwner());
        }
    }
    
    private static void assertDataDescs(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (!expected.getDataDescs().isEmpty()) {
            List<BrokerLoadDataDescSegment> actualDescs = new ArrayList<>(actual.getDataDescs());
            MatcherAssert.assertThat(assertContext.getText("Data descs size does not match: "), actualDescs.size(), CoreMatchers.is(expected.getDataDescs().size()));
            for (int i = 0; i < expected.getDataDescs().size(); i++) {
                assertDataDesc(assertContext, actualDescs.get(i), expected.getDataDescs().get(i));
            }
        }
    }
    
    private static void assertDataDesc(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        assertMergeType(assertContext, actual, expected);
        assertFilePaths(assertContext, actual, expected);
        assertNegative(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
        assertColumnSeparator(assertContext, actual, expected);
        assertLineDelimiter(assertContext, actual, expected);
        assertFormatType(assertContext, actual, expected);
        assertCompressType(assertContext, actual, expected);
        assertColumnList(assertContext, actual, expected);
        assertColumnsFromPath(assertContext, actual, expected);
        assertSetAssignments(assertContext, actual, expected);
        assertPrecedingFilter(assertContext, actual, expected);
        assertWhereExpr(assertContext, actual, expected);
        assertDeleteOnExpr(assertContext, actual, expected);
        assertOrderByColumn(assertContext, actual, expected);
        assertDataProperties(assertContext, actual, expected);
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertMergeType(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getMergeType()) {
            MatcherAssert.assertThat(assertContext.getText("Merge type does not match: "), actual.getMergeType().orElse(null), CoreMatchers.is(expected.getMergeType()));
        }
    }
    
    private static void assertFilePaths(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getFilePaths()) {
            List<String> expectedPaths = Arrays.asList(expected.getFilePaths().split(","));
            MatcherAssert.assertThat(assertContext.getText("File paths size does not match: "), actual.getFilePaths().size(), CoreMatchers.is(expectedPaths.size()));
            List<String> actualPaths = new ArrayList<>(actual.getFilePaths());
            for (int i = 0; i < expectedPaths.size(); i++) {
                MatcherAssert.assertThat(assertContext.getText("File path does not match: "), actualPaths.get(i), CoreMatchers.is(expectedPaths.get(i).trim()));
            }
        }
    }
    
    private static void assertNegative(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getNegative()) {
            MatcherAssert.assertThat(assertContext.getText("Negative does not match: "), actual.isNegative(), CoreMatchers.is(expected.getNegative()));
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (!expected.getPartitions().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Partitions size does not match: "), actual.getPartitions().size(), CoreMatchers.is(expected.getPartitions().size()));
            int count = 0;
            for (PartitionSegment each : actual.getPartitions()) {
                PartitionAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
                count++;
            }
        }
    }
    
    private static void assertColumnSeparator(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getColumnSeparator()) {
            MatcherAssert.assertThat(assertContext.getText("Column separator does not match: "), actual.getColumnSeparator().orElse(null), CoreMatchers.is(expected.getColumnSeparator()));
        }
    }
    
    private static void assertLineDelimiter(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getLineDelimiter()) {
            MatcherAssert.assertThat(assertContext.getText("Line delimiter does not match: "), actual.getLineDelimiter().orElse(null), CoreMatchers.is(expected.getLineDelimiter()));
        }
    }
    
    private static void assertFormatType(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getFormatType()) {
            MatcherAssert.assertThat(assertContext.getText("Format type does not match: "), actual.getFormatType().orElse(null), CoreMatchers.is(expected.getFormatType()));
        }
    }
    
    private static void assertCompressType(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getCompressType()) {
            MatcherAssert.assertThat(assertContext.getText("Compress type does not match: "), actual.getCompressType().orElse(null), CoreMatchers.is(expected.getCompressType()));
        }
    }
    
    private static void assertColumnList(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (!expected.getColumnList().isEmpty()) {
            List<ColumnSegment> actualCols = new ArrayList<>(actual.getColumnList());
            MatcherAssert.assertThat(assertContext.getText("Column list size does not match: "), actualCols.size(), CoreMatchers.is(expected.getColumnList().size()));
            for (int i = 0; i < expected.getColumnList().size(); i++) {
                ColumnAssert.assertIs(assertContext, actualCols.get(i), expected.getColumnList().get(i));
            }
        }
    }
    
    private static void assertColumnsFromPath(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (!expected.getColumnsFromPath().isEmpty()) {
            List<ColumnSegment> actualCols = new ArrayList<>(actual.getColumnsFromPath());
            MatcherAssert.assertThat(assertContext.getText("Columns from path size does not match: "), actualCols.size(), CoreMatchers.is(expected.getColumnsFromPath().size()));
            for (int i = 0; i < expected.getColumnsFromPath().size(); i++) {
                ColumnAssert.assertIs(assertContext, actualCols.get(i), expected.getColumnsFromPath().get(i));
            }
        }
    }
    
    private static void assertSetAssignments(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (!expected.getSetAssignments().isEmpty()) {
            List<ColumnAssignmentSegment> actualAssignments = new ArrayList<>(actual.getSetAssignments());
            MatcherAssert.assertThat(assertContext.getText("Set assignments size does not match: "), actualAssignments.size(), CoreMatchers.is(expected.getSetAssignments().size()));
            for (int i = 0; i < expected.getSetAssignments().size(); i++) {
                AssignmentAssert.assertIs(assertContext, actualAssignments.get(i), expected.getSetAssignments().get(i));
            }
        }
    }
    
    private static void assertPrecedingFilter(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getPrecedingFilter()) {
            ExpressionAssert.assertExpression(assertContext, actual.getPrecedingFilter().orElse(null), expected.getPrecedingFilter());
        }
    }
    
    private static void assertWhereExpr(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getWhereExpr()) {
            ExpressionAssert.assertExpression(assertContext, actual.getWhereExpr().orElse(null), expected.getWhereExpr());
        }
    }
    
    private static void assertDeleteOnExpr(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getDeleteOnExpr()) {
            ExpressionAssert.assertExpression(assertContext, actual.getDeleteOnExpr().orElse(null), expected.getDeleteOnExpr());
        }
    }
    
    private static void assertOrderByColumn(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (null != expected.getOrderByColumn()) {
            MatcherAssert.assertThat(assertContext.getText("Order by column does not match: "), actual.getOrderByColumn().orElse(null), CoreMatchers.is(expected.getOrderByColumn()));
        }
    }
    
    private static void assertDataProperties(final SQLCaseAssertContext assertContext, final BrokerLoadDataDescSegment actual, final BrokerLoadDataDescTestCase expected) {
        if (!expected.getDataProperties().isEmpty()) {
            assertNotNull(actual.getDataProperties().orElse(null), assertContext.getText("Data properties should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Data properties size does not match: "), actual.getDataProperties().get().getProperties().size(),
                    CoreMatchers.is(expected.getDataProperties().size()));
            for (int i = 0; i < expected.getDataProperties().size(); i++) {
                assertProperty(assertContext, actual.getDataProperties().get().getProperties().get(i), expected.getDataProperties().get(i));
            }
        }
    }
    
    private static void assertBrokerType(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (null != expected.getBrokerType()) {
            MatcherAssert.assertThat(assertContext.getText("Broker type does not match: "), actual.getBrokerType(), CoreMatchers.is(expected.getBrokerType()));
        }
    }
    
    private static void assertBrokerName(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (null != expected.getBrokerName()) {
            MatcherAssert.assertThat(assertContext.getText("Broker name does not match: "), actual.getBrokerName().orElse(null), CoreMatchers.is(expected.getBrokerName()));
        }
    }
    
    private static void assertBrokerProperties(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (actual.getBrokerProperties().isPresent() && !expected.getBrokerProperties().isEmpty()) {
            assertNotNull(actual.getBrokerProperties().get(), assertContext.getText("Broker properties should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Broker properties size does not match: "), actual.getBrokerProperties().get().getProperties().size(),
                    CoreMatchers.is(expected.getBrokerProperties().size()));
            for (int i = 0; i < expected.getBrokerProperties().size(); i++) {
                assertProperty(assertContext, actual.getBrokerProperties().get().getProperties().get(i), expected.getBrokerProperties().get(i));
            }
        }
    }
    
    private static void assertLoadProperties(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (actual.getLoadProperties().isPresent() && !expected.getLoadProperties().isEmpty()) {
            assertNotNull(actual.getLoadProperties().get(), assertContext.getText("Load properties should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Load properties size does not match: "), actual.getLoadProperties().get().getProperties().size(),
                    CoreMatchers.is(expected.getLoadProperties().size()));
            for (int i = 0; i < expected.getLoadProperties().size(); i++) {
                assertProperty(assertContext, actual.getLoadProperties().get().getProperties().get(i), expected.getLoadProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), CoreMatchers.is(expected.getKey()));
        MatcherAssert.assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), CoreMatchers.is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertComment(final SQLCaseAssertContext assertContext, final DorisBrokerLoadStatement actual, final DorisBrokerLoadStatementTestCase expected) {
        if (null != expected.getComment()) {
            MatcherAssert.assertThat(assertContext.getText("Comment does not match: "), actual.getComment().orElse(null), CoreMatchers.is(expected.getComment()));
        }
    }
}
