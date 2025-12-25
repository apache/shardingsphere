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

package org.apache.shardingsphere.agent.plugin.metrics.core.collector;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MetricsCollectorRegistryTest {
    
    @AfterEach
    void reset() throws ReflectiveOperationException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(MetricsCollectorRegistry.class.getDeclaredField("COLLECTORS"), null)).clear();
    }
    
    @Test
    void assertGet() {
        MetricConfiguration metricConfig = new MetricConfiguration("fixture_metric", MetricCollectorType.COUNTER, "help");
        CounterMetricsCollector first = MetricsCollectorRegistry.get(metricConfig, "FIXTURE");
        first.inc();
        CounterMetricsCollector second = MetricsCollectorRegistry.get(metricConfig, "FIXTURE");
        assertThat(first, is(second));
        assertThat(second.toString(), is("1"));
    }
}
