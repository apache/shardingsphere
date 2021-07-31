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

package org.apache.shardingsphere.agent.metrics.prometheus.hikari;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.shardingsphere.agent.metrics.prometheus.util.ReflectiveUtil;
import org.hamcrest.Matchers;
import org.junit.Test;

public final class HikariSimpleMetricsTest {
    
    @Test
    public void assertObserve() {
        HikariSimpleMetrics simpleMetrics = new HikariSimpleMetrics("pool-1");
        simpleMetrics.observe(org.apache.shardingsphere.agent.metrics.prometheus.hikari.HikariSimpleMetrics.MetricsType.CONNECTION_ACQUIRED_NANOS, 100);
        simpleMetrics.observe(org.apache.shardingsphere.agent.metrics.prometheus.hikari.HikariSimpleMetrics.MetricsType.CONNECTION_USAGE_MILLIS, 10);
        simpleMetrics.observe(org.apache.shardingsphere.agent.metrics.prometheus.hikari.HikariSimpleMetrics.MetricsType.CONNECTION_CREATED_MILLIS, 5);
        simpleMetrics.observe(org.apache.shardingsphere.agent.metrics.prometheus.hikari.HikariSimpleMetrics.MetricsType.CONNECTION_TIMEOUT_COUNT, 1);
        Histogram.Child elapsedAcquiredChild = (Histogram.Child) ReflectiveUtil.getFieldValue(simpleMetrics, "elapsedAcquiredChild");
        org.hamcrest.MatcherAssert.assertThat(elapsedAcquiredChild.get().sum, Matchers.is(100.0));
        Histogram.Child elapsedUsageChild = (Histogram.Child) ReflectiveUtil.getFieldValue(simpleMetrics, "elapsedUsageChild");
        org.hamcrest.MatcherAssert.assertThat(elapsedUsageChild.get().sum, Matchers.is(10.0));
        Histogram.Child elapsedCreationChild = (Histogram.Child) ReflectiveUtil.getFieldValue(simpleMetrics, "elapsedCreationChild");
        org.hamcrest.MatcherAssert.assertThat(elapsedCreationChild.get().sum, Matchers.is(5.0));
        Counter.Child connectionTimeoutCounterChild = (Counter.Child) ReflectiveUtil.getFieldValue(simpleMetrics, "connectionTimeoutCounterChild");
        org.hamcrest.MatcherAssert.assertThat(connectionTimeoutCounterChild.get(), Matchers.is(1.0));
    }
} 
