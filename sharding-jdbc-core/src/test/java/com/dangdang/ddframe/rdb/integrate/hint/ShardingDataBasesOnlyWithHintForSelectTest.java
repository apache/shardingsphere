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

package com.dangdang.ddframe.rdb.integrate.hint;

import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.google.common.collect.Lists;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

public final class ShardingDataBasesOnlyWithHintForSelectTest extends AbstractShardingDataBasesOnlyHintDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` = ? AND `order_id` = ?";
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_0.xml", new DynamicShardingValueHelper(10, 1000), shardingDataSource.getConnection(), "t_order", sql, 10, 1000);
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_1.xml", new DynamicShardingValueHelper(12, 1201), shardingDataSource.getConnection(), "t_order", sql, 12, 1201);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicShardingValueHelper(12, 1000), shardingDataSource.getConnection(), "t_order", sql, 12, 1000);
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` BETWEEN ? AND ? AND `order_id` BETWEEN ? AND ? ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/db/expect/select/SelectBetweenWithSingleTable.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12), 
                Condition.BinaryOperator.BETWEEN, Lists.newArrayList(1001, 1200), Condition.BinaryOperator.BETWEEN), shardingDataSource.getConnection(), "t_order", sql, 10, 12, 1001, 1200);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12), 
                Condition.BinaryOperator.BETWEEN, Lists.newArrayList(1309, 1408), Condition.BinaryOperator.BETWEEN), shardingDataSource.getConnection(), "t_order", sql, 10, 12, 1309, 1408);
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` IN (?, ?, ?) AND `order_id` IN (?, ?) ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_0.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15), 
                Condition.BinaryOperator.IN, Lists.newArrayList(1000, 1201), Condition.BinaryOperator.IN), shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1000, 1201);
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_1.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15), 
                Condition.BinaryOperator.IN, Lists.newArrayList(1000, 1101), Condition.BinaryOperator.IN), shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1000, 1101);
        assertDataSet("integrate/dataset/Empty.xml", new DynamicShardingValueHelper(Lists.newArrayList(10, 12, 15), 
                Condition.BinaryOperator.IN, Lists.newArrayList(1309, 1408), Condition.BinaryOperator.IN), shardingDataSource.getConnection(), "t_order", sql, 10, 12, 15, 1309, 1408);
    }
    
}
