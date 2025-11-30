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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.column.ColumnAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.orderby.OrderByItemAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.SelectStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.model.ExpectedModelClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.orderby.ExpectedOrderByClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.SelectStatementTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Model clause assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ModelClauseAssert {
    
    /**
     * Assert actual model segment is correct with expected model clause.
     * @param assertContext assert context
     * @param actual actual model
     * @param expected expected model
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final ModelSegment actual, final ExpectedModelClause expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (null != expected.getReferenceModelSelect()) {
            assertNotNull(actual.getReferenceModelSelects(), assertContext.getText("Actual reference model select subquery should exist."));
            assertThat(assertContext.getText("Actual reference model select subquery size assertion error: "), actual.getReferenceModelSelects().size(), is(expected.getReferenceModelSelect().size()));
            assertSubquerySegments(assertContext, actual.getReferenceModelSelects(), expected.getReferenceModelSelect());
        }
        if (null != expected.getOrderBySegments()) {
            assertNotNull(actual.getOrderBySegments(), assertContext.getText("Actual order by segments should exist."));
            assertThat(assertContext.getText("Actual order by segments size assertion error: "), actual.getOrderBySegments().size(), is(expected.getOrderBySegments().size()));
            assertOrderBySegments(assertContext, actual.getOrderBySegments(), expected.getOrderBySegments());
        }
        if (null != expected.getCellAssignmentColumns()) {
            assertNotNull(actual.getCellAssignmentColumns(), assertContext.getText("Actual cell assignment columns should exist."));
            assertThat(assertContext.getText("Actual cell assignment columns assertion error: "), actual.getCellAssignmentColumns().size(), is(expected.getCellAssignmentColumns().size()));
            assertCellAssignmentColumns(assertContext, actual.getCellAssignmentColumns(), expected.getCellAssignmentColumns());
        }
        if (null != expected.getCellAssignmentSelect()) {
            assertNotNull(actual.getCellAssignmentSelects(), assertContext.getText("Actual cell assignment select subquery should exist."));
            assertThat(assertContext.getText("Actual cell assignment select size assertion error: "), actual.getCellAssignmentSelects().size(), is(expected.getCellAssignmentSelect().size()));
            assertSubquerySegments(assertContext, actual.getCellAssignmentSelects(), expected.getCellAssignmentSelect());
        }
    }
    
    private static void assertSubquerySegments(final SQLCaseAssertContext assertContext, final List<SubquerySegment> actual, final List<SelectStatementTestCase> expected) {
        int count = 0;
        for (SubquerySegment each : actual) {
            SelectStatementAssert.assertIs(assertContext, each.getSelect(), expected.get(count));
            count++;
        }
    }
    
    private static void assertOrderBySegments(final SQLCaseAssertContext assertContext, final List<OrderBySegment> actual, final List<ExpectedOrderByClause> expected) {
        int count = 0;
        for (OrderBySegment each : actual) {
            OrderByItemAssert.assertIs(assertContext, each.getOrderByItems(), expected.get(count), "Order by");
            count++;
        }
    }
    
    private static void assertCellAssignmentColumns(final SQLCaseAssertContext assertContext, final List<ColumnSegment> actual, final List<ExpectedColumn> expected) {
        int count = 0;
        for (ColumnSegment each : actual) {
            ColumnAssert.assertIs(assertContext, each, expected.get(count));
            count++;
        }
    }
}
