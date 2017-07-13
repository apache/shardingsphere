/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SelectStatementTest {
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenGroupByAndOrderByAllEmpty() throws SQLException {
        assertFalse(new SelectStatement().isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenSame() throws SQLException {
        SelectStatement actual = new SelectStatement();
        actual.getOrderByItems().add(new OrderItem("col", OrderType.ASC, Optional.<String>absent()));
        actual.getGroupByItems().add(new OrderItem("col", OrderType.ASC, Optional.<String>absent()));
        assertTrue(actual.isSameGroupByAndOrderByItems());
    }
    
    @Test
    public void assertIsSameGroupByAndOrderByItemsWhenDifferent() throws SQLException {
        SelectStatement actual = new SelectStatement();
        actual.getOrderByItems().add(new OrderItem("order_col", OrderType.ASC, Optional.<String>absent()));
        actual.getGroupByItems().add(new OrderItem("group_col", OrderType.ASC, Optional.<String>absent()));
        assertFalse(actual.isSameGroupByAndOrderByItems());
    }
}
