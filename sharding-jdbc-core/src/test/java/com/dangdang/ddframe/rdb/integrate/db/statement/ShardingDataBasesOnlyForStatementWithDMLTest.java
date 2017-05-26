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

import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDataBasesOnlyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ShardingDataBasesOnlyForStatementWithDMLTest extends AbstractShardingDataBasesOnlyDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertInsert() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (Connection connection = shardingDataSource.getConnection()) {
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate(String.format("INSERT INTO `t_order` (`order_id`, `user_id`, `status`) VALUES (%s, %s, '%s')", i, i, "insert"));
                connection.commit();
                connection.close();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertInsertWithGenerateKeyColumn() throws SQLException, DatabaseUnitException {
        for (int i = 1; i <= 10; i++) {
            try (Connection connection = shardingDataSource.getConnection()) {
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate(String.format("INSERT INTO `t_order` (`user_id`, `status`) VALUES (%s, '%s')", i, "insert"));
                connection.commit();
                connection.close();
            }
        }
        assertDataSet("insert", "insert");
    }
    
    @Test
    public void assertUpdate() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (Connection connection = shardingDataSource.getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format("UPDATE `t_order` SET `status` = '%s' WHERE `order_id` = %s AND `user_id` = %s", "updated", i * 100 + j, i)), is(1));
                }
            }
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertUpdateWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = shardingDataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format("UPDATE `t_order` SET `status` = '%s' WHERE `status` = '%s'", "updated", "init")), is(40));
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertDelete() throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (Connection connection = shardingDataSource.getConnection()) {
                    Statement stmt = connection.createStatement();
                    assertThat(stmt.executeUpdate(String.format("DELETE FROM `t_order` WHERE `order_id` = %s AND `user_id` = %s AND `status` = '%s'", i * 100 + j, i, "init")), is(1));
                }
            }
        }
        assertDataSet("delete", "init");
    }
    
    @Test
    public void assertDeleteWithoutShardingValue() throws SQLException, DatabaseUnitException {
        try (Connection connection = shardingDataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            assertThat(stmt.executeUpdate(String.format("DELETE FROM `t_order` WHERE `status` = '%s'", "init")), is(40));
        }
        assertDataSet("delete", "init");
    }
    
    private void assertDataSet(final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            assertDataSet(String.format("integrate/dataset/db/expect/%s/db_%s.xml", expectedDataSetPattern, i),
                    shardingDataSource.getConnection().getConnection(String.format("dataSource_db_%s", i), SQLType.SELECT), "t_order", "SELECT * FROM `t_order` WHERE `status`=?", status);
        }
    }
}
