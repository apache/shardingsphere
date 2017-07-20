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

import com.dangdang.ddframe.rdb.integrate.tbl.AbstractShardingTablesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;

public final class ShardingTablesOnlyForPreparedStatementWithGroupByTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectSum.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectSumWithGroupBySql());
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectCount.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectCountWithGroupBySql());
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectMax.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectMaxWithGroupBySql());
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectMin.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectMinWithGroupBySql());
    }
    
    @Test
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectAvg.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectAvgWithGroupBySql());
    }
    
    @Test
    public void assertSelectOrderByDesc() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select_group_by/SelectOrderByDesc.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectSumWithOrderByDescAndGroupBySql());
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/tbl/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(),
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableSql()), 10, 11, 1000, 1109);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableSql()), 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile =  "integrate/dataset/tbl/expect/select/SelectGroupByWithoutGroupedColumn.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(),
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql()), 10, 11, 1000, 1109);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql()), 1, 9, 1000, 1909);
    }
    
}
