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
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterMaterializedViewStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterMaterializedViewStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Doris alter materialized view statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterMaterializedViewStatementAssert {
    
    /**
     * Assert doris alter materialized view statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual doris alter materialized view statement
     * @param expected expected doris alter materialized view statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
        assertMaterializedView(assertContext, actual, expected);
        assertRename(assertContext, actual, expected);
        assertRefresh(assertContext, actual, expected);
        assertReplace(assertContext, actual, expected);
        assertSetProperties(assertContext, actual, expected);
    }
    
    private static void assertMaterializedView(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
        if (null != expected.getMaterializedView()) {
            assertNotNull(actual.getMaterializedView(), assertContext.getText("Actual materialized view should exist."));
            TableAssert.assertIs(assertContext, actual.getMaterializedView(), expected.getMaterializedView());
        } else {
            assertNull(actual.getMaterializedView(), assertContext.getText("Actual materialized view should not exist."));
        }
    }
    
    private static void assertRename(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
        if (null != expected.getRenameValue()) {
            assertNotNull(actual.getRenameValue(), assertContext.getText("Actual rename value should exist."));
            assertThat(assertContext.getText("Rename value assertion error: "), actual.getRenameValue().getValue(), is(expected.getRenameValue()));
        }
    }
    
    private static void assertRefresh(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
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
    
    private static void assertReplace(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
        if (null != expected.getReplaceWithView()) {
            assertNotNull(actual.getReplaceWithView(), assertContext.getText("Actual replace with view should exist."));
            assertThat(assertContext.getText("Replace with view assertion error: "), actual.getReplaceWithView().getValue(), is(expected.getReplaceWithView()));
        }
        if (null != expected.getReplaceProperties()) {
            assertNotNull(actual.getReplaceProperties(), assertContext.getText("Actual replace properties should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getReplaceProperties(), expected.getReplaceProperties());
            assertProperties(assertContext, actual.getReplaceProperties().getProperties(), expected.getReplaceProperties().getProperties());
        }
    }
    
    private static void assertSetProperties(final SQLCaseAssertContext assertContext, final DorisAlterMaterializedViewStatement actual, final DorisAlterMaterializedViewStatementTestCase expected) {
        if (null != expected.getSetProperties()) {
            assertNotNull(actual.getSetProperties(), assertContext.getText("Actual set properties should exist."));
            SQLSegmentAssert.assertIs(assertContext, actual.getSetProperties(), expected.getSetProperties());
            assertProperties(assertContext, actual.getSetProperties().getProperties(), expected.getSetProperties().getProperties());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final java.util.List<PropertySegment> actualProperties, final java.util.List<ExpectedProperty> expectedProperties) {
        assertThat(assertContext.getText("Properties size assertion error: "), actualProperties.size(), is(expectedProperties.size()));
        int count = 0;
        for (PropertySegment each : actualProperties) {
            ExpectedProperty expectedProperty = expectedProperties.get(count);
            assertThat(assertContext.getText("Property key assertion error: "), each.getKey(), is(expectedProperty.getKey()));
            assertThat(assertContext.getText("Property value assertion error: "), each.getValue(), is(expectedProperty.getValue()));
            SQLSegmentAssert.assertIs(assertContext, each, expectedProperty);
            count++;
        }
    }
}
