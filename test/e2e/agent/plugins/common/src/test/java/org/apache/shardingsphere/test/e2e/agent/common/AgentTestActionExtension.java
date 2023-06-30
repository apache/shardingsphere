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

package org.apache.shardingsphere.test.e2e.agent.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.entity.OrderEntity;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.JDBCAgentTestUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Agent test action extension.
 */
@Slf4j
public final class AgentTestActionExtension implements BeforeEachCallback {
    
    private static volatile boolean hasSleep;
    
    @Override
    public void beforeEach(final ExtensionContext context) {
        checkEnvironment();
        if (E2ETestEnvironment.getInstance().isAdaptedProxy()) {
            requestProxy();
        }
        sleep();
    }
    
    private void checkEnvironment() {
        assumeTrue(E2ETestEnvironment.getInstance().isEnvironmentPrepared());
        E2ETestEnvironment.getInstance().prepareEnvironment();
        assumeTrue(E2ETestEnvironment.getInstance().isInitialized());
    }
    
    private void requestProxy() {
        DataSource dataSource = E2ETestEnvironment.getInstance().getDataSource();
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
        JDBCAgentTestUtils.createExecuteError(dataSource);
    }
    
    private void sleep() {
        if (!hasSleep) {
            log.info("Waiting to collect data ...");
            hasSleep = true;
            Awaitility.waitAtMost(Duration.ofMinutes(1)).await().pollDelay(getSleepMilliseconds(), TimeUnit.MILLISECONDS).until(() -> true);
        }
    }
    
    private Long getSleepMilliseconds() {
        return Long.valueOf(E2ETestEnvironment.getInstance().getProps().getProperty("collect.data.wait.milliseconds", "0"));
    }
}
