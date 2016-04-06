/**
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

import java.util.Arrays;
import java.util.Collections;

import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.google.common.collect.Lists;
import org.junit.Test;

public final class SelectBindingTableTest extends AbstractDynamicRouteSqlTest {
    
    @Test
    public void assertSelectWithBindingJoin() throws SQLParserException {
        assertSingleTarget("select * from order o inner join order_item i on o.order_id = i.order_id where o.order_id = 1", "ds_1",
                "SELECT * FROM order_1 o INNER JOIN order_item_1 i ON o.order_id = i.order_id WHERE o.order_id = 1");
        assertSingleTarget("select * from order o join order_item i on o.order_id = i.order_id where o.order_id = 1", "ds_1",
                "SELECT * FROM order_1 o JOIN order_item_1 i ON o.order_id = i.order_id WHERE o.order_id = 1");
        assertSingleTarget("select * from order o join order_item i using (order_id) where o.order_id = 1", "ds_1",
                "SELECT * FROM order_1 o JOIN order_item_1 i USING (order_id) WHERE o.order_id = 1");
        assertSingleTarget("select * from order o, order_item i WHERE o.order_id = i.order_id and o.order_id = 1", "ds_1",
                "SELECT * FROM order_1 o, order_item_1 i WHERE o.order_id = i.order_id AND o.order_id = 1");
        assertSingleTarget("select * from order o, order_item i WHERE o.order_id = i.order_id and o.order_id = ?", Collections.<Object>singletonList(1), "ds_1",
                "SELECT * FROM order_1 o, order_item_1 i WHERE o.order_id = i.order_id AND o.order_id = ?");
    }
    
    @Test
    public void assertSelectWithBindingJoinDynamic() throws SQLParserException {
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1)), "select * from order o inner join order_item i on o.order_id = i.order_id", "ds_1",
                "SELECT * FROM order_1 o INNER JOIN order_item_1 i ON o.order_id = i.order_id");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1)), "select * from order o join order_item i on o.order_id = i.order_id", "ds_1",
                "SELECT * FROM order_1 o JOIN order_item_1 i ON o.order_id = i.order_id");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1)), "select * from order o join order_item i using (order_id)", "ds_1",
                "SELECT * FROM order_1 o JOIN order_item_1 i USING (order_id)");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1)), "select * from order o, order_item i WHERE o.order_id = i.order_id", "ds_1",
                "SELECT * FROM order_1 o, order_item_1 i WHERE o.order_id = i.order_id");
    }
    
    @Test
    public void assertSelectWithRouteAllPartitions() throws SQLParserException {
        assertMultipleTargets("select * from order o inner join order_item i on o.order_id = i.order_id", 4, Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("SELECT * FROM order_0 o INNER JOIN order_item_0 i ON o.order_id = i.order_id", "SELECT * FROM order_1 o INNER JOIN order_item_1 i ON o.order_id = i.order_id"));
    }
}
