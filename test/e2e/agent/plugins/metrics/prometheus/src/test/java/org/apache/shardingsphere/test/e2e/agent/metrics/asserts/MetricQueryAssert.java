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
import org.apache.shardingsphere.test.e2e.agent.metrics.cases.MetricQueryAssertion;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult;
import org.apache.shardingsphere.test.e2e.agent.metrics.result.MetricsQueryResult.QueryDataResult;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Metric query assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricQueryAssert {
    
    /**
     * Assert metric is correct with expected result.
     *
     * @param actual assert result
     * @param expected expected metric
     */
    public static void assertIs(final MetricsQueryResult actual, final MetricQueryAssertion expected) {
        assertThat(String.format("The query `%s` is not success, error message is `%s`", expected.getQuery(), actual.getError()), actual.getStatus(), is("success"));
        assertFalse(actual.getData().getResult().isEmpty(), String.format("The query `%s` is empty.", expected.getQuery()));
        Collection<QueryDataResult> results = actual.getData().getResult();
        for (QueryDataResult each : results) {
            assertMetricName(each, expected);
            if (expected.isShouldAssertValue()) {
                assertValue(each, expected);
            }
        }
    }
    
    private static void assertMetricName(final QueryDataResult actual, final MetricQueryAssertion expected) {
        assertThat(actual.getMetric().get("__name__"), is(expected.getMetric()));
    }
    
    private static void assertValue(final QueryDataResult actual, final MetricQueryAssertion expected) {
        assertThat(actual.getValue(), notNullValue());
        assertThat(actual.getValue().size(), is(2));
        assertThat(String.format("The value of the `%s` is error", expected.getQuery()), Integer.valueOf(actual.getValue().get(1)), is(expected.getValue()));
    }
}
