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

package org.apache.shardingsphere.integration.agent.test.zipkin;

import org.apache.shardingsphere.integration.agent.test.common.entity.OrderEntity;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.JDBCAgentTestUtils;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public final class ZipkinPluginIT {
    
    @Test
    @Ignore
    public void assertProxyWithAgent() {
        if (IntegrationTestEnvironment.getInstance().isEnvironmentPrepared()) {
            DataSource dataSource = IntegrationTestEnvironment.getInstance().getDataSource();
            List<Long> results = new ArrayList<>(10);
            for (int i = 1; i <= 10; i++) {
                OrderEntity orderEntity = new OrderEntity(i, i, "INSERT_TEST");
                JDBCAgentTestUtils.insertOrder(orderEntity, dataSource);
                results.add(orderEntity.getOrderId());
            }
            OrderEntity orderEntity = new OrderEntity(1000, 1000, "ROLL_BACK");
            JDBCAgentTestUtils.insertOrderRollback(orderEntity, dataSource);
            JDBCAgentTestUtils.updateOrderStatus(orderEntity, dataSource);
            JDBCAgentTestUtils.selectAllOrders(dataSource);
            for (Long each : results) {
                JDBCAgentTestUtils.deleteOrderByOrderId(each, dataSource);
            }
        }
    }
}
