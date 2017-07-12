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
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.H2;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.MySQL;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.Oracle;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.PostgreSQL;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.SQLServer;

public final class ShardingTablesOnlyForPreparedStatementWithSelectTest extends AbstractShardingTablesOnlyDBUnitTest {
    
    private static final String TABLE_ONLY_PREFIX = "integrate/dataset/tbl";
    
    private ShardingDataSource shardingDataSource;
    
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectEqualsWithSingleTable_0.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql()), 10, 1000);
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectEqualsWithSingleTable_1.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql()), 11, 1109);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql()), 12, 1000);
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectBetweenWithSingleTable.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectBetweenWithSingleTableSql()), 10, 12, 1009, 1108);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectBetweenWithSingleTableSql()), 10, 12, 1309, 1408);
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectInWithSingleTable_0.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectInWithSingleTableSql()), 10, 11, 15, 1009, 1108);
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectInWithSingleTable_1.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectInWithSingleTableSql()), 10, 12, 15, 1009, 1108);
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), 
                "t_order", replacePreparedStatement(getDatabaseTestSQL().getSelectInWithSingleTableSql()), 10, 12, 15, 1309, 1408);
    }
    
    @Test
    public void assertSelectSubquerySingleTableWithParenthesesSql() throws SQLException, DatabaseUnitException {
        if (CURRENT_DB_TYPE != H2) {
            String sql = getDatabaseTestSQL().getSelectSubquerySingleTableWithParenthesesSql();
            assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectSubquerySingleTableWithParentheses.xml", shardingDataSource.getConnection(),
                    "t_order", replacePreparedStatement(sql), 1000, 1001);
        }
    }
    
    @Test
    public void assertSelectSubqueryMultiTableWithParenthesesSql() throws SQLException, DatabaseUnitException {
        if (CURRENT_DB_TYPE != H2) {
            String sql = getDatabaseTestSQL().getSelectSubqueryMultiTableWithParenthesesSql();
            assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectSubqueryMultiTableWithParentheses.xml", shardingDataSource.getConnection(),
                    "t_order", replacePreparedStatement(sql), 1000, 1001);
        }
    }
    
    @Test
    public void assertSelectIteratorSql() throws SQLException, DatabaseUnitException {
        String sql = getDatabaseTestSQL().getSelectIteratorSql();
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectIteratorSql_0.xml", shardingDataSource.getConnection(), "t_order_item", replacePreparedStatement(sql), 100000, 100001);
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectIteratorSql_1.xml", shardingDataSource.getConnection(), "t_order_item", replacePreparedStatement(sql), 100900, 100901);
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectIteratorSql_2.xml", shardingDataSource.getConnection(), "t_order_item", replacePreparedStatement(sql), 100000, 100900);
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectIteratorSql_3.xml", shardingDataSource.getConnection(), "t_order_item", replacePreparedStatement(sql), 100000, 100200);
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item", replacePreparedStatement(sql), 10, 11);
    }
    
    @Test
    public void assertSelectPagingWithOffsetAndRowCountSql() throws SQLException, DatabaseUnitException {
        if (currentDbType() == SQLServer) {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", replacePreparedStatement(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql()), 4, 10, 19, 1000, 1909, 2);
        } else if (currentDbType() == Oracle) {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", replacePreparedStatement(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql()), 10, 19, 1000, 1909, 4, 2);
        } else {
            assertSelectPaging("SelectPagingWithOffsetAndRowCountSql.xml", replacePreparedStatement(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql()), 10, 19, 1000, 1909, 2, 2);
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
    
    @Test
    public void assertSelectPagingWithOffsetSql() throws SQLException, DatabaseUnitException {
        if (currentDbType() == PostgreSQL) {
            assertSelectPaging("SelectPagingWithOffsetSql.xml", getDatabaseTestSQL().getSelectPagingWithOffsetSql(), 10, 19, 1000, 1909, 18);
        }
    }
    
    private void assertSelectPaging(final String expectedDataSetFileName, final String sql, final Object... params) throws SQLException, DatabaseUnitException {
        assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/" + currentDbType().name().toLowerCase() + "/" + expectedDataSetFileName, 
                getShardingDataSource().getConnection(), "t_order_item", replacePreparedStatement(sql), params);
    }
    
    @Test
    public void assertSelectLikeWithBindingTable() throws SQLException, DatabaseUnitException {
        if (currentDbType() == MySQL) {
            assertDataSet(TABLE_ONLY_PREFIX + "/expect/select/SelectLikeWithCount.xml", getShardingDataSource().getConnection(),
                    "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectLikeWithCountSql()), "init", 10, 11, 1000, 1909);
        }
    }
    
    @Test
    public void assertSelectNoShardingTable() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = TABLE_ONLY_PREFIX + "/expect/select/SelectNoShardingTable.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(),
                "t_order_item", getDatabaseTestSQL().getSelectWithNoShardingTableSql());
    }
    
    @Test
    public void assertSelectWithBindingTableAndConfigTable() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = TABLE_ONLY_PREFIX + "/expect/select/SelectWithBindingTableAndConfigTable.xml";
        assertDataSet(expectedDataSetFile, shardingDataSource.getConnection(), 
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableAndConfigSql()), 10, 11, 1009, 1108, "init");
        assertDataSet("integrate/dataset/Empty.xml", shardingDataSource.getConnection(), 
                "t_order_item", replacePreparedStatement(getDatabaseTestSQL().getSelectGroupWithBindingTableAndConfigSql()), 10, 11, 1009, 1108, "none");
    }
}
