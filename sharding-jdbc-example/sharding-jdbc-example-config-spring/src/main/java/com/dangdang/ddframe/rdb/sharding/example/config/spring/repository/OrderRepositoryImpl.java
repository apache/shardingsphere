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

package com.dangdang.ddframe.rdb.sharding.example.config.spring.repository;

import com.dangdang.ddframe.rdb.sharding.spring.datasource.SpringShardingDataSource;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    
    @Resource
    private SpringShardingDataSource shardingDataSource;
    
    @Override
    public void insert() {
        String orderSql = "INSERT INTO `t_order` (`order_id`, `user_id`, `status`) VALUES (?, ?, ?)";
        String orderItemSql = "INSERT INTO `t_order_item` (`order_item_id`, `order_id`, `user_id`, `status`) VALUES (?, ?, ?, ?)";
        for (int orderId = 1; orderId <= 4; orderId++) {
            for (int userId = 1; userId <= 2; userId++) {
                try (Connection connection = shardingDataSource.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(orderSql);
                    preparedStatement.setInt(1, orderId);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setString(3, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                
                    preparedStatement = connection.prepareStatement(orderItemSql);
                    int orderItemId = orderId + 4;
                    preparedStatement.setInt(1, orderItemId);
                    preparedStatement.setInt(2, orderId);
                    preparedStatement.setInt(3, userId);
                    preparedStatement.setString(4, "insert");
                    preparedStatement.execute();
                    preparedStatement.close();
                // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                // CHECKSTYLE:ON
                    ex.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public void delete() {
        String orderSql = "DELETE FROM `t_order`";
        String orderItemSql = "DELETE FROM `t_order_item`";
        try (Connection connection = shardingDataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(orderSql);
            preparedStatement.execute();
            preparedStatement.close();
            preparedStatement = connection.prepareStatement(orderItemSql);
            preparedStatement.execute();
            preparedStatement.close();
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            ex.printStackTrace();
        }
    }
    
    @Override
    public void select() {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (
            Connection conn = shardingDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 1);
            preparedStatement.setInt(2, 2);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println("orderItemId:" + rs.getInt(1) + ",orderId:" + rs.getInt(2) 
                        + ",userId:" + rs.getInt(3) + ",status:" + rs.getString(4));
                }
            }
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            ex.printStackTrace();
        }
    }
}
