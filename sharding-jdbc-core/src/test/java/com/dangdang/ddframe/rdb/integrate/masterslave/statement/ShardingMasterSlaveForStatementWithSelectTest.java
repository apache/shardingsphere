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

package com.dangdang.ddframe.rdb.integrate.masterslave.statement;

import com.dangdang.ddframe.rdb.integrate.masterslave.AbstractShardingMasterSlaveDBUnitTest;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.SQLException;

public final class ShardingMasterSlaveForStatementWithSelectTest extends AbstractShardingMasterSlaveDBUnitTest {
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT i.* FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s ORDER BY i.item_id DESC LIMIT %s, %s";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectLimitWithBindingTable.xml", 
                getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909, 2, 2));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909, 10000, 2));
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count, o.`user_id` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = "SELECT count(*) as items_count FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s GROUP BY o.`user_id`";
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
}
