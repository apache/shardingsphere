/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.spring;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class AbstractShardingBothDataBasesAndTablesJUnitTest extends AbstractSpringJUnitTest {
    
    @Test
    public void assertWithAllPlaceholders() throws SQLException {
        insertData();
        selectData();
    }
    
    private void insertData() throws SQLException {
        String orderSql = "INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)";
        String orderItemSql = "INSERT INTO t_order_item (order_item_id, order_id, user_id, status) VALUES (?, ?, ?, ?)";
        String configSql = "INSERT INTO t_config (id, status) VALUES (?, ?)";
        for (int orderId = 1; orderId <= 4; orderId++) {
            for (int userId = 1; userId <= 2; userId++) {
                try (Connection connection = getShardingSphereDataSource().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(orderSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setString(3, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(orderItemSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, orderId);
                    preparedStatement.setInt(3, userId);
                    preparedStatement.setString(4, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(orderItemSql);
                    preparedStatement.setInt(1, orderId + 4);
                    preparedStatement.setInt(2, orderId);
                    preparedStatement.setInt(3, userId);
                    preparedStatement.setString(4, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                    
                    preparedStatement = connection.prepareStatement(configSql);
                    preparedStatement.setInt(1, new Long(System.nanoTime()).intValue());
                    preparedStatement.setString(2, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                }
            }
        }
    }
    
    private void selectData() throws SQLException {
        String sql = "SELECT i.order_id, i.order_item_id  FROM t_order o JOIN t_order_item i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.user_id = ? AND o.order_id = ? AND i.order_id = ? ORDER BY i.order_item_id DESC";
        try (Connection connection = getShardingSphereDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, 1);
            preparedStatement.setInt(3, 1);
            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                if (0 == count) {
                    assertThat(resultSet.getInt(1), is(1));
                    assertThat(resultSet.getInt(2), is(5));
                } else if (1 == count) {
                    assertThat(resultSet.getInt(1), is(1));
                    assertThat(resultSet.getInt(2), is(1));
                }
                count++;
            }
            preparedStatement.close();
        }
    }
}
