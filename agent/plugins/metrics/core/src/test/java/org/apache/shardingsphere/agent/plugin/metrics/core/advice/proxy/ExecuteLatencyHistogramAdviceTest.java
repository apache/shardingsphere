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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.proxy;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryPacketExecutor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class ExecuteLatencyHistogramAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_execute_latency_millis", MetricCollectorType.HISTOGRAM, null, Collections.emptyList(), Collections.emptyMap());
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    void assertExecuteLatencyHistogramWhenQueryCommandExecutor() {
        ExecuteLatencyHistogramAdvice advice = new ExecuteLatencyHistogramAdvice();
        TargetAdviceObjectFixture targetObject = new TargetAdviceObjectFixture();
        TargetAdviceMethod method = mock(TargetAdviceMethod.class);
        Object[] args = new Object[]{null, null, mock(MySQLComQueryPacketExecutor.class)};
        advice.beforeMethod(targetObject, method, args, "FIXTURE");
        Awaitility.await().pollDelay(500L, TimeUnit.MILLISECONDS).until(() -> true);
        advice.afterMethod(targetObject, method, args, null, "FIXTURE");
        assertThat(Double.parseDouble(MetricsCollectorRegistry.get(config, "FIXTURE").toString()), greaterThanOrEqualTo(500D));
    }
    
    @Test
    void assertExecuteLatencyHistogramWhenNotQueryCommandExecutor() {
        ExecuteLatencyHistogramAdvice advice = new ExecuteLatencyHistogramAdvice();
        TargetAdviceObjectFixture targetObject = new TargetAdviceObjectFixture();
        TargetAdviceMethod method = mock(TargetAdviceMethod.class);
        Object[] args = new Object[]{null, null, mock(MySQLComQuitExecutor.class)};
        advice.beforeMethod(targetObject, method, args, "FIXTURE");
        Awaitility.await().pollDelay(20L, TimeUnit.MILLISECONDS).until(() -> true);
        advice.afterMethod(targetObject, method, args, null, "FIXTURE");
        assertThat(Double.parseDouble(MetricsCollectorRegistry.get(config, "FIXTURE").toString()), is(0D));
    }
}
