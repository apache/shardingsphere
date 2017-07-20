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
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;

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
        shardingDataSource.close();
    }
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql());
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLimitWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 10000, 2);
    }
    
    @Test
    public void assertSelectLimitWithBindingTableWithoutOffset() throws SQLException, DatabaseUnitException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getSelectPagingWithRowCountSql());
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLimitWithBindingTableWithoutOffset.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 2);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909, 0);
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableSql());
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql());
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectGroupByWithoutGroupedColumn.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectWithBindingTableAndConfigTable() throws SQLException, DatabaseUnitException {
        String sql = replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableAndConfigSql());
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectWithBindingTableAndConfigTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 11, 1009, 1108, "init");
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", sql, 10, 11, 1009, 1108, "none");
    }
    
    @Test
    public void assertSelectWithNoShardingTable() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectWithNoShardingTableSql();
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectNoShardingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql);
    }
}
