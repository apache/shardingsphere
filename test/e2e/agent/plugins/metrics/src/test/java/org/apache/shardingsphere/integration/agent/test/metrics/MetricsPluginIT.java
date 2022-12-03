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
import org.apache.shardingsphere.integration.agent.test.common.BasePluginIT;
import org.apache.shardingsphere.integration.agent.test.common.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.agent.test.common.util.OkHttpUtils;
import org.apache.shardingsphere.integration.agent.test.metrics.result.MetricResult;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public final class MetricsPluginIT extends BasePluginIT {
    
    public static final String PROXY_REQUEST = "proxy_request_total";
    
    public static final String PROXY_COLLECTION = "proxy_connection_total";
    
    public static final String PROXY_EXECUTE_LATENCY_MILLIS = "proxy_execute_latency_millis";
    
    public static final String PROXY_EXECUTE_ERROR = "proxy_execute_error_total";
    
    public static final String SQL_SELECT = "sql_select_total";
    
    public static final String SQL_UPDATE = "sql_update_total";
    
    public static final String SQL_DELETE = "sql_delete_total";
    
    public static final String SQL_INSERT = "sql_insert_total";
    
    public static final String ROUTE_DATASOURCE = "route_datasource_total";
    
    public static final String ROUTE_TABLE = "route_table_total";
    
    public static final String TRANSACTION_COMMIT = "proxy_transaction_commit_total";
    
    public static final String TRANSACTION_ROLLBACK = "proxy_transaction_rollback_total";
    
    @Test
    public void assertProxyWithAgent() throws IOException {
        super.assertProxyWithAgent();
        Properties props = IntegrationTestEnvironment.getInstance().getProps();
        try {
            Thread.sleep(Long.parseLong(props.getProperty("prometheus.waitMs", "60000")));
        } catch (final InterruptedException ignore) {
        }
        String url = props.getProperty("prometheus.url");
        Collection<String> metricsNames = buildMetricsNames();
        for (String each : metricsNames) {
            String metricURL = buildMetricURL(url, each);
            try {
                assertResult(OkHttpUtils.getInstance().get(metricURL, MetricResult.class), each);
            } catch (final IOException ex) {
                log.info("http get prometheus is error :", ex);
            }
        }
    }
    
    private void assertResult(final MetricResult metricResult, final String metricsName) {
        assertThat(metricResult.getStatus(), is("success"));
        assertNotNull(metricResult.getData());
    }
    
    private Collection<String> buildMetricsNames() {
        Collection<String> result = new HashSet<>();
        result.add(PROXY_REQUEST);
        result.add(PROXY_COLLECTION);
        result.add(PROXY_EXECUTE_LATENCY_MILLIS);
        result.add(SQL_SELECT);
        result.add(SQL_UPDATE);
        result.add(SQL_DELETE);
        result.add(SQL_INSERT);
        result.add(ROUTE_DATASOURCE);
        result.add(ROUTE_TABLE);
        result.add(TRANSACTION_COMMIT);
        result.add(TRANSACTION_ROLLBACK);
        result.add(PROXY_EXECUTE_ERROR);
        return result;
    }
    
    private String buildMetricURL(final String url, final String metricsName) {
        return String.join("", url, metricsName);
    }
}
