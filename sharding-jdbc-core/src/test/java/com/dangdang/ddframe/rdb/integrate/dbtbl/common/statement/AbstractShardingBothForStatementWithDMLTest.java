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

package com.dangdang.ddframe.rdb.integrate.dbtbl.common.statement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.AbstractShardingBothTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import lombok.AccessLevel;
import lombok.Getter;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractShardingBothForStatementWithDMLTest extends AbstractShardingBothTest {
    
    @Getter(AccessLevel.PROTECTED)
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertInsert() throws SQLException, DatabaseUnitException {
        String sql = "INSERT INTO `t_order` (`order_id`, `user_id`, `status`) VALUES (%s, %s, '%s')";
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                try (Connection connection = shardingDataSource.getConnection("", "")) {
                    Statement stmt = connection.createStatement();
                    stmt.executeUpdate(String.format(sql, i, j, "insert"));
                }
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertUpdate() throws SQLException, DatabaseUnitException {
        String sql = "UPDATE `t_order` SET `status` = '%s' WHERE `order_id` = %s AND `user_id` = %s";
        for (int i = 10; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                try (Connection connection = shardingDataSource.getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format(sql, "updated", i * 100 + j, i)), is(1));
                }
            }
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertDelete() throws SQLException, DatabaseUnitException {
        String sql = "DELETE FROM `t_order` WHERE `order_id` = %s AND `user_id` = %s";
        for (int i = 10; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                try (Connection connection = shardingDataSource.getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format(sql, i * 100 + j, i)), is(1));
                }
            }
        }
        assertDataSet("delete", "init");
    }
    
    protected void assertDataSet(final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertDataSet(String.format("integrate/dataset/dbtbl/expect/%s/dbtbl_%s.xml", expectedDataSetPattern, i), 
                        shardingDataSource.getConnection().getConnection(String.format("dataSource_dbtbl_%s", i), SQLType.SELECT), 
                        String.format("t_order_%s", j), String.format("SELECT * FROM `t_order_%s` WHERE `status`=?", j), status);
            }
        }
    }
}
