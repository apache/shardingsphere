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
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingMasterSlaveForStatementWithDMLTest extends AbstractShardingMasterSlaveDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertUpdateWithoutShardingValue() throws SQLException, DatabaseUnitException {
        assertSelectUpdate(false);
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getUpdateWithoutShardingValueSql(), "'updated'", "'init_master'")), is(100));
        }
        assertDataSet("update", "updated");
        assertSelectUpdate(true);
    }
    
    private void assertSelectUpdate(final boolean updated) throws SQLException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            if (updated) {
                assertTrue(stmt.executeQuery(String.format(getDatabaseTestSQL().getAssertSelectWithStatusSql(), "'updated'")).next());
            } else {
                assertFalse(stmt.executeQuery(String.format(getDatabaseTestSQL().getAssertSelectWithStatusSql(), "'updated'")).next());
            }
        }
    }
    
    @Test
    public void assertDeleteWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = getShardingDataSource().getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format(getDatabaseTestSQL().getDeleteWithoutShardingValueSql(), "'init_master'")), is(100));
        }
        assertDataSet("delete", "init");
    }
    
    protected void assertDataSet(final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertDataSet(String.format("integrate/dataset/masterslave/expect/%s/master_%s.xml", expectedDataSetPattern, i),
                        shardingDataSource.getConnection().getConnection(String.format("ms_%s", i), SQLType.INSERT),
                        String.format("t_order_%s", j), String.format(getDatabaseTestSQL().getAssertSelectShardingTablesWithStatusSql(), j), status);
            }
        }
    }
}
