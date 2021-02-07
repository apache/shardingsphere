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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.agent.test.common.entity.OrderEntity;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.JDBCAgentTestUtils;
import org.apache.shardingsphere.integration.agent.test.common.util.OkHttpUtils;
import org.apache.shardingsphere.integration.agent.test.metrics.result.MetricResult;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@Slf4j
public final class MetricsPluginIT {
    
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
            Properties engineEnvProps = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
            try {
                Thread.sleep(Long.parseLong(engineEnvProps.getProperty("prometheus.waitMs", "60000")));
            } catch (final InterruptedException ignore) {
            }
            String url = engineEnvProps.getProperty("prometheus.url");
            Collection<String> metricsNames = buildMetricsNames();
            for (String each : metricsNames) {
                String metricURL = buildMetricURL(url, each);
                try {
                    MetricResult metricResult = OkHttpUtils.getInstance().get(metricURL, MetricResult.class);
                    assertResult(metricResult, each);
                } catch (final IOException ex) {
                    log.info("http get prometheus is error :", ex);
                }
            }
        }
    }
    
    private void assertResult(final MetricResult metricResult, final String metricsName) {
        assertThat(metricResult.getStatus(), is("success"));
        assertFalse(metricResult.getData().isEmpty());
        List<MetricResult.Metric> metricList = metricResult.getData().get(metricsName);
        assertFalse(metricList.isEmpty());
    }
    
    private Collection<String> buildMetricsNames() {
        Collection<String> result = new HashSet<>(11, 1);
        result.add(REQUEST_TOTAL);
        result.add(COLLECTION_TOTAL);
        result.add(EXECUTE_LATENCY);
        result.add(SELECT);
        result.add(UPDATE);
        result.add(DELETE);
        result.add(INSERT);
        result.add(ROUTE_DATASOURCE);
        result.add(ROUTE_TABLE);
        result.add(COMMIT);
        result.add(ROLLBACK);
        return result;
    }
    
    private String buildMetricURL(final String url, final String metricsName) {
        return String.join("", url, metricsName);
    }
}
