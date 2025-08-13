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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.limit.LimitClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.output.OutputClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.where.WhereClauseAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.UpdateStatementTestCase;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Update statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateStatementAssert {
    
    /**
     * Assert update statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual update statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
        assertFromClause(assertContext, actual, expected);
        assertWhereClause(assertContext, actual, expected);
        assertOrderByClause(assertContext, actual, expected);
        assertLimitClause(assertContext, actual, expected);
        assertOptionHint(assertContext, actual, expected);
        assertOutputClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null == expected.getTable()) {
            assertNull(actual.getTable(), assertContext.getText("Actual from should not exist."));
        } else {
            TableAssert.assertIs(assertContext, actual.getTable(), expected.getTable());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        SetClauseAssert.assertIs(assertContext, actual.getSetAssignment(), expected.getSetClause());
    }
    
    private static void assertFromClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null == expected.getFrom()) {
            assertFalse(actual.getFrom().isPresent(), assertContext.getText("Actual from segment should not exist."));
        } else {
            assertTrue(actual.getFrom().isPresent(), assertContext.getText("Actual from segment should exist."));
            TableAssert.assertIs(assertContext, actual.getFrom().get(), expected.getFrom());
        }
    }
    
    private static void assertWhereClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        if (null == expected.getWhereClause()) {
            assertFalse(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should not exist."));
        } else {
            assertTrue(actual.getWhere().isPresent(), assertContext.getText("Actual where segment should exist."));
            WhereClauseAssert.assertIs(assertContext, actual.getWhere().get(), expected.getWhereClause());
        }
    }
    
    private static void assertOrderByClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<OrderBySegment> orderBySegment = actual.getOrderBy();
        if (null == expected.getOrderByClause()) {
            assertFalse(orderBySegment.isPresent(), assertContext.getText("Actual order by segment should not exist."));
        } else {
            assertTrue(orderBySegment.isPresent(), assertContext.getText("Actual order by segment should exist."));
            OrderByClauseAssert.assertIs(assertContext, orderBySegment.get(), expected.getOrderByClause());
        }
    }
    
    private static void assertLimitClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<LimitSegment> limitSegment = actual.getLimit();
        if (null == expected.getLimitClause()) {
            assertFalse(limitSegment.isPresent(), assertContext.getText("Actual limit segment should not exist."));
        } else {
            assertTrue(limitSegment.isPresent(), assertContext.getText("Actual limit segment should exist."));
            LimitClauseAssert.assertRowCount(assertContext, limitSegment.get().getRowCount().orElse(null), expected.getLimitClause().getRowCount());
            SQLSegmentAssert.assertIs(assertContext, limitSegment.get(), expected.getLimitClause());
        }
    }
    
    private static void assertOptionHint(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<OptionHintSegment> optionHintSegment = actual.getOptionHint();
        if (null == expected.getOptionHint()) {
            assertFalse(optionHintSegment.isPresent(), assertContext.getText("Actual option hint segment should not exist."));
        } else {
            assertTrue(optionHintSegment.isPresent(), assertContext.getText("Actual option hint segment should exist."));
            assertThat(assertContext.getText("Option hint text assertion error: "), optionHintSegment.get().getText(), is(expected.getOptionHint().getText()));
            SQLSegmentAssert.assertIs(assertContext, optionHintSegment.get(), expected.getOptionHint());
        }
    }
    
    private static void assertOutputClause(final SQLCaseAssertContext assertContext, final UpdateStatement actual, final UpdateStatementTestCase expected) {
        Optional<OutputSegment> outputSegment = actual.getOutput();
        if (null == expected.getOutputClause()) {
            assertFalse(outputSegment.isPresent(), assertContext.getText("Actual output segment should not exist."));
        } else {
            assertTrue(outputSegment.isPresent(), assertContext.getText("Actual output segment should exist."));
            OutputClauseAssert.assertIs(assertContext, outputSegment.get(), expected.getOutputClause());
        }
    }
}
