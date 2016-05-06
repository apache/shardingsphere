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

package com.dangdang.ddframe.rdb.integrate.dbtbl.common.pstatement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.AbstractShardingBothTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public abstract class AbstractShardingBothForPStatementWithAggregateTest extends AbstractShardingBothTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        String sql = "SELECT COUNT(*) AS `orders_count` FROM `t_order`";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectCount.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        String sql = "SELECT SUM(`user_id`) AS `user_id_sum` FROM `t_order`";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectSum.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MAX(`user_id`) AS `max_user_id` FROM `t_order`";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectMax.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MIN(`user_id`) AS `min_user_id` FROM `t_order`";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectMin.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    // TODO 改名 avg SHARDING_GEN_2 SHARDING_GEN_3
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        String sql = "SELECT AVG(`user_id`) AS `user_id_avg` FROM `t_order`";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectAvg.xml", shardingDataSource.getConnection(), "t_order", sql);
    }
    
    @Test
    public void assertSelectCountWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT COUNT(*) AS `items_count` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ?";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectCountWithBindingTable_0.xml", shardingDataSource.getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectCountWithBindingTable_1.xml", shardingDataSource.getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
}
