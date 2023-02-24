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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.BasePluginE2EIT;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricMetadataAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricQueryAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryAssertion;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricTestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Properties;

@Slf4j
@RunWith(Parameterized.class)
public final class MetricsPluginE2EIT extends BasePluginE2EIT {
    
    private final MetricTestCase metricTestCase;
    
    public MetricsPluginE2EIT(final MetricTestCase metricTestCase) {
        this.metricTestCase = metricTestCase;
    }
    
    @Parameters
    public static Collection<MetricTestCase> getTestParameters() {
        return IntegrationTestCasesLoader.getInstance().loadIntegrationTestCases();
    }
    
    @Test
    @SneakyThrows(IOException.class)
    public void assertProxyWithAgent() {
        super.assertProxyWithAgent();
        Properties props = E2ETestEnvironment.getInstance().getProps();
        String metaDataURL = props.getProperty("prometheus.metadata.url");
        String queryURL = props.getProperty("prometheus.query.url");
        assertMetadata(metaDataURL, metricTestCase);
        assertQuery(queryURL, metricTestCase);
    }
    
    private void assertMetadata(final String metaDataURL, final MetricTestCase metricCase) {
        String metricName = "counter".equalsIgnoreCase(metricCase.getMetricType()) && metricCase.getMetricName().endsWith("_total")
                ? metricCase.getMetricName().replace("_total", "")
                : metricCase.getMetricName();
        try {
            String metaDataURLWithParam = String.join("", metaDataURL, "?metric=", URLEncoder.encode(metricName, "UTF-8"));
            MetricMetadataAssert.assertIs(OkHttpUtils.getInstance().get(metaDataURLWithParam, MetricsMetaDataResult.class), metricCase);
        } catch (final IOException ex) {
            log.info("Access prometheus HTTP RESTful API error: ", ex);
        }
    }
    
    private void assertQuery(final String queryURL, final MetricTestCase metricCase) {
        for (MetricQueryAssertion each : metricCase.getQueryAssertions()) {
            try {
                String queryURLWithParam = String.join("", queryURL, "?query=", URLEncoder.encode(each.getQuery(), "UTF-8"));
                MetricQueryAssert.assertIs(OkHttpUtils.getInstance().get(queryURLWithParam, MetricsQueryResult.class), each);
            } catch (final IOException ex) {
                log.info("Access prometheus HTTP RESTful API error: ", ex);
            }
        }
    }
}
