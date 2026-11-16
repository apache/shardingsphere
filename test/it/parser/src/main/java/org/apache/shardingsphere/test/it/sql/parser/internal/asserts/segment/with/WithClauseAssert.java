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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.DMLStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with.ExpectedCommonTableExpressionClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with.ExpectedWithClause;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * With clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithClauseAssert {
    
    /**
     * Assert actual with segment is correct with expected with clause.
     *
     * @param assertContext assert context
     * @param actual actual with segment
     * @param expected expected with clause
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final WithSegment actual, final ExpectedWithClause expected) {
        assertNotNull(expected, assertContext.getText("With clause should exist."));
        assertThat(assertContext.getText("With clause common table expressions size assertion error: "),
                actual.getCommonTableExpressions().size(), is(expected.getCommonTableExpressions().size()));
        int count = 0;
        for (CommonTableExpressionSegment each : actual.getCommonTableExpressions()) {
            assertCommonTableExpressionSegment(assertContext, each, expected.getCommonTableExpressions().get(count));
            count++;
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertCommonTableExpressionSegment(final SQLCaseAssertContext assertContext, final CommonTableExpressionSegment actual, final ExpectedCommonTableExpressionClause expected) {
        if (!expected.getColumns().isEmpty()) {
            assertThat(assertContext.getText("Common table expression column size assertion error: "), actual.getColumns().size(), is(expected.getColumns().size()));
        }
        assertThat(assertContext.getText("Common table expression name assertion error: "), actual.getAliasName().orElse(null), is(expected.getName()));
        int count = 0;
        for (ColumnSegment each : actual.getColumns()) {
            ColumnAssert.assertIs(assertContext, each, expected.getColumns().get(count));
            count++;
        }
        assertColumns(assertContext, actual.getSearchColumns(), expected.getSearchColumns(), expected.getSearchColumnCount());
        assertColumns(assertContext, actual.getCycleColumns(), expected.getCycleColumns(), expected.getCycleColumnCount());
        if (null != expected.getSubquery()) {
            ExpressionAssert.assertSubquery(assertContext, actual.getSubquery(), expected.getSubquery());
        }
        if (null != expected.getInsert()) {
            assertThat(actual.getDataModification(), isA(InsertStatement.class));
            DMLStatementAssert.assertIs(assertContext, actual.getDataModification(), expected.getInsert());
        } else if (null != expected.getUpdate()) {
            assertThat(actual.getDataModification(), isA(UpdateStatement.class));
            DMLStatementAssert.assertIs(assertContext, actual.getDataModification(), expected.getUpdate());
        } else if (null != expected.getDelete()) {
            assertThat(actual.getDataModification(), isA(DeleteStatement.class));
            DMLStatementAssert.assertIs(assertContext, actual.getDataModification(), expected.getDelete());
        } else if (null != expected.getMerge()) {
            assertThat(actual.getDataModification(), isA(MergeStatement.class));
            DMLStatementAssert.assertIs(assertContext, actual.getDataModification(), expected.getMerge());
        } else {
            assertNull(actual.getDataModification());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert common table expression key columns.
     *
     * @param assertContext assert context
     * @param actual actual key columns
     * @param expected expected key columns
     * @param expectedCount expected count, or {@code null} when the fixture does not specify a count
     */
    private static void assertColumns(final SQLCaseAssertContext assertContext, final Collection<ColumnSegment> actual, final List<ExpectedColumn> expected, final Integer expectedCount) {
        if (null != expectedCount) {
            assertThat(assertContext.getText("Common table expression expected key count assertion error: "), expected.size(), is(expectedCount));
        }
        if (null == expectedCount && expected.isEmpty()) {
            return;
        }
        assertThat(assertContext.getText("Common table expression search/cycle key count assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (ColumnSegment each : actual) {
            ColumnAssert.assertIs(assertContext, each, expected.get(count++));
        }
    }
}
