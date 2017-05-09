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

import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class SelectSingleTableTest extends AbstractDynamicRouteSqlTest {

    @Test
    public void assertGroupBy() {
        assertSingleTargetWithoutParameter("select sum(qty) from order where order_id = 1 group by tenant_id", "ds_1",
                "select sum(qty) , tenant_id AS sharding_gen_1 from order_1 where order_id = 1 group by tenant_id");
//        assertMultipleTargetsWithoutParameters("select sum(qty) from order group by tenant_id", 4, Arrays.asList("ds_0", "ds_1"),
//                Arrays.asList("select sum(qty) , tenant_id as sharding_gen_1 from order_0 group by tenant_id", "select sum(qty) , tenant_id as sharding_gen_1 from order_1 group by tenant_id"));
    }
    
    @Test
    public void assertSingleSelect() {
        assertSingleTargetWithoutParameter("select * from order where order_id = 1", "ds_1", "select * from order_1 where order_id = 1");
        assertSingleTargetWithoutParameter(Collections.singletonList(new ShardingValuePair("order", 1)), "select * from order", "ds_1", "select * from order_1");
        assertSingleTargetWithoutParameter(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order", "ds_0", "select * from order_0");
        assertSingleTargetWithParameters("select * from order where order_id = ?", Collections.<Object>singletonList(2), "ds_0", "select * from order_0 where order_id = ?");
    }
    
    @Test
    public void assertSelectWithAlias() {
        assertSingleTargetWithoutParameter("select * from order a where a.order_id = 2", "ds_0", "select * from order_0 a where a.order_id = 2");
        assertSingleTargetWithoutParameter("select * from order A where a.order_id = 2", "ds_0", "select * from order_0 A where a.order_id = 2");
        assertSingleTargetWithoutParameter("select * from order a where A.order_id = 2", "ds_0", "select * from order_0 a where A.order_id = 2");
        assertSingleTargetWithoutParameter(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "select * from order_0 a");
        assertSingleTargetWithoutParameter(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order A", "ds_0", "select * from order_0 A");
        assertSingleTargetWithoutParameter(Collections.singletonList(new ShardingValuePair("order", 2)), "select * from order a", "ds_0", "select * from order_0 a");
    }
    
    @Test
    public void assertSelectWithTableNameAsAlias() {
        assertSingleTargetWithoutParameter("select * from order where order.order_id = 10", "ds_0", "select * from order_0 where order_0.order_id = 10");
    }
    
    @Test
    public void assertSelectWithIn() {
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?)", Arrays.<Object>asList(1, 2, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?)", "select * from order_1 where order_id in (?,?,?)"));
        assertMultipleTargetsWithoutParameter(Collections.singletonList(new ShardingValuePair("order", ShardingOperator.IN, 1, 2, 100)), "select * from order", 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0", "select * from order_1"));
    }
    
    @Test
    @Ignore
    // TODO or
    public void assertSelectWithInAndIntersection() {
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?) or order_id in (?,?)", Arrays.<Object>asList(1, 2, 100, 2), 4,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("select * from order_1 where order_id in (?,?) or order_id in (?,?)", "select * from order_1 where order_id in (?, ?) or order_id in (?, ?)"));
    }
    
    @Test
    public void assertSelectWithBetween() {
        assertMultipleTargetsWithParameters("select * from order where order_id between ? and ?", Arrays.<Object>asList(1, 100), 4, 
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id between ? and ?", "select * from order_1 where order_id between ? and ?"));
        assertMultipleTargetsWithoutParameter(Collections.singletonList(new ShardingValuePair("order", ShardingOperator.BETWEEN, 1, 100)), "select * from order", 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0", "select * from order_1"));
    }
    
    @Test
    @Ignore
    // TODO or
    public void assertSelectWithBetweenAndIntersection() {
        assertMultipleTargetsWithParameters("select * from order where order_id between ? and ? or order_id between ? and ? ", Arrays.<Object>asList(1, 50, 29, 100), 4,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("select * from order_0 where order_id between ? and ? or order_id between ? and ? ", 
                        "select * from order_1 where order_id between ? and ? or order_id between ? and ? "));
    }
    
    @Test
    public void assertSingleSelectLimit() {
        assertSingleTargetWithoutParameter("select * from order where order_id = 1 limit 5", "ds_1", "select * from order_1 where order_id = 1 limit 5");
        assertSingleTargetWithoutParameter("select * from order where order_id = 1 limit 2,5", "ds_1", "select * from order_1 where order_id = 1 limit 2,5");
        assertSingleTargetWithoutParameter("select * from order where order_id = 1 limit 5 offset 2", "ds_1", "select * from order_1 where order_id = 1 limit 5 offset 2");
        List<Object> parameters = Arrays.<Object>asList(2, 4, 5);
        assertSingleTargetWithParameters("select * from order where order_id = ? limit ?,?", parameters, "ds_0", "select * from order_0 where order_id = ? limit ?,?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 4, 5)));
        parameters = Arrays.<Object>asList(2, 4, 5);
        assertSingleTargetWithParameters("select * from order where order_id = ? limit ? offset ?", parameters, "ds_0", "select * from order_0 where order_id = ? limit ? offset ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 4, 5)));
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTargetWithParameters("select * from order where order_id = ? limit ?", parameters, "ds_0", "select * from order_0 where order_id = ? limit ?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTargetWithParameters("select * from order where order_id = ? limit ?,10", parameters, "ds_0", "select * from order_0 where order_id = ? limit ?,10");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
        parameters = Arrays.<Object>asList(2, 5);
        assertSingleTargetWithParameters("select * from order where order_id = ? limit 10,?", parameters, "ds_0", "select * from order_0 where order_id = ? limit 10,?");
        assertThat(parameters, is(Arrays.<Object>asList(2, 5)));
    }
    
    @Test
    public void assertSelectInLimit() {
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit 5", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit 5", "select * from order_1 where order_id in (?,?,?) limit 5"));
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit 2,5", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit 0,7", "select * from order_1 where order_id in (?,?,?) limit 0,7"));
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit 5 offset 2", Arrays.<Object>asList(1, 2, 100), 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit 7 offset 0", "select * from order_1 where order_id in (?,?,?) limit 7 offset 0"));
        List<Object> parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit ?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit ?", "select * from order_1 where order_id in (?,?,?) limit ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 5)));
        parameters = Arrays.<Object>asList(1, 2, 100, 2, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit ?,?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit ?,?", "select * from order_1 where order_id in (?,?,?) limit ?,?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
        parameters = Arrays.<Object>asList(1, 2, 100, 5, 2);
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit ? offset ?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit ? offset ?", "select * from order_1 where order_id in (?,?,?) limit ? offset ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 7, 0)));
        parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit 2,?", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit 0,?", "select * from order_1 where order_id in (?,?,?) limit 0,?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 7)));
        parameters = Arrays.<Object>asList(1, 2, 100, 2);
        assertMultipleTargetsWithParameters("select * from order where order_id in (?,?,?) limit ?,5", parameters, 4,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList("select * from order_0 where order_id in (?,?,?) limit ?,7", "select * from order_1 where order_id in (?,?,?) limit ?,7"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0)));
    }
    
    @Test
    @Ignore
    public void assertSelectOrLimit() {
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit 5", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit 5", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit 5"));
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit 2,5", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                        "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit 0, 7", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit 0, 7"));
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit 5 offset 2", Arrays.<Object>asList(1, 2, 100), 2,
                Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                        "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit 0, 7", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit 0, 7"));
    
        List<Object> parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?", parameters, 2, Arrays.asList("ds_0", "ds_1"), 
                Arrays.asList("select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit ?", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 5)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?,?", parameters, 2, Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit ?, ?", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5, 2);
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit ? offset ?", parameters, 2, Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit ?, ?", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit ?, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 5);
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit 2,?", parameters, 2, Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit 0, ?", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit 0, ?"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 7)));
    
        parameters = Arrays.<Object>asList(1, 2, 100, 2);
        assertMultipleTargetsWithParameters("select * from order where order_id = ? or order_id = ? or order_id = ? limit ?,5", parameters, 2, Arrays.asList("ds_0", "ds_1"), Arrays.asList(
                "select * from order_0 where order_id = ? or order_id = ? or order_id = ? limit ?, 7", "select * from order_1 where order_id = ? or order_id = ? or order_id = ? limit ?, 7"));
        assertThat(parameters, is(Arrays.<Object>asList(1, 2, 100, 0)));
    }
}
