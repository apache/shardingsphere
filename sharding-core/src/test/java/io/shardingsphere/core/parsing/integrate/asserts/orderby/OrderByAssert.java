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

package io.shardingsphere.core.parsing.integrate.asserts.orderby;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.orderby.ExpectedOrderByColumn;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Order by assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class OrderByAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert order by items.
     *
     * @param actual actual order by items
     * @param expected expected order by items
     */
    public void assertOrderByItems(final List<OrderItem> actual, final List<ExpectedOrderByColumn> expected) {
        assertThat(assertMessage.getFullAssertMessage("Order by items size error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (OrderItem each : actual) {
            assertOrderByItem(each, expected.get(count));
            count++;
        }
    }
    
    private void assertOrderByItem(final OrderItem actual, final ExpectedOrderByColumn expected) {
        assertThat(assertMessage.getFullAssertMessage("Order by item owner assertion error: "), actual.getOwner().orNull(), is(expected.getOwner()));
        assertThat(assertMessage.getFullAssertMessage("Order by item name assertion error: "), actual.getName().orNull(), is(expected.getName()));
        assertThat(assertMessage.getFullAssertMessage("Order by item order direction assertion error: "), actual.getOrderDirection().name(), is(expected.getOrderDirection()));
        // TODO assert nullOrderDirection
        assertThat(assertMessage.getFullAssertMessage("Order by item index assertion error: "), actual.getIndex(), is(expected.getIndex()));
        assertThat(assertMessage.getFullAssertMessage("Order by item alias assertion error: "), actual.getAlias().orNull(), is(expected.getAlias()));
    }
}
