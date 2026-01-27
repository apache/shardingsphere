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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.MaterializedViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisCreateMaterializedViewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedMaterializedViewColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisCreateMaterializedViewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Doris create materialized view statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisCreateMaterializedViewStatementAssert {
    
    /**
     * Assert doris create materialized view statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual doris create materialized view statement
     * @param expected expected doris create materialized view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        assertMaterializedView(assertContext, actual, expected);
        assertIfNotExists(assertContext, actual, expected);
        assertColumns(assertContext, actual, expected);
        assertBuildMode(assertContext, actual, expected);
        assertRefresh(assertContext, actual, expected);
        assertKeyColumns(assertContext, actual, expected);
        assertComment(assertContext, actual, expected);
        assertPartition(assertContext, actual, expected);
        assertDistributed(assertContext, actual, expected);
        assertProperties(assertContext, actual, expected);
        assertSelect(assertContext, actual, expected);
    }
    
    private static void assertMaterializedView(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual,
                                               final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getMaterializedView()) {
            assertNotNull(actual.getMaterializedView(), assertContext.getText("Actual materialized view should exist."));
            TableAssert.assertIs(assertContext, actual.getMaterializedView(), expected.getMaterializedView());
        } else {
            assertNull(actual.getMaterializedView(), assertContext.getText("Actual materialized view should not exist."));
        }
    }
    
    private static void assertIfNotExists(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getIfNotExists()) {
            if (expected.getIfNotExists()) {
                assertTrue(actual.isIfNotExists(), assertContext.getText("Actual ifNotExists should be true."));
            } else {
                assertFalse(actual.isIfNotExists(), assertContext.getText("Actual ifNotExists should be false."));
            }
        }
    }
    
    private static void assertColumns(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getColumns() && !expected.getColumns().isEmpty()) {
            assertThat(assertContext.getText("Columns size assertion error: "), actual.getColumns().size(), is(expected.getColumns().size()));
            int count = 0;
            for (MaterializedViewColumnSegment each : actual.getColumns()) {
                ExpectedMaterializedViewColumn expectedColumn = expected.getColumns().get(count);
                SQLSegmentAssert.assertIs(assertContext, each, expectedColumn);
                ColumnAssert.assertIs(assertContext, each.getColumn(), expectedColumn.getColumn());
                if (null != expectedColumn.getComment()) {
                    assertNotNull(each.getComment(), assertContext.getText("Actual column comment should exist."));
                    SQLSegmentAssert.assertIs(assertContext, each.getComment(), expectedColumn.getComment());
                }
                count++;
            }
        }
    }
    
    private static void assertBuildMode(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getBuildMode()) {
            assertNotNull(actual.getBuildMode(), assertContext.getText("Actual build mode should exist."));
            assertThat(assertContext.getText("Build mode assertion error: "), actual.getBuildMode(), is(expected.getBuildMode()));
        }
    }
    
    private static void assertRefresh(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getRefreshMethod()) {
            assertNotNull(actual.getRefreshMethod(), assertContext.getText("Actual refresh method should exist."));
            assertThat(assertContext.getText("Refresh method assertion error: "), actual.getRefreshMethod(), is(expected.getRefreshMethod()));
        }
        if (null != expected.getRefreshTrigger()) {
            assertNotNull(actual.getRefreshTrigger(), assertContext.getText("Actual refresh trigger should exist."));
            assertThat(assertContext.getText("Refresh trigger assertion error: "), actual.getRefreshTrigger(), is(expected.getRefreshTrigger()));
        }
        if (null != expected.getRefreshIntervalExpression()) {
            assertNotNull(actual.getRefreshIntervalExpression(), assertContext.getText("Actual refresh interval expression should exist."));
            ExpressionAssert.assertExpression(assertContext, actual.getRefreshIntervalExpression(), expected.getRefreshIntervalExpression());
        }
        if (null != expected.getRefreshUnit()) {
            assertNotNull(actual.getRefreshUnit(), assertContext.getText("Actual refresh unit should exist."));
            assertThat(assertContext.getText("Refresh unit assertion error: "), actual.getRefreshUnit(), is(expected.getRefreshUnit()));
        }
        if (null != expected.getStartTime()) {
            assertNotNull(actual.getStartTime(), assertContext.getText("Actual start time should exist."));
            assertThat(assertContext.getText("Start time assertion error: "), actual.getStartTime(), is(expected.getStartTime()));
        }
    }
    
    private static void assertKeyColumns(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getDuplicateKey() && expected.getDuplicateKey()) {
            assertTrue(actual.isDuplicateKey(), assertContext.getText("Actual duplicate key should be true."));
        }
        if (null != expected.getKeyColumns() && !expected.getKeyColumns().isEmpty()) {
            assertThat(assertContext.getText("Key columns size assertion error: "), actual.getKeyColumns().size(), is(expected.getKeyColumns().size()));
            int count = 0;
            for (ColumnSegment each : actual.getKeyColumns()) {
                ColumnAssert.assertIs(assertContext, each, expected.getKeyColumns().get(count));
                count++;
            }
        }
    }
    
    private static void assertComment(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getComment()) {
            assertNotNull(actual.getComment(), assertContext.getText("Actual comment should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getComment(), expected.getComment());
        }
    }
    
    private static void assertPartition(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getPartitionColumn()) {
            assertNotNull(actual.getPartitionColumn(), assertContext.getText("Actual partition column should exist."));
            ColumnAssert.assertIs(assertContext, actual.getPartitionColumn(), expected.getPartitionColumn());
        }
        if (null != expected.getPartitionFunction()) {
            assertNotNull(actual.getPartitionFunctionName(), assertContext.getText("Actual partition function name should exist."));
            assertThat(assertContext.getText("Partition function name assertion error: "), actual.getPartitionFunctionName(), is(expected.getPartitionFunction().getFunctionName()));
            assertNotNull(actual.getPartitionFunctionColumn(), assertContext.getText("Actual partition function column should exist."));
            ColumnAssert.assertIs(assertContext, actual.getPartitionFunctionColumn(), expected.getPartitionFunction().getColumn());
            assertNotNull(actual.getPartitionFunctionUnit(), assertContext.getText("Actual partition function unit should exist."));
            assertThat(assertContext.getText("Partition function unit assertion error: "), actual.getPartitionFunctionUnit(), is(expected.getPartitionFunction().getUnit()));
        }
    }
    
    private static void assertDistributed(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getDistributeType()) {
            assertNotNull(actual.getDistributeType(), assertContext.getText("Actual distribute type should exist."));
            assertThat(assertContext.getText("Distribute type assertion error: "), actual.getDistributeType(), is(expected.getDistributeType()));
        }
        if (null != expected.getDistributeColumns() && !expected.getDistributeColumns().isEmpty()) {
            assertThat(assertContext.getText("Distribute columns size assertion error: "), actual.getDistributeColumns().size(), is(expected.getDistributeColumns().size()));
            int count = 0;
            for (String each : expected.getDistributeColumns()) {
                assertThat(assertContext.getText("Distribute column assertion error: "), actual.getDistributeColumns().get(count).getValue(), is(each));
                count++;
            }
        }
        if (null != expected.getBucketCount()) {
            assertNotNull(actual.getBucketCount(), assertContext.getText("Actual bucket count should exist."));
            assertThat(assertContext.getText("Bucket count assertion error: "), actual.getBucketCount(), is(expected.getBucketCount()));
        }
        if (null != expected.getAutoBucket() && expected.getAutoBucket()) {
            assertTrue(actual.isAutoBucket(), assertContext.getText("Actual auto bucket should be true."));
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getProperties()) {
            assertNotNull(actual.getProperties(), assertContext.getText("Actual properties should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getProperties(), expected.getProperties());
            assertThat(assertContext.getText("Properties size assertion error: "), actual.getProperties().getProperties().size(), is(expected.getProperties().getProperties().size()));
            for (int i = 0; i < expected.getProperties().getProperties().size(); i++) {
                assertProperty(assertContext, actual.getProperties().getProperties().get(i), expected.getProperties().getProperties().get(i));
            }
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertSelect(final SQLCaseAssertContext assertContext, final DorisCreateMaterializedViewStatement actual, final DorisCreateMaterializedViewStatementTestCase expected) {
        if (null != expected.getSelectClause()) {
            assertNotNull(actual.getSelectStatement(), assertContext.getText("Actual select statement should exist."));
            SelectStatementAssert.assertIs(assertContext, actual.getSelectStatement(), expected.getSelectClause());
        }
    }
}
