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

package io.shardingsphere.core.parsing.parser.context;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.OrderDirection;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class OrderItemTest {
    
    @Test
    public void assertGetColumnLabelWithoutAlias() {
        OrderItem actualOrderItem = new OrderItem("column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent());
        assertThat(actualOrderItem.getColumnLabel(), is("column_name"));
    }
    
    @Test
    public void assertGetColumnLabelWithAlias() {
        OrderItem actualOrderItem = new OrderItem("column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"));
        assertThat(actualOrderItem.getColumnLabel(), is("column_alias"));
    }
    
    @Test
    public void assertGetColumnLabelWithIndex() {
        OrderItem actualOrderItem = new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC);
        assertNull(actualOrderItem.getColumnLabel());
    }
    
    @Test
    public void assertGetQualifiedNameWithoutName() {
        OrderItem actualOrderItem = new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC);
        assertNull(actualOrderItem.getQualifiedName().orNull());
    }
    
    @Test
    public void assertGetColumnLabelWithOutOwner() {
        OrderItem actualOrderItem = new OrderItem("column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"));
        assertThat(actualOrderItem.getQualifiedName().get(), is("column_name"));
    }
    
    @Test
    public void assertGetColumnLabelWithOwner() {
        OrderItem actualOrderItem = new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"));
        assertThat(actualOrderItem.getQualifiedName().get(), is("tbl.column_name"));
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
    public void assertEqualsWithSameColumnLabel() {
        assertTrue(new OrderItem("column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"))
                .equals(new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"))));
    }
    
    @Test
    public void assertEqualsWithSameQualifiedName() {
        assertTrue(new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"))
                .equals(new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent())));
    }
    
    @Test
    public void assertEqualsWithSameIndex() {
        assertTrue(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC).equals(new OrderItem(1, OrderDirection.ASC, OrderDirection.ASC)));
    }
    
    @Test
    public void assertNotEquals() {
        assertFalse(new OrderItem("tbl", "column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.of("column_alias"))
                .equals(new OrderItem("column_name", OrderDirection.ASC, OrderDirection.ASC, Optional.<String>absent())));
    }
}
