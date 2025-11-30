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

package org.apache.shardingsphere.test.e2e.agent.metrics.asserts;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.agent.engine.util.AgentE2EHttpUtils;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.response.MetricsMetaDataResponse;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.response.MetricsMetaDataResponse.Metric;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.response.MetricsQueryResponse;
import org.apache.shardingsphere.test.e2e.agent.metrics.asserts.response.MetricsQueryResponse.QueryDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricE2ETestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryAssertion;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Metric assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricAssert {
    
    /**
     * Assert metric.
     *
     * @param prometheusURL prometheus URL
     * @param expected expected test case
     */
    public static void assertIs(final String prometheusURL, final MetricE2ETestCase expected) {
        assertMetaData(prometheusURL + "/api/v1/metadata", expected);
        expected.getQueryAssertions().forEach(each -> assertQueryData(prometheusURL + "/api/v1/query", each));
    }
    
    @SneakyThrows(IOException.class)
    private static void assertMetaData(final String metaDataURL, final MetricE2ETestCase expected) {
        String metricName = getMetricName(expected);
        String metaDataQueryURL = String.join("", metaDataURL, "?metric=", encode(metricName));
        MetricsMetaDataResponse actual = JsonUtils.fromJsonString(AgentE2EHttpUtils.query(metaDataQueryURL), MetricsMetaDataResponse.class);
        assertThat(String.format("Metric `%s` status is not success, error is `%s`", expected.getMetricName(), actual.getError()), actual.getStatus(), is("success"));
        assertFalse(actual.getData().isEmpty(), String.format("Metric `%s` is empty.", expected.getMetricName()));
        Collection<Metric> metrics = actual.getData().get(metricName);
        assertFalse(metrics.isEmpty(), String.format("Metric `%s` is empty.", expected.getMetricName()));
        for (Metric each : metrics) {
            assertThat(String.format("Metric `%s` is not `%s` type.", expected.getMetricName(), expected.getMetricType()), each.getType(), is(expected.getMetricType()));
        }
    }
    
    private static String getMetricName(final MetricE2ETestCase expected) {
        return "counter".equalsIgnoreCase(expected.getMetricType()) && expected.getMetricName().endsWith("_total") ? expected.getMetricName().replace("_total", "") : expected.getMetricName();
    }
    
    @SneakyThrows(UnsupportedEncodingException.class)
    private static String encode(final String value) {
        return URLEncoder.encode(value, "UTF-8");
    }
    
    @SneakyThrows(IOException.class)
    private static void assertQueryData(final String queryURL, final MetricQueryAssertion expected) {
        String queryURLWithParam = String.join("", queryURL, "?query=", encode(expected.getQuery()));
        MetricsQueryResponse actual = JsonUtils.fromJsonString(AgentE2EHttpUtils.query(queryURLWithParam), MetricsQueryResponse.class);
        assertThat(String.format("The query `%s` is not success, error message is `%s`", expected.getQuery(), actual.getError()), actual.getStatus(), is("success"));
        assertFalse(actual.getData().getResult().isEmpty(), String.format("The query `%s` is empty.", expected.getQuery()));
        actual.getData().getResult().forEach(each -> assertMetricData(each, expected));
    }
    
    private static void assertMetricData(final QueryDataResult actual, final MetricQueryAssertion expected) {
        assertThat(actual.getMetric().get("__name__"), is(expected.getMetric()));
        if (null != expected.getValue()) {
            assertThat(actual.getValue().size(), is(2));
            assertThat(String.format("The value of the `%s` is error", expected.getQuery()), Integer.valueOf(actual.getValue().get(1)), is(expected.getValue()));
        }
    }
}
