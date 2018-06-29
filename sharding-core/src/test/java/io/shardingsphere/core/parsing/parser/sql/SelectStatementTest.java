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

package io.shardingsphere.core.parsing.parser.sql;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.OrderDirection;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SelectStatementTest {
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenGroupByAndOrderByAllEmpty() {
        assertFalse(new SelectStatement().isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenSame() {
        SelectStatement actual = new SelectStatement();
        actual.getOrderByItems().add(new OrderItem("col", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        actual.getGroupByItems().add(new OrderItem("col", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        assertTrue(actual.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenDifferent() {
        SelectStatement actual = new SelectStatement();
        actual.getOrderByItems().add(new OrderItem("order_col", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        actual.getGroupByItems().add(new OrderItem("group_col", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent()));
        assertFalse(actual.isSameGroupByAndOrderByItems());
    }
}
