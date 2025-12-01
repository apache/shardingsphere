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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.IndexHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.bound.TableBoundAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.expression.ExpressionAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.MergeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedTableFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedCollectionTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedFunctionTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedHintIndexName;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedIndexHint;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedJoinTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSubqueryTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedTable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TableSegment actual, final ExpectedTable expected) {
        if (actual instanceof JoinTableSegment) {
            assertIs(assertContext, (JoinTableSegment) actual, expected.getJoinTable());
        } else if (actual instanceof SimpleTableSegment) {
            assertIs(assertContext, (SimpleTableSegment) actual, expected.getSimpleTable());
        } else if (actual instanceof SubqueryTableSegment) {
            assertIs(assertContext, (SubqueryTableSegment) actual, expected.getSubqueryTable());
        } else if (actual instanceof FunctionTableSegment) {
            assertIs(assertContext, (FunctionTableSegment) actual, expected.getFunctionTable());
        } else if (actual instanceof CollectionTableSegment) {
            assertIs(assertContext, (CollectionTableSegment) actual, expected.getCollectionTable());
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported table segment type `%s`.", actual.getClass()));
        }
    }
    
    /**
     * Assert actual collection table segment is correct with expected collection table.
     *
     * @param assertContext assert context
     * @param actual actual collection table
     * @param expected expected collection table
     */
    private static void assertIs(final SQLCaseAssertContext assertContext, final CollectionTableSegment actual, final ExpectedCollectionTable expected) {
        if (null != expected.getExpectedExpression()) {
            ExpressionAssert.assertExpression(assertContext, actual.getExpressionSegment(), expected.getExpectedExpression());
        }
    }
    
    /**
     * Assert actual function table segment is correct with expected table.
     *
     * @param assertContext assert context
     * @param actual actual function table
     * @param expected expected function table
     */
    private static void assertIs(final SQLCaseAssertContext assertContext, final FunctionTableSegment actual, final ExpectedFunctionTable expected) {
        assertTableFunction(assertContext, actual.getTableFunction(), expected.getTableFunction());
        actual.getAliasName().ifPresent(optional -> assertThat(assertContext.getText("Table function alias assertion error"), optional, is(expected.getTableAlias())));
    }
    
    /**
     * Assert actual table segment is correct with expected table.
     *
     * @param assertContext assert context
     * @param actual actual table
     * @param expected expected table
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SimpleTableSegment actual, final ExpectedSimpleTable expected) {
        assertTableNameSegment(assertContext, actual, expected);
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAliasName().orElse(null), is(expected.getAlias()));
        if (null == expected.getOwner()) {
            assertFalse(actual.getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
        if (!expected.getIndexHints().isEmpty()) {
            assertFalse(actual.getIndexHintSegments().isEmpty());
            assertIs(assertContext, actual.getIndexHintSegments(), expected.getIndexHints());
        }
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final Collection<IndexHintSegment> actual, final Collection<ExpectedIndexHint> expected) {
        assertThat(expected.size(), is(actual.size()));
        Iterator<ExpectedIndexHint> expectedIndexHintIterator = expected.iterator();
        Iterator<IndexHintSegment> actualIndexHintIterator = actual.iterator();
        while (expectedIndexHintIterator.hasNext()) {
            ExpectedIndexHint expectedIndexHint = expectedIndexHintIterator.next();
            IndexHintSegment actualIndexHint = actualIndexHintIterator.next();
            assertIs(assertContext, actualIndexHint, expectedIndexHint);
        }
    }
    
    private static void assertIs(final SQLCaseAssertContext assertContext, final IndexHintSegment actual, final ExpectedIndexHint expected) {
        assertThat(expected.getHintIndexNames().size(), is(actual.getIndexNames().size()));
        Iterator<ExpectedHintIndexName> expectedIndexNameIterator = expected.getHintIndexNames().iterator();
        Iterator<String> actualIndexNameIterator = actual.getIndexNames().iterator();
        while (expectedIndexNameIterator.hasNext()) {
            ExpectedHintIndexName expectedIndexName = expectedIndexNameIterator.next();
            String actualIndexName = actualIndexNameIterator.next();
            assertThat(assertContext.getText("Index hint name assertion error: "), actualIndexName, is(expectedIndexName.getName()));
        }
        assertThat(assertContext.getText("Index hint origin text assertion error: "), actual.getOriginText(), is(expected.getOriginText()));
    }
    
    /**
     * Assert subquery expression.
     *
     * @param assertContext assert context
     * @param actual actual subquery segment
     * @param expected expected subquery expression
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final SubqueryTableSegment actual, final ExpectedSubqueryTable expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (null != actual.getSubquery().getSelect()) {
            SelectStatementAssert.assertIs(assertContext, actual.getSubquery().getSelect(), expected.getSubquery().getSelectTestCases());
        }
        if (null != actual.getSubquery().getMerge()) {
            MergeStatementAssert.assertIs(assertContext, actual.getSubquery().getMerge(), expected.getSubquery().getMergeTestCases());
        }
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAliasName().orElse(null), is(expected.getAlias()));
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
        assertThat(assertContext.getText("Natural should be the same."), actual.isNatural(), is(expected.isNatural()));
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
            assertTrue(Strings.isNullOrEmpty(actual), assertContext.getText("Actual join-type should not exist."));
        } else {
            assertThat(assertContext.getText("Actual join-type should exist."), actual, is(expected));
        }
    }
    
    private static void assertTableFunction(final SQLCaseAssertContext assertContext, final ExpressionSegment actual, final ExpectedTableFunction expected) {
        if (actual instanceof XmlTableFunctionSegment) {
            XmlTableFunctionSegment actualXmlTableFunction = (XmlTableFunctionSegment) actual;
            assertThat(assertContext.getText("Function name assertion error"), actualXmlTableFunction.getFunctionName(), is(expected.getFunctionName()));
            assertThat(assertContext.getText("Function text assert error"), actual.getText(), is(expected.getText()));
        } else if (actual instanceof FunctionSegment) {
            FunctionSegment actualTableFunction = (FunctionSegment) actual;
            assertThat(assertContext.getText("Function name assertion error"), actualTableFunction.getFunctionName(), is(expected.getFunctionName()));
            assertThat(assertContext.getText("Function text assert error"), actual.getText(), is(expected.getText()));
        }
    }
    
    private static void assertTableNameSegment(final SQLCaseAssertContext assertContext, final SimpleTableSegment actual, final ExpectedSimpleTable expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getTableName().getIdentifier(), expected, "Table");
        TableBoundAssert.assertIs(assertContext, actual.getTableName().getTableBoundInfo().orElse(null), expected.getTableBound());
    }
}
