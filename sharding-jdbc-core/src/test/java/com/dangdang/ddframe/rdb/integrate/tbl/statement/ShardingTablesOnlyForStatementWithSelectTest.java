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

package com.dangdang.ddframe.rdb.integrate.tbl.statement;

import com.dangdang.ddframe.rdb.integrate.tbl.AbstractShardingTablesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public final class ShardingTablesOnlyForStatementWithSelectTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` = %s AND `order_id` = %s";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 1000));
        assertDataSet("integrate/dataset/tbl/expect/select/SelectEqualsWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 11, 1109));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 12, 1000));
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` BETWEEN %s AND %s AND `order_id` BETWEEN %s AND %s ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectBetweenWithSingleTable.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 1009, 1108));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 1309, 1408));
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` IN (%s, %s, %s) AND `order_id` IN (%s, %s) ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectInWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 11, 15, 1009, 1108));
        assertDataSet("integrate/dataset/tbl/expect/select/SelectInWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 15, 1009, 1108));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 15, 1309, 1408));
    }
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s ORDER BY i.item_id DESC LIMIT %s, %s";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectLimitWithBindingTable.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909, 2, 2));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909, 10000, 2));
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count, o.`user_id` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectGroupByWithBindingTable.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 11, 1000, 1109));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectGroupByWithBindingTable.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 11, 1000, 1109));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectWithBindingTableAndConfigTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id JOIN `t_config` c ON o.status = c.status"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s AND c.status = '%s' ORDER BY i.item_id";
        assertDataSet("integrate/dataset/tbl/expect/select/SelectWithBindingTableAndConfigTable.xml", 
                shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 11, 1009, 1108, "init"));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 11, 1009, 1108, "none"));
    }
}
