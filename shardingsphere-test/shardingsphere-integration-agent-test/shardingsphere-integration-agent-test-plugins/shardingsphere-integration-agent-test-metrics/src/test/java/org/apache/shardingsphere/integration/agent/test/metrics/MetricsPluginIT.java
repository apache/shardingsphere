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

package org.apache.shardingsphere.integration.agent.test.metrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.sql.DataSource;
import org.apache.shardingsphere.integration.agent.test.metrics.entity.OrderEntity;
import org.apache.shardingsphere.integration.agent.test.metrics.env.IntegrationTestEnvironment;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MetricsPluginIT {
    
    @Test
    public void assertProxyWithAgent() {
        if (IntegrationTestEnvironment.getInstance().isEnvironmentPrepared()) {
            DataSource dataSource = IntegrationTestEnvironment.getInstance().getDataSource();
            List<Long> results = new ArrayList<>(10);
            for (int i = 1; i <= 10; i++) {
                OrderEntity orderEntity = new OrderEntity(i, i, "INSERT_TEST");
                insert(orderEntity, dataSource);
                results.add(orderEntity.getOrderId());
            }
            Collection<OrderEntity> orderEntities = selectAll(dataSource);
            assertThat(orderEntities.size(), is(10));
            for (Long each : results) {
                delete(each, dataSource);
            }
        }
    }
    
    private void insert(final OrderEntity orderEntity, final DataSource dataSource) {
        String sql = "INSERT INTO t_order (order_id,user_id, status) VALUES (?, ?,?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(1, orderEntity.getOrderId());
            preparedStatement.setInt(2, orderEntity.getUserId());
            preparedStatement.setString(3, orderEntity.getStatus());
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
    
    private void delete(final Long orderId, final DataSource dataSource) {
        String sql = "DELETE FROM t_order WHERE order_id=?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, orderId);
            preparedStatement.executeUpdate();
        } catch (final SQLException ignored) {
        }
    }
    
    private Collection<OrderEntity> selectAll(final DataSource dataSource) {
        String sql = "SELECT * FROM t_order";
        return getOrders(sql, dataSource);
    }
    
    private Collection<OrderEntity> getOrders(final String sql, final DataSource dataSource) {
        Collection<OrderEntity> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                OrderEntity orderEntity = new OrderEntity(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3));
                result.add(orderEntity);
            }
        } catch (final SQLException ignored) {
        }
        return result;
    }
}
