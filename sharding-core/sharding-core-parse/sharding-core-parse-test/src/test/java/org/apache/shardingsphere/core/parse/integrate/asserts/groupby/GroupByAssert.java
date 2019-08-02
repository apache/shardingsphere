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

package org.apache.shardingsphere.core.parse.integrate.asserts.groupby;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.groupby.ExpectedGroupByColumn;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.item.OrderByItemSegment;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Group by assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class GroupByAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert group by items.
     * 
     * @param actual actual group by items
     * @param expected expected group by items
     */
    public void assertGroupByItems(final Collection<OrderByItemSegment> actual, final List<ExpectedGroupByColumn> expected) {
        assertThat(assertMessage.getFullAssertMessage("Group by items size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (OrderByItemSegment each : actual) {
            if (each instanceof ColumnOrderByItemSegment) {
                assertGroupByItem((ColumnOrderByItemSegment) each, expected.get(count));
            }
            count++;
        }
    }
    
    private void assertGroupByItem(final ColumnOrderByItemSegment actual, final ExpectedGroupByColumn expected) {
        assertThat(assertMessage.getFullAssertMessage("Group by item owner assertion error: "), 
                actual.getColumn().getOwner().isPresent() ? actual.getColumn().getOwner().get().getTableName() : null, is(expected.getOwner()));
        assertThat(assertMessage.getFullAssertMessage("Group by item name assertion error: "), actual.getColumn().getName(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("Group by item order direction assertion error: "), actual.getOrderDirection().name(), is(expected.getOrderDirection()));
        // TODO assert nullOrderDirection
    }
}
