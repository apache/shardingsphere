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
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class SelectSingleTableTest extends AbstractDynamicRouteSqlTest {
    
    @Test
    public void assertSingleSelect() throws SQLParserException {
        assertSingleTarget("select * from order where order_id = 1", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1");
        assertSingleTarget("select * from order where order_id = ?", Collections.<Object>singletonList(2), "ds_0", "SELECT * FROM order_0 WHERE order_id = ?");
        assertSingleTarget(Collections.singletonList(new ShardingValuePair("order", 1)), "select * from order", "ds_1", "SELECT * FROM order_1");
        assertSingleTarget(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order", "ds_0", "SELECT * FROM order_0");
    }
    
    @Test
    public void assertSelectWithAlias() throws SQLParserException {
        assertSingleTarget("select * from order a where a.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE a.order_id = 2");
        assertSingleTarget("select * from order A where a.order_id = 2", "ds_0", "SELECT * FROM order_0 A WHERE a.order_id = 2");
        assertSingleTarget("select * from order a where A.order_id = 2", "ds_0", "SELECT * FROM order_0 a WHERE A.order_id = 2");
        assertSingleTarget(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "SELECT * FROM order_0 a");
        assertSingleTarget(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order A", "ds_0", "SELECT * FROM order_0 A");
        assertSingleTarget(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "SELECT * FROM order_0 a");
    }
    
    @Test
    public void assertSelectWithTableNameAsAlias() throws SQLParserException {
        assertSingleTarget("select * from order where order.order_id = 10", "ds_0", "SELECT * FROM order_0 WHERE order_0.order_id = 10");
    }
    
    @Test
    public void assertSelectWithIn() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id in (?,?,?)", Arrays.<Object>asList(1, 2, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?)", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?)"));
        assertMultipleTargets(Collections.singletonList(new ShardingValuePair("order", Condition.BinaryOperator.IN, 1, 2, 100)), "select * from order", 4,
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
        assertMultipleTargets(Collections.singletonList(new ShardingValuePair("order", Condition.BinaryOperator.BETWEEN, 1, 100)), "select * from order", 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0", "SELECT * FROM order_1"));
    }
    
    @Test
    public void assertSelectWithBetweenAndIntersection() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id between ? and ? or order_id between ? and ? ", Arrays.<Object>asList(1, 50, 29, 100), 4,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("SELECT * FROM order_0 WHERE order_id BETWEEN ? AND ? OR order_id BETWEEN ? AND ?", 
                        "SELECT * FROM order_1 WHERE order_id BETWEEN ? AND ? OR order_id BETWEEN ? AND ?"));
    }
    
    @Test
    public void assertSingleSelectLimit() throws SQLParserException {
        assertSingleTarget("select * from order where order_id = 1 limit 5", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1 LIMIT 5");
        assertSingleTarget("select * from order where order_id = 1 limit 2,5", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1 LIMIT 2, 5");
        assertSingleTarget("select * from order where order_id = 1 limit 5 offset 2", "ds_1", "SELECT * FROM order_1 WHERE order_id = 1 LIMIT 2, 5");
    
        List<Object> parameters = Arrays.<Object>asList(2, 4, 5);
        assertSingleTarget("select * from order where order_id = ? limit ?,?", parameters, "ds_0", "SELECT * FROM order_0 WHERE order_id = ? LIMIT ?, ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 4, 5)));
    
        parameters = Arrays.<Object>asList(2, 4, 5);
        assertSingleTarget("select * from order where order_id = ? limit ? offset ?", parameters, "ds_0", "SELECT * FROM order_0 WHERE order_id = ? LIMIT ?, ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5, 4)));
    
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTarget("select * from order where order_id = ? limit ?", parameters, "ds_0", "SELECT * FROM order_0 WHERE order_id = ? LIMIT ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
    
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTarget("select * from order where order_id = ? limit ?,10", parameters, "ds_0", "SELECT * FROM order_0 WHERE order_id = ? LIMIT ?, 10");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
    
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTarget("select * from order where order_id = ? limit 10,?", parameters, "ds_0", "SELECT * FROM order_0 WHERE order_id = ? LIMIT 10, ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
    }
    
    @Test
    public void assertSelectInLimit() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit 5", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT 5", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT 5"));
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit 2,5", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT 0, 7", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT 0, 7"));
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit 5 offset 2", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT 0, 7", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT 0, 7"));
    
        List<Object> parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit ?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT ?", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 5)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2, 5);
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit ?,?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT ?, ?", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5, 2);
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit ? offset ?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT ?, ?", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit 2,?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT 0, ?", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT 0, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2);
        assertMultipleTargets("select * from order where order_id in (?,?,?) limit ?,5", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id IN (?, ?, ?) LIMIT ?, 7", "SELECT * FROM order_1 WHERE order_id IN (?, ?, ?) LIMIT ?, 7"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0)));
    }
    
    @Test
    public void assertSelectOrLimit() throws SQLParserException {
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit 5", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 5", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 5"));
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit 2,5", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, 7", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, 7"));
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit 5 offset 2", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, 7", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, 7"));
    
        List<Object> parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?", parameters, 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 5)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2, 5);
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?,?", parameters, 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, ?", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5, 2);
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit ? offset ?", parameters, 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, ?", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit 2,?", parameters, 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, ?", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT 0, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2);
        assertMultipleTargets("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?,5", parameters, 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("SELECT * FROM order_0 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, 7", "SELECT * FROM order_1 WHERE order_id = ? OR order_id = ? OR order_id = ? LIMIT ?, 7"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0)));
    }
}
