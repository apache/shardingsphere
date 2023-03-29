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
import org.apache.shardingsphere.test.e2e.agent.common.AgentTestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.common.env.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.util.OkHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricMetadataAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricQueryAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.IntegrationTestCasesLoader;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryAssertion;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricTestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.stream.Stream;

@ExtendWith(AgentTestActionExtension.class)
@Slf4j
class MetricsPluginE2EIT {
    
    @ParameterizedTest
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertWithAgent(final MetricTestCase metricTestCase) {
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
            log.info("Access prometheus HTTP RESTFul API error: ", ex);
        }
    }
    
    private void assertQuery(final String queryURL, final MetricTestCase metricCase) {
        for (MetricQueryAssertion each : metricCase.getQueryAssertions()) {
            try {
                String queryURLWithParam = String.join("", queryURL, "?query=", URLEncoder.encode(each.getQuery(), "UTF-8"));
                MetricQueryAssert.assertIs(OkHttpUtils.getInstance().get(queryURLWithParam, MetricsQueryResult.class), each);
            } catch (final IOException ex) {
                log.info("Access prometheus HTTP RESTFul API error: ", ex);
            }
        }
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return IntegrationTestCasesLoader.getInstance().loadIntegrationTestCases(E2ETestEnvironment.getInstance().getAdapter()).stream().map(Arguments::of);
        }
    }
}
