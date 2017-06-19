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

package com.dangdang.ddframe.rdb.integrate.db.pstatement;

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.SQLException;

public class ShardingDatabaseOnlyForPreparedStatementWithGroupByTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectSum.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectSumWithGroupBySql());
    }
    
    @Test
    public void assertSelectSumOrderBy() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectSum.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectSumWithOrderByAndGroupBySql());
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectCount.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectCountWithGroupBySql());
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectMax.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectMaxWithGroupBySql());
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectMin.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectMinWithGroupBySql());
    }
    
    @Test
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectAvg.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectAvgWithGroupBySql());
    }
    
    @Test
    public void assertSelectOrderByDesc() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_group_by/SelectOrderByDesc.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectSumWithOrderByDescAndGroupBySql());
    }
}
