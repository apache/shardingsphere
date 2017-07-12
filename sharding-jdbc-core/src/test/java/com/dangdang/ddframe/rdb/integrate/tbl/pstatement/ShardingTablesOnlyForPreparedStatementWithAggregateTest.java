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

package com.dangdang.ddframe.rdb.integrate.tbl.pstatement;

import com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil;
import com.dangdang.ddframe.rdb.integrate.tbl.AbstractShardingTablesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public final class ShardingTablesOnlyForPreparedStatementWithAggregateTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectSum.xml", shardingDataSource.getConnection(), "t_order", getDatabaseTestSQL().getSelectSumAliasSql());
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectCount.xml", shardingDataSource.getConnection(), "t_order", getDatabaseTestSQL().getSelectCountAliasSql());
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectMax.xml", shardingDataSource.getConnection(), "t_order", getDatabaseTestSQL().getSelectMaxAliasSql());
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectMin.xml", shardingDataSource.getConnection(), "t_order", getDatabaseTestSQL().getSelectMinAliasSql());
    }
    
    @Test
    // TODO 改名 avg SHARDING_GEN_2 SHARDING_GEN_3
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectAvg.xml", shardingDataSource.getConnection(), "t_order", getDatabaseTestSQL().getSelectAvgAliasSql());
    }
    
    @Test
    public void assertSelectCountWithBindingTable() throws SQLException, DatabaseUnitException {
        String selectSql = SqlPlaceholderUtil.replacePreparedStatement(getDatabaseTestSQL().getSelectCountWithBindingTableSql());
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectCountWithBindingTable_0.xml", shardingDataSource.getConnection(), "t_order_item", selectSql, 10, 11, 1000, 1909);
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectCountWithBindingTable_1.xml", shardingDataSource.getConnection(), "t_order_item", selectSql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectCountWithBindingTableAndWithoutJoinSql() throws SQLException, DatabaseUnitException {
        String selectSql = SqlPlaceholderUtil.replacePreparedStatement(getDatabaseTestSQL().getSelectCountWithBindingTableAndWithoutJoinSql());
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectCountWithBindingTable_0.xml", shardingDataSource.getConnection(), "t_order_item", selectSql, 10, 11, 1000, 1909);
        assertDataSet("integrate/dataset/tbl/expect/select_aggregate/SelectCountWithBindingTable_1.xml", shardingDataSource.getConnection(), "t_order_item", selectSql, 1, 9, 1000, 1909);
    }
}
