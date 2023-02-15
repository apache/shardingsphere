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

package org.apache.shardingsphere.test.e2e.agent.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.BasePluginE2EIT;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetadataAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.QueryAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricMetadataCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricMetadataCasesPool;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryCasePool;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class MetricsPluginE2EIT extends BasePluginE2EIT {
    
    @Test
    public void assertProxyWithAgent() throws IOException {
        super.assertProxyWithAgent();
        Properties props = E2ETestEnvironment.getInstance().getProps();
        String metaDataURL = props.getProperty("prometheus.metadata.url");
        String queryURL = props.getProperty("prometheus.query.url");
        long collectSeconds = 35;
        try {
            log.info("Wait for prometheus to collect data ...");
            TimeUnit.SECONDS.sleep(collectSeconds);
            log.info("Start to assert ...");
        } catch (final InterruptedException ignored) {
        }
        for (MetricMetadataCase each : MetricMetadataCasesPool.getMetricCases()) {
            assertMetadata(metaDataURL, each);
        }
        for (MetricQueryCase each : MetricQueryCasePool.getMetricQueryCases()) {
            assertQuery(queryURL, each);
        }
    }
    
    private void assertMetadata(final String metaDataURL, final MetricMetadataCase metricCase) {
        String metricName = "counter".equalsIgnoreCase(metricCase.getMetricType()) && metricCase.getMetricName().endsWith("_total")
                ? metricCase.getMetricName().replace("_total", "")
                : metricCase.getMetricName();
        try {
            MetricsMetaDataResult metricsMetaDataResult = OkHttpUtils.getInstance().get(String.format("%s?metric=%s", metaDataURL, metricName), MetricsMetaDataResult.class);
            MetadataAssert.assertIs(metricsMetaDataResult, metricCase);
        } catch (final IOException ex) {
            log.info("Access prometheus HTTP RESTful API error: ", ex);
        }
    }
    
    private void assertQuery(final String queryRangeURL, final MetricQueryCase metricCase) {
        try {
            MetricsQueryResult metricsQueryResult = OkHttpUtils.getInstance().get(String.format("%s?query=%s", queryRangeURL, metricCase.getQuery()), MetricsQueryResult.class);
            QueryAssert.assertIs(metricsQueryResult, metricCase);
        } catch (final IOException ex) {
            log.info("Access prometheus HTTP RESTful API error: ", ex);
        }
    }
}
