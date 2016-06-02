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

package com.dangdang.ddframe.rdb.integrate.masterslave.pstatement;

import com.dangdang.ddframe.rdb.integrate.masterslave.AbstractShardingMasterSlaveDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class ShardingMasterSlaveForPStatementWithSelectTest extends AbstractShardingMasterSlaveDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException, NoSuchFieldException, IllegalAccessException {
        shardingDataSource = getShardingDataSource();
        reset();
    }
    
    @Test
    public void assertSelectForFullTableNameWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT `t_order`.order_id, `t_order`.user_id, `t_order`.status FROM `t_order` WHERE `t_order`.`user_id` = ? AND `t_order`.`order_id` = ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 1000);
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE 1 = 1 AND `user_id` = ? AND `order_id` = ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 1000);
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectEqualsWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", sql, 12, 1201);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", sql, 12, 1000);
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` BETWEEN ? AND ? AND `order_id` BETWEEN ? AND ? ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectBetweenWithSingleTable.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 12, 1009, 1108);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 12, 1309, 1408);
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` IN (?, ?, ?) AND `order_id` IN (?, ?) ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectInWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1009, 1208);
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectInWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1009, 1108);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1309, 1408);
    }
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? ORDER BY i.item_id DESC LIMIT ?, ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectLimitWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 10000, 2);
    }
    
    @Test
    public void assertSelectLimitWithBindingTableWithoutOffset() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? ORDER BY i.item_id DESC LIMIT ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectLimitWithBindingTableWithoutOffset.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 0);
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count, o.`user_id` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectForHint() throws SQLException, DatabaseUnitException {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_order", "user_id", 10);
        hintManager.addTableShardingValue("t_order", "order_id", 1000);
        String sql = "SELECT `t_order`.order_id, `t_order`.user_id, `t_order`.status FROM `t_order` WHERE `t_order`.`user_id` = ? AND `t_order`.`order_id` = ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 1000);
    }
    
    @Test
    public void assertSelectForHintAndForceMaster() throws SQLException, DatabaseUnitException {
        HintManager hintManager = HintManager.getInstance();
        hintManager.setMasterRouteOnly();
        hintManager.addDatabaseShardingValue("t_order", "user_id", 10);
        hintManager.addTableShardingValue("t_order", "order_id", 1000);
        String sql = "SELECT `t_order`.order_id, `t_order`.user_id, `t_order`.status FROM `t_order` WHERE `t_order`.`user_id` = ? AND `t_order`.`order_id` = ?";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectEqualsWithSingleMasterTable_0.xml", shardingDataSource.getConnection(), "t_order", sql, 10, 1000);
    }
}
