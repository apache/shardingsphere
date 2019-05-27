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

package org.apache.shardingsphere.core.parse.sql.context;

import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parse.sql.context.orderby.OrderItem;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderItemTest {
    
    @Test
    public void assertGetQualifiedNameWithoutName() {
        OrderItem actualOrderItem = new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC);
        assertNull(actualOrderItem.getQualifiedName().orNull());
    }
    
    @SuppressWarnings("ObjectEqualsNull")
    @Test
    public void assertEqualsWithNull() {
        assertFalse(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC).equals(null));
    }
    
    @Test
    public void assertEqualsWithOtherObject() {
        assertFalse(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC).equals(new Object()));
    }
    
    @Test
    public void assertEqualsWithDifferentOrderType() {
        assertFalse(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC).equals(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC)));
    }
    
    @Test
    public void assertEqualsWithSameQualifiedName() {
        OrderItem orderItem1 = new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC);
        OrderItem orderItem2 = new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC);
        assertThat(orderItem1, is(orderItem2));
    }
    
    @Test
    public void assertEqualsWithSameIndex() {
        assertTrue(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC).equals(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC)));
    }
}
