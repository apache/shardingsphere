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
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.agent.common.env.AgentE2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.agent.common.framework.AgentE2ETestActionExtension;
import org.apache.shardingsphere.test.e2e.agent.common.framework.AgentE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.agent.common.util.HttpUtils;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricMetadataAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.MetricQueryAssert;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricE2ETestCases;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryAssertion;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricTestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.net.URLEncoder;

@ExtendWith(AgentE2ETestActionExtension.class)
@Slf4j
class MetricsPluginE2EIT {
    
    @EnabledIf("isEnabled")
    @ParameterizedTest
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertWithAgent(final MetricTestCase metricTestCase) {
        String metaDataURL = AgentE2ETestEnvironment.getInstance().getPrometheusHttpUrl() + "/api/v1/metadata";
        String queryURL = AgentE2ETestEnvironment.getInstance().getPrometheusHttpUrl() + "/api/v1/query";
        assertMetadata(metaDataURL, metricTestCase);
        assertQuery(queryURL, metricTestCase);
    }
    
    private void assertMetadata(final String metaDataURL, final MetricTestCase metricCase) {
        String metricName = "counter".equalsIgnoreCase(metricCase.getMetricType()) && metricCase.getMetricName().endsWith("_total")
                ? metricCase.getMetricName().replace("_total", "")
                : metricCase.getMetricName();
        try {
            String metaDataURLWithParam = String.join("", metaDataURL, "?metric=", URLEncoder.encode(metricName, "UTF-8"));
            MetricMetadataAssert.assertIs(JsonUtils.fromJsonString(HttpUtils.getInstance().get(metaDataURLWithParam), MetricsMetaDataResult.class), metricCase);
        } catch (final IOException ex) {
            log.info("Access prometheus HTTP RESTFul API error: ", ex);
        }
    }
    
    private void assertQuery(final String queryURL, final MetricTestCase metricCase) {
        for (MetricQueryAssertion each : metricCase.getQueryAssertions()) {
            try {
                String queryURLWithParam = String.join("", queryURL, "?query=", URLEncoder.encode(each.getQuery(), "UTF-8"));
                MetricQueryAssert.assertIs(JsonUtils.fromJsonString(HttpUtils.getInstance().get(queryURLWithParam), MetricsQueryResult.class), each);
            } catch (final IOException ex) {
                log.info("Access prometheus HTTP RESTFul API error: ", ex);
            }
        }
    }
    
    private static boolean isEnabled() {
        return AgentE2ETestEnvironment.getInstance().containsTestParameter();
    }
    
    private static final class TestCaseArgumentsProvider extends AgentE2ETestCaseArgumentsProvider {
        
        private TestCaseArgumentsProvider() {
            super(MetricE2ETestCases.class);
        }
    }
}
