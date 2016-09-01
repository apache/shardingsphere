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

package com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.pstatement.AbstractShardingBothForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.StaticShardingBothHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.SQLException;

public final class StaticShardingBothForPStatementWithSelectTest extends AbstractShardingBothForPStatementWithSelectTest {
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected ShardingDataSource getShardingDataSource() {
        if (null != shardingDataSource) {
            return shardingDataSource;
        }
        shardingDataSource = StaticShardingBothHelper.getShardingDataSource(createDataSourceMap("dataSource_%s"));
        return shardingDataSource;
    }
    
    @AfterClass
    public static void clear() {
        shardingDataSource.shutdown();
    }
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? ORDER BY i.item_id DESC LIMIT ?, ?";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLimitWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 10000, 2);
    }
    
    @Test
    public void assertSelectLimitWithBindingTableWithoutOffset() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? ORDER BY i.item_id DESC LIMIT ?";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLimitWithBindingTableWithoutOffset.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 0);
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count, o.`user_id` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectWithBindingTableAndConfigTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id JOIN t_config c ON o.status = c.status"
                + " WHERE o.`user_id` IN (?, ?) AND o.`order_id` BETWEEN ? AND ? AND c.status = ? ORDER BY i.item_id";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectWithBindingTableAndConfigTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 11, 1009, 1108, "init");
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 11, 1009, 1108, "none");
    }
    
    @Test
    public void assertSelectNoShardingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id JOIN t_config c ON o.status = c.status ORDER BY i.item_id";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectNoShardingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql);
    }
}
