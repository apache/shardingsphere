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

package com.dangdang.ddframe.rdb.integrate.db.statement;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.SQLException;

public final class ShardingDatabaseOnlyForStatementWithSelectTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    @Test
    public void assertSelectEqualsWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_0.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql(), 10, 1000));
        assertDataSet("integrate/dataset/db/expect/select/SelectEqualsWithSingleTable_1.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql(), 12, 1201));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectEqualsWithSingleTableSql(), 12, 1000));
    }
    
    @Test
    public void assertSelectBetweenWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select/SelectBetweenWithSingleTable.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectBetweenWithSingleTableSql(), 10, 12, 1001, 1200));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectBetweenWithSingleTableSql(), 10, 12, 1309, 1408));
    }
    
    @Test
    public void assertSelectInWithSingleTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_0.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectInWithSingleTableSql(), 10, 12, 15, 1000, 1201));
        assertDataSet("integrate/dataset/db/expect/select/SelectInWithSingleTable_1.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectInWithSingleTableSql(), 10, 12, 15, 1000, 1101));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectInWithSingleTableSql(), 10, 12, 15, 1309, 1408));
    }
    
    @Test
    public void assertSelectLimitWithBindingTable() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = "integrate/dataset/db/expect/select/SelectLimitWithBindingTable.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(),
                "t_order_item", String.format(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql(), 10, 19, 1000, 1909, 2, 2));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order_item", String.format(getDatabaseTestSQL().getSelectPagingWithOffsetAndRowCountSql(), 10, 19, 1000, 1909, 10000, 2));
    }
    
    @Test
    public void assertSelectOrderByWithAlias() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = "integrate/dataset/db/expect/select/SelectOrderByWithAlias.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectOrderByWithAliasSql(), 10, 12, 1001, 1200));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(),
                "t_order", String.format(getDatabaseTestSQL().getSelectOrderByWithAliasSql(), 10, 12, 1309, 1408));
    }
    
    @Test
    public void assertSelectLimitWithBindingTableWithoutOffset() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = "integrate/dataset/db/expect/select/SelectLimitWithBindingTableWithoutOffset.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(), "t_order_item", 
                String.format(getDatabaseTestSQL().getSelectPagingWithRowCountSql(), 10, 19, 1000, 1909, 2));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order_item",
                String.format(getDatabaseTestSQL().getSelectPagingWithRowCountSql(), 10, 19, 1000, 1909, 0));
    }
    
    @Test
    public void assertSelectGroupByWithBindingTable() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select/SelectGroupByWithBindingTable.xml", getShardingDataSource().getConnection(),
                "t_order_item", String.format(getDatabaseTestSQL().getSelectGroupWithBindingTableSql(), 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), 
                "t_order_item", String.format(getDatabaseTestSQL().getSelectGroupWithBindingTableSql(), 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectGroupByWithoutGroupedColumn() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = "integrate/dataset/db/expect/select/SelectGroupByWithoutGroupedColumn.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(),
                "t_order_item", String.format(getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql(), 10, 19, 1000, 1909));
        assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), 
                "t_order_item", String.format(getDatabaseTestSQL().getSelectGroupWithoutGroupedColumnSql(), 1, 9, 1000, 1909));
    }
    
    @Test
    public void assertSelectNoShardingTable() throws SQLException, DatabaseUnitException {
        String expectedDataSetFile = "integrate/dataset/db/expect/select/SelectNoShardingTable.xml";
        assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(), "t_order_item", getDatabaseTestSQL().getSelectWithNoShardingTableSql());
    }
}
