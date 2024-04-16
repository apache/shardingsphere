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

package org.apache.shardingsphere.test.e2e.agent.common.request;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.e2e.agent.common.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.common.util.JDBCAgentTestUtils;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Proxy request executor.
 */
@RequiredArgsConstructor
public final class ProxyRequestExecutor implements Runnable {
    
    private final Connection connection;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    
    /**
     * Start.
     */
    public void start() {
        executor.submit(this);
    }
    
    /**
     * Stop.
     */
    public void stop() {
        try {
            executor.shutdownNow();
        } catch (final SecurityException ignored) {
        }
    }
    
    @Override
    public void run() {
        while (true) {
            request();
            Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(5L)).pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
        }
    }
    
    private void request() {
        Collection<Long> results = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            OrderEntity orderEntity = new OrderEntity(i, i, "INSERT_TEST");
            JDBCAgentTestUtils.insertOrder(orderEntity, connection);
            results.add(orderEntity.getOrderId());
        }
        OrderEntity orderEntity = new OrderEntity(1000, 1000, "ROLL_BACK");
        JDBCAgentTestUtils.insertOrderRollback(orderEntity, connection);
        JDBCAgentTestUtils.updateOrderStatus(orderEntity, connection);
        JDBCAgentTestUtils.selectAllOrders(connection);
        for (Long each : results) {
            JDBCAgentTestUtils.deleteOrderByOrderId(each, connection);
        }
        JDBCAgentTestUtils.createExecuteError(connection);
    }
}
