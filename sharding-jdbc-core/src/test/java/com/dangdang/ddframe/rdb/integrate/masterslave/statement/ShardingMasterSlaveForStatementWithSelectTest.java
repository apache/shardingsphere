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

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.Oracle;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.SQLServer;

public final class ShardingMasterSlaveForStatementWithSelectTest extends AbstractShardingMasterSlaveDBUnitTest {
    
    @Test
    public void assertSelectPagingWithOffsetAndRowCountSql() throws SQLException, DatabaseUnitException {
        if (currentDbType() == SQLServer) {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql(), 2, 10, 19, 1000, 1909, 1);
        } else if (currentDbType() == Oracle) {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql(), 10, 19, 1000, 1909, 4, 2);
        } else {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql(), 10, 19, 1000, 1909, 2, 2);
        }
    }
    
    @Test
    public void assertSelectPagingWithRowCountSql() throws SQLException, DatabaseUnitException {
        if (currentDbType() == SQLServer) {
            assertSelectPaging("SelectPagingWithRowCountSql.xml", getDatabaseTestSQL().getSelectPagingWithRowCountSql(), 2, 10, 19, 1000, 1909);
        } else {
            assertSelectPaging("SelectPagingWithRowCountSql.xml", getDatabaseTestSQL().getSelectPagingWithRowCountSql(), 10, 19, 1000, 1909, 2);
        }
    }
    
    private void assertSelectPaging(final String expectedDataSetFileName, final String sql, final Object... params) throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/masterslave/expect/select/" + currentDbType().name().toLowerCase() + "/" + expectedDataSetFileName,
                getShardingDataSource().getConnection(), "t_order_item", replacePreparedStatement(sql), params);
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectGroupWithBindingTableSql();
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql();
        assertDataSet("integrate/dataset/masterslave/expect/select/SelectGroupByWithoutGroupedColumn.xml", getShardingDataSource().getConnection(), 
                "t_order_item", String.format(sql, 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", String.format(sql, 1, 9, 1000, 1909));
    }
}
