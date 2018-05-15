/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.integrate.asserts.groupby;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.groupby.ExpectedGroupByColumn;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import lombok.RequiredArgsConstructor;

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
    public void assertGroupByItems(final List<OrderItem> actual, final List<ExpectedGroupByColumn> expected) {
        assertThat(assertMessage.getFullAssertMessage("Group by items size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (OrderItem each : actual) {
            assertGroupByItem(each, expected.get(count));
            count++;
        }
    }
    
    private void assertGroupByItem(final OrderItem actual, final ExpectedGroupByColumn expected) {
        assertThat(assertMessage.getFullAssertMessage("Group by item owner assertion error: "), actual.getOwner().orNull(), is(expected.getOwner()));
        assertThat(assertMessage.getFullAssertMessage("Group by item name assertion error: "), actual.getName().orNull(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("Group by item order direction assertion error: "), actual.getOrderDirection().name(), is(expected.getOrderDirection()));
        // TODO assert nullOrderDirection
        assertThat(assertMessage.getFullAssertMessage("Group by item alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
    }
}
