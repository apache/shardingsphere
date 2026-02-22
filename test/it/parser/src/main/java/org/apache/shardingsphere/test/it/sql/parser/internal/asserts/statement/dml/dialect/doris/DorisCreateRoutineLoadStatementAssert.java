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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnMappingSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisCreateRoutineLoadStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.partition.PartitionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumnMapping;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris.DorisCreateRoutineLoadStatementTestCase;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;

/**
 * Create routine load statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateRoutineLoadStatementAssert {
    
    /**
     * Assert create routine load statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create routine load statement
     * @param expected expected create routine load statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        assertJobName(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertMergeType(assertContext, actual, expected);
        assertColumnSeparator(assertContext, actual, expected);
        assertColumnMappings(assertContext, actual, expected);
        assertPartitions(assertContext, actual, expected);
        assertPrecedingFilter(assertContext, actual, expected);
        assertWhere(assertContext, actual, expected);
        assertDeleteOn(assertContext, actual, expected);
        assertOrderBy(assertContext, actual, expected);
        assertJobProperties(assertContext, actual, expected);
        assertDataSource(assertContext, actual, expected);
        assertDataSourceProperties(assertContext, actual, expected);
        assertComment(assertContext, actual, expected);
    }
    
    private static void assertJobName(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (actual.getJobName().isPresent()) {
            MatcherAssert.assertThat(assertContext.getText("Job name does not match: "), actual.getJobName().get().getIdentifier().getValue(), CoreMatchers.is(expected.getJobName()));
            if (null != expected.getOwner()) {
                OwnerAssert.assertIs(assertContext, actual.getJobName().get().getOwner().orElse(null), expected.getOwner());
            }
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getTable()) {
            TableAssert.assertIs(assertContext, actual.getTable().orElse(null), expected.getTable());
        }
    }
    
    private static void assertMergeType(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getMergeType()) {
            MatcherAssert.assertThat(assertContext.getText("Merge type does not match: "), actual.getMergeType().orElse(null), CoreMatchers.is(expected.getMergeType()));
        }
    }
    
    private static void assertColumnSeparator(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getColumnSeparator()) {
            MatcherAssert.assertThat(assertContext.getText("Column separator does not match: "), actual.getColumnSeparator().orElse(null), CoreMatchers.is(expected.getColumnSeparator()));
        }
    }
    
    private static void assertColumnMappings(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (!expected.getColumnMappings().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Column mappings size does not match: "), actual.getColumnMappings().size(), CoreMatchers.is(expected.getColumnMappings().size()));
            int count = 0;
            for (ColumnMappingSegment each : actual.getColumnMappings()) {
                assertColumnMapping(assertContext, each, expected.getColumnMappings().get(count));
                count++;
            }
        }
    }
    
    private static void assertColumnMapping(final SQLCaseAssertContext assertContext, final ColumnMappingSegment actual, final ExpectedColumnMapping expected) {
        ColumnAssert.assertIs(assertContext, actual.getColumn(), expected.getColumn());
        if (null != expected.getMappingExpression()) {
            ExpressionAssert.assertExpression(assertContext, actual.getMappingExpression().orElse(null), expected.getMappingExpression());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertPartitions(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (!expected.getPartitions().isEmpty()) {
            MatcherAssert.assertThat(assertContext.getText("Partitions size does not match: "), actual.getPartitions().size(), CoreMatchers.is(expected.getPartitions().size()));
            int count = 0;
            for (PartitionSegment each : actual.getPartitions()) {
                PartitionAssert.assertIs(assertContext, each, expected.getPartitions().get(count));
                count++;
            }
        }
    }
    
    private static void assertPrecedingFilter(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getPrecedingFilter()) {
            ExpressionAssert.assertExpression(assertContext, actual.getPrecedingFilter().orElse(null), expected.getPrecedingFilter());
        }
    }
    
    private static void assertWhere(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getWhereClause()) {
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().orElse(null), expected.getWhereClause());
        }
    }
    
    private static void assertDeleteOn(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getDeleteOn()) {
            ExpressionAssert.assertExpression(assertContext, actual.getDeleteOn().orElse(null), expected.getDeleteOn());
        }
    }
    
    private static void assertOrderBy(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getOrderByClause()) {
            OrderByClauseAssert.assertIs(assertContext, actual.getOrderBy().orElse(null), expected.getOrderByClause());
        }
    }
    
    private static void assertJobProperties(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (actual.getJobProperties().isPresent() && null != expected.getJobProperties() && !expected.getJobProperties().isEmpty()) {
            Assertions.assertNotNull(actual.getJobProperties().get(), assertContext.getText("Job properties should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Job properties size does not match: "), actual.getJobProperties().get().getProperties().size(),
                    CoreMatchers.is(expected.getJobProperties().size()));
            for (int i = 0; i < expected.getJobProperties().size(); i++) {
                assertProperty(assertContext, actual.getJobProperties().get().getProperties().get(i), expected.getJobProperties().get(i));
            }
        }
    }
    
    private static void assertDataSource(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getDataSource()) {
            MatcherAssert.assertThat(assertContext.getText("Data source does not match: "), actual.getDataSource().orElse(null), CoreMatchers.is(expected.getDataSource()));
        }
    }
    
    private static void assertDataSourceProperties(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (actual.getDataSourceProperties().isPresent() && null != expected.getDataSourceProperties() && !expected.getDataSourceProperties().isEmpty()) {
            Assertions.assertNotNull(actual.getDataSourceProperties().get(), assertContext.getText("Data source properties should not be null"));
            MatcherAssert.assertThat(assertContext.getText("Data source properties size does not match: "), actual.getDataSourceProperties().get().getProperties().size(),
                    CoreMatchers.is(expected.getDataSourceProperties().size()));
            for (int i = 0; i < expected.getDataSourceProperties().size(); i++) {
                assertProperty(assertContext, actual.getDataSourceProperties().get().getProperties().get(i), expected.getDataSourceProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final PropertyTestCase expected) {
        MatcherAssert.assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), CoreMatchers.is(expected.getKey()));
        MatcherAssert.assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), CoreMatchers.is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertComment(final SQLCaseAssertContext assertContext, final DorisCreateRoutineLoadStatement actual, final DorisCreateRoutineLoadStatementTestCase expected) {
        if (null != expected.getComment()) {
            MatcherAssert.assertThat(assertContext.getText("Comment does not match: "), actual.getComment().orElse(null), CoreMatchers.is(expected.getComment()));
        }
    }
}
