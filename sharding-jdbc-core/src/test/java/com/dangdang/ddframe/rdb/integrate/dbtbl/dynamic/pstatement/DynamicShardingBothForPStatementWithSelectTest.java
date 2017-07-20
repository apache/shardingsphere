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

package com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.pstatement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.pstatement.AbstractShardingBothForPStatementWithSelectTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.dynamic.DynamicShardingBothHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.AfterClass;
import org.junit.Test;

import java.sql.SQLException;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.MySQL;

public final class DynamicShardingBothForPStatementWithSelectTest extends AbstractShardingBothForPStatementWithSelectTest {
    
    private static ShardingDataSource shardingDataSource;
    
    @Override
    protected ShardingDataSource getShardingDataSource() {
        if (null != shardingDataSource) {
            return shardingDataSource;
        }
        shardingDataSource = DynamicShardingBothHelper.getShardingDataSource(createDataSourceMap("dataSource_%s"));
        return shardingDataSource;
    }
    
    @AfterClass
    public static void clear() {
        shardingDataSource.close();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertSelectWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectWithBindingTableSql();
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLimitWithBindingTable.xml", getShardingDataSource().getConnection(), 
                "t_order_item", sql, 10, 19, 1000, 1909);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSelectNoShardingTable() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectWithNoShardingTableSql();
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectNoShardingTable.xml", getShardingDataSource().getConnection(), "t_order_item", sql);
    }
    
    @Test
    public void assertSelectForFullTableNameWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/dbtbl/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectForFullTableNameWithSingleTableSql(), 10, 1000);
    }
    
    @Test
    public void assertSelectLikeWithBindingTable() throws SQLException, DatabaseUnitException {
        if (currentDbType() == MySQL) {
            assertDataSet("integrate/dataset/dbtbl/expect/select/SelectLikeWithCount.xml", getShardingDataSource().getConnection(),
                    "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectLikeWithCountSql()), "init", 10, 11, 1000, 1001);
        }
    }
}
