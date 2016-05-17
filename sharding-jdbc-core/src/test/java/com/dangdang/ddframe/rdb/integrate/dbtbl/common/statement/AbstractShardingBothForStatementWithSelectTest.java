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

package com.dangdang.ddframe.rdb.integrate.dbtbl.common.statement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.AbstractShardingBothTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public abstract class AbstractShardingBothForStatementWithSelectTest extends AbstractShardingBothTest {
    
    @Getter(AccessLevel.PROTECTED)
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE 1 = 1 AND `user_id` = %s AND `order_id` = %s";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 1000));
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectEqualsWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 12, 1201));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 12, 1000));
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` BETWEEN %s AND %s AND `order_id` BETWEEN %s AND %s ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectBetweenWithSingleTable.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 1009, 1108));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 1309, 1408));
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT * FROM `t_order` WHERE `user_id` IN (%s, %s, %s) AND `order_id` IN (%s, %s) ORDER BY user_id, order_id";
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectInWithSingleTable_0.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 15, 1009, 1208));
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectInWithSingleTable_1.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 15, 1009, 1108));
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), "t_order", String.format(sql, 10, 12, 15, 1309, 1408));
    }
}
