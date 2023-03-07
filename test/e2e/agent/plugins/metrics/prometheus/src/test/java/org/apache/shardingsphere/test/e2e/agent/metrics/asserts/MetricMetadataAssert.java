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
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricTestCase;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsMetaDataResult.Metric;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Metric meta data assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricMetadataAssert {
    
    /**
     * Assert metric is correct with expected result.
     *
     * @param actual assert result
     * @param expected expected metric
     */
    public static void assertIs(final MetricsMetaDataResult actual, final MetricTestCase expected) {
        String metricName = "counter".equalsIgnoreCase(expected.getMetricType()) && expected.getMetricName().endsWith("_total")
                ? expected.getMetricName().replace("_total", "")
                : expected.getMetricName();
        assertThat(String.format("Metric `%s` status is not success, error is `%s`", expected.getMetricName(), actual.getError()), actual.getStatus(), is("success"));
        assertFalse(actual.getData().isEmpty(), String.format("Metric `%s` is empty.", expected.getMetricName()));
        Collection<Metric> metrics = actual.getData().get(metricName);
        assertFalse(metrics.isEmpty(), String.format("Metric `%s` is empty.", expected.getMetricName()));
        for (Metric each : metrics) {
            assertThat(String.format("Metric `%s` is not `%s` type", expected.getMetricName(), expected.getMetricType()), each.getType(), is(expected.getMetricType()));
        }
    }
}
