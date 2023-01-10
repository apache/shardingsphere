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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapperFactory;
import org.junit.After;
import org.junit.BeforeClass;

public abstract class MetricsAdviceBaseTest {
    
    @BeforeClass
    public static void setup() {
        MetricsPool.setMetricsFactory(new FixtureWrapperFactory());
    }
    
    @After
    public void reset() {
        MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_EXECUTE_ERRORS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_CURRENT_CONNECTIONS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_REQUESTS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
    }
}
