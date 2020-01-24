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

package org.apache.shardingsphere.sql.parser.integrate.asserts.groupby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.groupby.ExpectedGroupBy;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.groupby.ColumnExpectedGroupByItem;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Group by assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupByAssert {
    
    /**
     * Assert actual group by segment is correct with expected group by.
     * 
     * @param assertMessage assert message
     * @param actual actual group by segment
     * @param expected expected group by
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final GroupBySegment actual, final ExpectedGroupBy expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Group by items size error: "), actual.getGroupByItems().size(), is(expected.getItems().size()));
        int count = 0;
        for (OrderByItemSegment each : actual.getGroupByItems()) {
            if (each instanceof ColumnOrderByItemSegment) {
                assertColumnGroupByItem(assertMessage, (ColumnOrderByItemSegment) each, expected.getItems().get(count), sqlCaseType);
            }
            // TODO assert other group by item
            count++;
        }
    }
    
    private static void assertColumnGroupByItem(final SQLStatementAssertMessage assertMessage, 
                                                final ColumnOrderByItemSegment actual, final ColumnExpectedGroupByItem expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Group by item column name assertion error: "), actual.getColumn().getName(), is(expected.getName()));
        assertThat(assertMessage.getText("Group by item order direction assertion error: "), actual.getOrderDirection().name(), is(expected.getOrderDirection()));
        // TODO assert nullOrderDirection
        if (null != expected.getOwner()) {
            assertTrue(assertMessage.getText("Actual owner should exist."), actual.getColumn().getOwner().isPresent());
            assertOwner(assertMessage, actual.getColumn().getOwner().get(), expected.getOwner(), sqlCaseType);
        } else {
            assertFalse(assertMessage.getText("Actual owner should not exist."), actual.getColumn().getOwner().isPresent());
        }
    }
    
    private static void assertOwner(final SQLStatementAssertMessage assertMessage, final TableSegment actual, final ExpectedTableOwner expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Group by column owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertMessage.getText("Group by column owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Group by column owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
