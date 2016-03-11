/**
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

package com.dangdang.ddframe.rdb.sharding.spring;

import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractShardingBothDataBasesAndTablesSpringDBUnitTest extends AbstractSpringDBUnitTest {
    
    @Test
    public void testWithAllPlacehloders() throws SQLException {
        insertData();
        selectData();
    }
    
    private void insertData() throws SQLException {
        String orderSql = "INSERT INTO `t_order` (`order_id`, `user_id`, `status`) VALUES (?, ?, ?)";
        String orderItemSql = "INSERT INTO `t_order_item` (`order_item_id`, `order_id`, `user_id`, `status`) VALUES (?, ?, ?, ?)";
        for (int orderId = 1; orderId <= 4; orderId++) {
            for (int userId = 1; userId <= 2; userId++) {
                try (Connection connection = getShardingDataSource().getConnection()) {
                    PreparedStatement pstmt = connection.prepareStatement(orderSql);
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, userId);
                    pstmt.setString(3, "insert");
                    pstmt.execute();
                    pstmt.close();
                    
                    pstmt = connection.prepareStatement(orderItemSql);
                    pstmt.setInt(1, orderId);
                    pstmt.setInt(2, orderId);
                    pstmt.setInt(3, userId);
                    pstmt.setString(4, "insert");
                    pstmt.execute();
                    pstmt.close();
                    
                    pstmt = connection.prepareStatement(orderItemSql);
                    pstmt.setInt(1, orderId + 4);
                    pstmt.setInt(2, orderId);
                    pstmt.setInt(3, userId);
                    pstmt.setString(4, "insert");
                    pstmt.execute();
                    pstmt.close();
                }
            }
        }
    }
    
    private void selectData() throws SQLException {
        String sql = "SELECT i.order_id, i.order_item_id  FROM `t_order` o JOIN `t_order_item` i ON o.user_id = i.user_id AND o.order_id = i.order_id"
            + " WHERE o.`user_id` = %s AND o.`order_id` = %s ORDER BY i.order_item_id DESC";
        try (Connection connection = getShardingDataSource().getConnection()) {
            PreparedStatement pstmt = connection.prepareStatement(String.format(sql, 10, 1000));
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                int order_id = resultSet.getInt(1);
                int order_item_id = resultSet.getInt(2);
                System.out.println(order_id+","+order_item_id);
            }
            pstmt.close();
        }
    }
}
