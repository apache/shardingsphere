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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.table;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.value.IdentifierValueAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedJoinTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSubqueryTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedTable;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Table assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableAssert {
    
    /**
     * Assert actual table segments is correct with expected tables.
     *
     * @param assertContext assert context
     * @param actual actual tables
     * @param expected expected tables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableSegment actual, final ExpectedTable expected) {
        if (actual instanceof JoinTableSegment) {
            assertIs(assertContext, (JoinTableSegment) actual, expected.getJoinTable());
        } else if (actual instanceof SimpleTableSegment) {
            assertIs(assertContext, (SimpleTableSegment) actual, expected.getSimpleTable());
        } else if (actual instanceof SubqueryTableSegment) {
            assertIs(assertContext, (SubqueryTableSegment) actual, expected.getSubqueryTable());
        } else {
            throw new UnsupportedOperationException(
                    String.format("Unsupported table segment type `%s`", actual.getClass()));
        }
    }
    
    /**
     * Assert actual table segment is correct with expected table.
     *
     * @param assertContext assert context
     * @param actual actual table
     * @param expected expected table
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SimpleTableSegment actual, final ExpectedSimpleTable expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getTableName().getIdentifier(), expected, "Table");
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
        if (null == expected.getOwner()) {
            assertFalse(assertContext.getText("Actual owner should not exist"), actual.getOwner().isPresent());
        } else {
            assertTrue(assertContext.getText("Actual owner should exist"), actual.getOwner().isPresent());
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert subquery expression.
     *
     * @param assertContext assert context
     * @param actual actual subquery segment
     * @param expected expected subquery expression
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SubqueryTableSegment actual, final ExpectedSubqueryTable expected) {
        SelectStatementAssert.assertIs(assertContext, actual.getSubquery().getSelect(), expected.getSubquery().getSelectTestCases());
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
    }
    
    /**
     * Assert join table.
     *
     * @param assertContext assert context
     * @param actual actual JoinTableSegment
     * @param expected expected ExpectedJoinTable
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final JoinTableSegment actual, final ExpectedJoinTable expected) {
        assertIs(assertContext, actual.getLeft(), expected.getLeft());
        assertIs(assertContext, actual.getRight(), expected.getRight());
        ExpressionAssert.assertExpression(assertContext, actual.getCondition(), expected.getOnCondition());
        assertJoinType(assertContext, actual.getJoinType(), expected.getJoinType());
        assertThat(assertContext.getText("Column size assertion error: "), actual.getUsing().size(), is(expected.getUsingColumns().size()));
        int count = 0;
        for (ExpectedColumn each : expected.getUsingColumns()) {
            ColumnAssert.assertIs(assertContext, actual.getUsing().get(count), each);
            count++;
        }
    }
    
    /**
     * Assert actual table segments is correct with expected tables.
     *
     * @param assertContext assert context
     * @param actual actual tables
     * @param expected expected tables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final Collection<SimpleTableSegment> actual, final List<ExpectedSimpleTable> expected) {
        assertThat(assertContext.getText("Tables size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (SimpleTableSegment each : actual) {
            assertIs(assertContext, each, expected.get(count));
            count++;
        }
    }
    
    /**
     * Assert actual simple table segments with expected simple tables.
     *
     * @param assertContext assert context
     * @param actualTables actual simple tables
     * @param expectedTables expected simple tables
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final List<SimpleTableSegment> actualTables, final List<ExpectedSimpleTable> expectedTables) {
        assertThat(assertContext.getText("tables size should be the same."),
                actualTables.size(), is(expectedTables.size()));
        for (int i = 0; i < actualTables.size(); i++) {
            assertIs(assertContext, actualTables.get(i), expectedTables.get(i));
        }
    }
    
    private static void assertJoinType(final SQLCaseAssertContext assertContext, final String actual, final String expected) {
        if (Strings.isNullOrEmpty(expected)) {
            assertTrue(assertContext.getText("Actual join-type should not exist."), Strings.isNullOrEmpty(actual));
        } else {
            assertThat(assertContext.getText("Actual join-type should exist."), actual, is(expected));
        }
    }
}
