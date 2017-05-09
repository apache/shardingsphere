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

package com.dangdang.ddframe.rdb.sharding.router;

import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

public final class DMLTest extends AbstractDynamicRouteSqlTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void assertInsert() {
        assertSingleTargetWithoutParameter("insert into `order` (order_id, name) value (1,'test')", "ds_1", "insert into order_1 (order_id, name) value (1,'test')");
        assertSingleTargetWithoutParameter(Lists.newArrayList(new ShardingValuePair("order", 1)), "insert into `order` value (1,'test')", "ds_1", "insert into order_1 value (1,'test')");
        assertSingleTargetWithParameters("insert into `order` (order_id, name) value (?,?)", Arrays.<Object>asList(2, "test"), "ds_0", "insert into order_0 (order_id, name) value (?,?)");
        assertSingleTargetWithParameters(
                Lists.newArrayList(new ShardingValuePair("order", 2)), "insert into `order` value (?,?)", Arrays.<Object>asList(2, "test"), "ds_0", "insert into order_0 value (?,?)");
    }
    
    @Test
    public void assertInsertError() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("INSERT statement should contain sharding value.");
        assertSingleTargetWithoutParameter("insert into `order` value (1,'test')", null, null);
    }
    
    @Test
    public void assertUpdate() {
        assertSingleTargetWithoutParameter("update `order` set name = 'test' where order_id = 1", "ds_1", "update order_1 set name = 'test' where order_id = 1");
        assertSingleTargetWithoutParameter(Lists.newArrayList(new ShardingValuePair("order", 1)), "update `order` set name = 'test'", "ds_1", "update order_1 set name = 'test'");
        assertSingleTargetWithParameters("update `order` set name = ? where order_id = ?", Arrays.<Object>asList("test", 2), "ds_0", "update order_0 set name = ? where order_id = ?");
        assertSingleTargetWithParameters(
                Lists.newArrayList(new ShardingValuePair("order", 2)), "update `order` set name = ?", Collections.<Object>singletonList("test"), "ds_0", "update order_0 set name = ?");
    }
    
    @Test
    public void assertDelete() {
        assertSingleTargetWithoutParameter("delete from `order` where order_id = 1", "ds_1", "delete from order_1 where order_id = 1");
        assertSingleTargetWithoutParameter(Lists.newArrayList(new ShardingValuePair("order", 1)), "delete from `order`", "ds_1", "delete from order_1");
        assertSingleTargetWithoutParameter(Lists.newArrayList(new ShardingValuePair("order", 2)), "delete from `order`", "ds_0", "delete from order_0");
        assertSingleTargetWithParameters("delete from `order` where order_id = ?", Collections.<Object>singletonList(2), "ds_0", "delete from order_0 where order_id = ?");
    }
}
