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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.with.WithClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.DeleteStatementTestCase;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Delete statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeleteStatementAssert {
    
    /**
     * Assert delete statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual delete statement
     * @param expected expected delete statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        assertWithClause(assertContext, actual, expected);
        assertTable(assertContext, actual, expected);
        assertOutput(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
    }
    
    private static void assertWithClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        Optional<WithSegment> withSegment = actual.getWith();
        if (null == expected.getWithClause()) {
            assertFalse(withSegment.isPresent(), assertContext.getText("Actual with segment should not exist."));
        } else {
            assertTrue(withSegment.isPresent(), assertContext.getText("Actual with segment should exist."));
            WithClauseAssert.assertIs(assertContext, withSegment.get(), expected.getWithClause());
        }
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        if (!expected.getTables().isEmpty()) {
            assertNotNull(actual.getTable(), assertContext.getText("Actual table segment should exist."));
            List<SimpleTableSegment> actualTableSegments = new LinkedList<>();
            if (actual.getTable() instanceof SimpleTableSegment) {
                actualTableSegments.add((SimpleTableSegment) actual.getTable());
            } else if (actual.getTable() instanceof DeleteMultiTableSegment) {
                DeleteMultiTableSegment deleteMultiTableSegment = (DeleteMultiTableSegment) actual.getTable();
                actualTableSegments.addAll(deleteMultiTableSegment.getActualDeleteTables());
            }
            TableAssert.assertIs(assertContext, actualTableSegments, expected.getTables());
        } else if (null != expected.getFunctionTable()) {
            assertNotNull(actual.getTable(), assertContext.getText("Actual function table segment should exist."));
            ExpectedTable expectedTable = new ExpectedTable();
            expectedTable.setFunctionTable(expected.getFunctionTable());
            TableAssert.assertIs(assertContext, actual.getTable(), expectedTable);
        } else if (null != expected.getSubqueryTable()) {
            assertNotNull(actual.getTable(), assertContext.getText("Actual subquery table segment should exist."));
            TableAssert.assertIs(assertContext, (SubqueryTableSegment) actual.getTable(), expected.getSubqueryTable());
        } else {
            assertNull(actual.getTable(), assertContext.getText("Actual table should not exist."));
        }
    }
    
    private static void assertOutput(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = actual.getOutput();
        if (null == expected.getOutputClause()) {
            assertFalse(outputSegment.isPresent(), assertContext.getText("Actual output segment should not exist."));
        } else {
            assertTrue(outputSegment.isPresent(), assertContext.getText("Actual output segment should exist."));
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        if (null == expected.getWhereClause()) {
            assertFalse(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should not exist."));
        } else {
            assertTrue(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        Optional<OrderBySegment> orderBySegment = actual.getOrderBy();
        if (null == expected.getOrderByClause()) {
            assertFalse(orderBySegment.isPresent(), assertContext.getText("Actual order by segment should not exist."));
        } else {
            assertTrue(orderBySegment.isPresent(), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, orderBySegment.get(), expected.getOrderByClause());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final DeleteStatement actual, final DeleteStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = actual.getLimit();
        if (null == expected.getLimitClause()) {
            assertFalse(limitSegment.isPresent(), assertContext.getText("Actual limit segment should not exist."));
        } else {
            assertTrue(limitSegment.isPresent(), assertContext.getText("Actual limit segment should exist."));
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        }
    }
}
