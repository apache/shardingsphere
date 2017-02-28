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

import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;

public final class SelectMixedTablesTest extends AbstractDynamicRouteSqlTest {
    
    @Test
    public void assertBindingTableWithUnBoundTable() throws SQLParserException {
        assertSingleTarget("select * from order o join order_item i join order_attr a using(order_id) where o.order_id = 1", "ds_1",
                "select * from order_1 o join order_item_1 i join order_attr_b a using(order_id) where o.order_id = 1");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1), new ShardingValuePair("order_attr", 1)), 
                "select * from order o join order_item i join order_attr a using(order_id)", "ds_1",
                "select * from order_1 o join order_item_1 i join order_attr_b a using(order_id)");
    }
    
    @Test
    public void assertConditionFromRelationship() throws SQLParserException {
        assertSingleTarget("select * from order o join order_attr a using(order_id) where o.order_id = 1", "ds_1",
                "select * from order_1 o join order_attr_b a using(order_id) where o.order_id = 1");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1), new ShardingValuePair("order_attr", 1)), "select * from order o join order_attr a using(order_id)", "ds_1",
                "select * from order_1 o join order_attr_b a using(order_id)");
    }
    
    @Test
    public void assertSelectWithCartesianProductAllPartitions() throws SQLParserException {
        assertMultipleTargets("select * from order o, order_attr a", 4, Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("select * from order_0 o, order_attr_a a", "select * from order_1 o, order_attr_a a", 
                        "select * from order_0 o, order_attr_b a", "select * from order_1 o, order_attr_b a"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSelectTableWithoutRules() throws SQLParserException {
        assertSingleTarget("select * from aaa, bbb, ccc", null, null);
    }
}
