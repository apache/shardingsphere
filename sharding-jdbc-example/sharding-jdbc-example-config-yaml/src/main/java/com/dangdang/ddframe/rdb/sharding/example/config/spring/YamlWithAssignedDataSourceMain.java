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

package com.dangdang.ddframe.rdb.sharding.example.config.spring;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.config.yaml.api.YamlShardingDataSource;

public final class YamlWithAssignedDataSourceMain {
    
    public static void main(final String[] args) throws Exception {
        YamlShardingDataSource dataSource =  new YamlShardingDataSource(
            new File(YamlWithAssignedDataSourceMain.class.getResource("/META-INF/withAssignedDataSource.yaml").getFile()));
        printSimpleSelect(dataSource);
        printJoinSelect(dataSource);
        printGroupBy(dataSource);
    }
    
    private static void printSimpleSelect(final DataSource dataSource) throws SQLException {
        String sql = "SELECT c.* FROM t_config c";
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                    System.out.println(rs.getString(2));
                    System.out.println(rs.getString(3));
                }
            }
        }
    }
    
    private static void printJoinSelect(final DataSource dataSource) throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 1001);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                    System.out.println(rs.getInt(2));
                    System.out.println(rs.getString(3));
                }
            }
        }
    }
    
    private static void printGroupBy(final DataSource dataSource) throws SQLException {
        String sql = "SELECT o.user_id, COUNT(*) FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id GROUP BY o.user_id";
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                System.out.println("user_id: " + rs.getInt(1) + ", count: " + rs.getInt(2));
            }
        }
    }
}
