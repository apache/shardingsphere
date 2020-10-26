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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.schema.SchemaAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dml.impl.SelectStatementAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedJoinTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSubqueryTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTableOwner;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
        } else if (actual instanceof DeleteMultiTableSegment) {
            return;
        } else {
            return;
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
        assertThat(assertContext.getText("Table name assertion error: "), actual.getTableName().getIdentifier().getValue(), is(expected.getName()));
        assertThat(assertContext.getText("Table alias assertion error: "), actual.getAlias().orElse(null), is(expected.getAlias()));
        if (null != expected.getOwner()) {
            assertTrue(assertContext.getText("Actual owner should exist."), actual.getOwner().isPresent());
            // TODO OwnerAssert is necessary.
            OwnerSegment owner = actual.getOwner().get();
            SchemaAssert.assertIs(assertContext, new SchemaSegment(owner.getStartIndex(), owner.getStopIndex(), owner.getIdentifier()), expected.getOwner());
        } else {
            assertFalse(assertContext.getText("Actual owner should not exist."), actual.getOwner().isPresent());
        }
        assertThat(assertContext.getText("Table start delimiter assertion error: "), actual.getTableName().getIdentifier().getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Table end delimiter assertion error: "), actual.getTableName().getIdentifier().getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
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
        TableAssert.assertIs(assertContext, actual.getLeft(), expected.getLeft());
        TableAssert.assertIs(assertContext, actual.getRight(), expected.getRight());
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
     * Assert actual table segment is correct with expected table owner.
     *
     * @param assertContext assert context
     * @param actual actual table segment
     * @param expected expected table owner
     */
    public static void assertOwner(final SQLCaseAssertContext assertContext, final SimpleTableSegment actual, final ExpectedSimpleTableOwner expected) {
        assertThat(assertContext.getText("Owner name assertion error: "), actual.getTableName().getIdentifier().getValue(), is(expected.getName()));
        assertThat(assertContext.getText("Owner name start delimiter assertion error: "),
                actual.getTableName().getIdentifier().getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Owner name end delimiter assertion error: "), actual.getTableName().getIdentifier().getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }

}
