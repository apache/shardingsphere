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

package org.apache.shardingsphere.sql.parser.integrate.asserts.orderby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.asserts.owner.OwnerAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedOrderBy;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedColumnOrderByItem;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedExpressionOrderByItem;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedIndexOrderByItem;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedOrderByItem;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Order by item assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderByItemAssert {
    
    /**
     * Assert actual order by segment is correct with expected order by.
     *
     * @param assertMessage assert message
     * @param actual actual order by segments
     * @param expected expected order by
     * @param sqlCaseType SQL case type
     * @param type type of assertion, should be Order by or Group by
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage,
                                final Collection<OrderByItemSegment> actual, final ExpectedOrderBy expected, final SQLCaseType sqlCaseType, final String type) {
        assertThat(assertMessage.getText(String.format("%s items size assertion error: ", type)), actual.size(), is(expected.getItemSize()));
        int count = 0;
        for (OrderByItemSegment each : actual) {
            if (each instanceof ColumnOrderByItemSegment) {
                assertOrderInfo(assertMessage, each, expected.getColumnItems().get(count), type);
                assertColumnOrderByItem(assertMessage, (ColumnOrderByItemSegment) each, expected.getColumnItems().get(count), sqlCaseType, type);
                count++;
            }
        }
        for (OrderByItemSegment each : actual) {
            if (each instanceof IndexOrderByItemSegment) {
                assertOrderInfo(assertMessage, each, expected.getIndexItems().get(count), type);
                assertIndexOrderByItem(assertMessage, (IndexOrderByItemSegment) each, expected.getIndexItems().get(count), sqlCaseType, type);
                count++;
            }
        }
        for (OrderByItemSegment each : actual) {
            if (each instanceof ExpressionOrderByItemSegment) {
                assertOrderInfo(assertMessage, each, expected.getExpressionItems().get(count), type);
                assertExpressionOrderByItem(assertMessage, (ExpressionOrderByItemSegment) each, expected.getExpressionItems().get(count), sqlCaseType, type);
                count++;
            }
        }
        // TODO assert start index and stop index
        //        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertOrderInfo(final SQLStatementAssertMessage assertMessage, final OrderByItemSegment actual, final ExpectedOrderByItem expected, final String type) {
        assertThat(assertMessage.getText(String.format("%s item order direction assertion error: ", type)), actual.getOrderDirection().name(), is(expected.getOrderDirection()));
    }
    
    private static void assertColumnOrderByItem(final SQLStatementAssertMessage assertMessage,
                                                final ColumnOrderByItemSegment actual, final ExpectedColumnOrderByItem expected, final SQLCaseType sqlCaseType, final String type) {
        assertThat(assertMessage.getText(String.format("%s item column name assertion error: ", type)), actual.getColumn().getName(), is(expected.getName()));
        if (null != expected.getOwner()) {
            assertTrue(assertMessage.getText("Actual owner should exist."), actual.getColumn().getOwner().isPresent());
            OwnerAssert.assertTable(assertMessage, actual.getColumn().getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertFalse(assertMessage.getText("Actual owner should not exist."), actual.getColumn().getOwner().isPresent());
        }
        // TODO assert start index and stop index
        //        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertIndexOrderByItem(final SQLStatementAssertMessage assertMessage,
                                               final IndexOrderByItemSegment actual, final ExpectedIndexOrderByItem expected, final SQLCaseType sqlCaseType, final String type) {
        assertThat(assertMessage.getText(String.format("%s item index assertion error: ", type)), actual.getColumnIndex(), is(expected.getIndex()));
        // TODO assert start index and stop index
        //        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    private static void assertExpressionOrderByItem(final SQLStatementAssertMessage assertMessage,
                                                    final ExpressionOrderByItemSegment actual, final ExpectedExpressionOrderByItem expected, final SQLCaseType sqlCaseType, final String type) {
        assertThat(assertMessage.getText(String.format("%s item expression assertion error: ", type)), actual.getExpression(), is(expected.getExpression()));
        // TODO assert start index and stop index
        //        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
