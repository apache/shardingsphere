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

package com.dangdang.ddframe.rdb.integrate.tbl;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSource;

public final class SelectGroupByShardingTablesOnlyTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        String sql = "SELECT SUM(order_id) AS `orders_sum`, `user_id` FROM `t_order` GROUP BY `user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectSum.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        String sql = "SELECT COUNT(order_id) AS `orders_count`, `user_id` FROM `t_order` GROUP BY `user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectCount.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MAX(order_id) AS `max_order_id`, `user_id` FROM `t_order` GROUP BY `user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectMax.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MIN(order_id) AS `min_order_id`, `user_id` FROM `t_order` GROUP BY `user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectMin.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        String sql = "SELECT AVG(order_id) AS `orders_avg`, `user_id` FROM `t_order` GROUP BY `user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectAvg.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectOrderByDesc() throws SQLException, DatabaseUnitException {
        String sql = "SELECT SUM(order_id) AS `orders_sum`, `user_id` FROM `t_order` GROUP BY `user_id` ORDER BY orders_sum DESC";
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectOrderByDesc.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
}
