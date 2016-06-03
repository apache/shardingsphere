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

import java.util.Arrays;
import java.util.Collections;

import com.dangdang.ddframe.rdb.sharding.exception.SQLParserException;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.google.common.collect.Lists;
import org.junit.Test;

public final class SelectSingleTableTest extends AbstractDynamicRouteSqlTest {
    
    @Test
    public void assertSingleSelect() throws SQLParserException {
        assertSingleTarget("select * from order where order_id = 1", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1");
        assertSingleTarget("select * from order where order_id = ?", Collections.<Object>singletonList(2), "ds_0", "SELECT * FROM order_0 WHERE order_id = ?");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 1)), "select * from order", "ds_1", "SELECT * FROM order_1");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 2)), "select * from order", "ds_0", "SELECT * FROM order_0");
    }
    
    @Test
    public void assertSelectWithAlias() throws SQLParserException {
        assertSingleTarget("select * from order a where a.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE a.order_id = 2");
        assertSingleTarget("select * from order A where a.order_id = 2", "ds_0", "SELECT * FROM order_0 A WHERE a.order_id = 2");
        assertSingleTarget("select * from order a where A.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE A.order_id = 2");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "SELECT * FROM order_0 a");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 2)), "select * from order A", "ds_0", "SELECT * FROM order_0 A");
        assertSingleTarget(Lists.newArrayList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "SELECT * FROM order_0 a");
    }
    
    @Test
    public void assertSelectWithTableNameAsAlias() throws SQLParserException {
        assertSingleTarget("select * from order where order.order_id = 10", "ds_0", "SELECT * FROM order_0 WHERE order_0.order_id = 10");
    }
    
    @Test
    public void assertSelectWithIn() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id in (?,?,?)", Arrays.<Object>asList(1, 2, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?)", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?)"));
        assertMultipleTargets(Lists.newArrayList(new ShardingValuePair("order", Condition.BinaryOperator.IN, 1, 2, 100)), "select * from order", 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0", "SELECT * FROM order_1"));
    }
    
    @Test
    public void assertSelectWithInAndIntersection() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id in (?,?) or order_id in (?,?)", Arrays.<Object>asList(1, 2, 100, 2), 4,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("SELECT * FROM order_1 WHERE order_id IN (?, ?) OR order_id IN (?, ?)", "SELECT * FROM order_1 WHERE order_id IN (?, ?) OR order_id IN (?, ?)"));
    }
    
    @Test
    public void assertSelectWithBetween() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id between ? and ?", Arrays.<Object>asList(1, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id BETWEEN ? AND ?", "SELECT * FROM order_1 WHERE order_id BETWEEN ? AND ?"));
        assertMultipleTargets(Lists.newArrayList(new ShardingValuePair("order", Condition.BinaryOperator.BETWEEN, 1, 100)), "select * from order", 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0", "SELECT * FROM order_1"));
    }
    
    @Test
    public void assertSelectWithBetweenAndIntersection() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id between ? and ? or order_id between ? and ? ", Arrays.<Object>asList(1, 50, 29, 100), 4,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("SELECT * FROM order_0 WHERE order_id BETWEEN ? AND ? OR order_id BETWEEN ? AND ?", 
                        "SELECT * FROM order_1 WHERE order_id BETWEEN ? AND ? OR order_id BETWEEN ? AND ?"));
    }
}
