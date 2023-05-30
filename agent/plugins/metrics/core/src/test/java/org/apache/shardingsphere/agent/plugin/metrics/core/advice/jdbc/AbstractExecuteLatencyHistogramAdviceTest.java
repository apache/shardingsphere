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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.jdbc;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.Mockito.mock;

class AbstractExecuteLatencyHistogramAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("jdbc_statement_execute_latency_millis", MetricCollectorType.HISTOGRAM, null);
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    void assertWithStatement() {
        StatementExecuteLatencyHistogramAdvice advice = new StatementExecuteLatencyHistogramAdvice();
        TargetAdviceObjectFixture targetObject = new TargetAdviceObjectFixture();
        Method method = mock(Method.class);
        advice.beforeMethod(targetObject, method, new Object[]{}, "FIXTURE");
        Awaitility.await().pollDelay(200L, TimeUnit.MILLISECONDS).until(() -> true);
        advice.afterMethod(targetObject, method, new Object[]{}, null, "FIXTURE");
        assertThat(Double.parseDouble(MetricsCollectorRegistry.get(config, "FIXTURE").toString()), greaterThanOrEqualTo(200D));
    }
    
    @Test
    void assertWithPreparedStatement() {
        PreparedStatementExecuteLatencyHistogramAdvice advice = new PreparedStatementExecuteLatencyHistogramAdvice();
        TargetAdviceObjectFixture targetObject = new TargetAdviceObjectFixture();
        Method method = mock(Method.class);
        advice.beforeMethod(targetObject, method, new Object[]{}, "FIXTURE");
        Awaitility.await().pollDelay(200L, TimeUnit.MILLISECONDS).until(() -> true);
        advice.afterMethod(targetObject, method, new Object[]{}, null, "FIXTURE");
        assertThat(Double.parseDouble(MetricsCollectorRegistry.get(config, "FIXTURE").toString()), greaterThanOrEqualTo(200D));
    }
}
