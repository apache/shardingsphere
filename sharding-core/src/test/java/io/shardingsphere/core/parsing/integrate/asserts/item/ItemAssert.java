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

package io.shardingsphere.core.parsing.integrate.asserts.item;

import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.item.ExpectedAggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;

import java.util.List;
import java.util.Set;

/**
 * Item assert.
 *
 * @author zhangliang
 */
public final class ItemAssert {
    
    private final AggregationSelectItemAssert aggregationSelectItemAssert;
    
    public ItemAssert(final SQLStatementAssertMessage assertMessage) {
        aggregationSelectItemAssert = new AggregationSelectItemAssert(assertMessage);
    }
    
    /**
     * Assert items.
     * 
     * @param actual actual items
     * @param expected expected items
     */
    public void assertItems(final Set<SelectItem> actual, final List<ExpectedAggregationSelectItem> expected) {
        // TODO assert SelectItems total size
        // TODO assert StarSelectItem
        // TODO assert CommonSelectItem
        aggregationSelectItemAssert.assertAggregationSelectItems(actual, expected);
    }
}
