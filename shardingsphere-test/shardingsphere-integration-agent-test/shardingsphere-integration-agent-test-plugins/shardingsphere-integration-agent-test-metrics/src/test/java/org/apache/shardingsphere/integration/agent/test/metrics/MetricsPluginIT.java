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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.agent.test.common.entity.OrderEntity;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.JdbcUtils;
import org.apache.shardingsphere.integration.agent.test.common.util.OkHttpUtils;
import org.apache.shardingsphere.integration.agent.test.metrics.result.MetricsLabelResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public final class MetricsPluginIT {
    
    private static final String PROMETHEUS_URL = "http://127.0.0.1:19090/api/v1/labels";
    
    private static final String REQUEST_TOTAL = "proxy_request_total";
    
    private static final String COLLECTION_TOTAL = "proxy_connection_total";
    
    private static final String EXECUTE_LATENCY = "proxy_execute_latency_millis";
    
    private static final String SELECT = "sql_select_total";
    
    private static final String UPDATE = "sql_update_total";
    
    private static final String DELETE = "sql_delete_total";
    
    private static final String INSERT = "sql_insert_total";
    
    private static final String ROUTE_DATASOURCE = "route_datasource";
    
    private static final String ROUTE_TABLE = "route_table";
    
    private static final String COMMIT = "proxy_transaction_commit_total";
    
    private static final String ROLLBACK = "proxy_transaction_rollback_total";
    
    @Test
    public void assertProxyWithAgent() {
        if (IntegrationTestEnvironment.getInstance().isEnvironmentPrepared()) {
            DataSource dataSource = IntegrationTestEnvironment.getInstance().getDataSource();
            List<Long> results = new ArrayList<>(10);
            for (int i = 1; i <= 10; i++) {
                OrderEntity orderEntity = new OrderEntity(i, i, "INSERT_TEST");
                JdbcUtils.insertOrder(orderEntity, dataSource);
                results.add(orderEntity.getOrderId());
            }
            OrderEntity orderEntity = new OrderEntity(1000, 1000, "ROLL_BACK");
            JdbcUtils.insertOrderRollback(orderEntity, dataSource);
            Collection<OrderEntity> orderEntities = JdbcUtils.selectAllOrders(dataSource);
            assertThat(orderEntities.size(), is(10));
            for (Long each : results) {
                JdbcUtils.deleteOrderByOrderId(each, dataSource);
            }
            try {
                Thread.sleep(6000);
            } catch (final InterruptedException ignore) {
            }
            try {
                MetricsLabelResult metricsLabelResult = OkHttpUtils.getInstance().get(PROMETHEUS_URL, MetricsLabelResult.class);
                log.info("prometheus label result is: {}", metricsLabelResult);
                String[] labelNames = metricsLabelResult.getData();
                log.info("prometheus label names is: {}", Arrays.toString(labelNames));
                assertThat(metricsLabelResult.getStatus(), is("success"));
                assertTrue(labelNames.length > 0);
                assertFalse(isExist(labelNames, REQUEST_TOTAL));
                assertFalse(isExist(labelNames, COLLECTION_TOTAL));
                assertFalse(isExist(labelNames, EXECUTE_LATENCY));
                assertFalse(isExist(labelNames, SELECT));
                assertFalse(isExist(labelNames, UPDATE));
                assertFalse(isExist(labelNames, DELETE));
                assertFalse(isExist(labelNames, INSERT));
                assertFalse(isExist(labelNames, ROUTE_DATASOURCE));
                assertFalse(isExist(labelNames, ROUTE_TABLE));
                assertFalse(isExist(labelNames, COMMIT));
                assertFalse(isExist(labelNames, ROLLBACK));
            } catch (IOException e) {
                e.printStackTrace();
                log.info("http get prometheus is error :", e);
            }
        }
    }
    
    private static boolean isExist(final String[] labelNames, final String labelName) {
        return Arrays.asList(labelNames).contains(labelName);
    }
}
