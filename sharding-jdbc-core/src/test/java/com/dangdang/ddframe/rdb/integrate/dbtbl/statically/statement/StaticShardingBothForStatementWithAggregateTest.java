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

package com.dangdang.ddframe.rdb.integrate.dbtbl.statically.statement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.AbstractShardingBothTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.StaticShardingBothHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public final class StaticShardingBothForStatementWithAggregateTest extends AbstractShardingBothTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Override
    protected ShardingDataSource getShardingDataSource() {
        return StaticShardingBothHelper.getShardingDataSource(createDataSourceMap("dataSource_%s"));
    }
    
    @Test
    public void assertSelectCountWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = "SELECT COUNT(*) AS `items_count` FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
                + " WHERE o.`user_id` IN (%s, %s) AND o.`order_id` BETWEEN %s AND %s";
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectCountWithBindingTable_0.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/dbtbl/expect/select_aggregate/SelectCountWithBindingTable_1.xml", shardingDataSource.getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
}
