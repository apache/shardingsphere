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
        MetricsPool.get(MetricIds.PARSED_INSERT_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_UPDATE_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_DELETE_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_SELECT_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_DDL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_DCL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_DAL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_TCL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_RQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_RDL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PARSED_RAL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_INSERT_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_UPDATE_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_DELETE_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_SELECT_SQL).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_DATA_SOURCES).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.ROUTED_TABLES).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_EXECUTE_ERRORS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_CURRENT_CONNECTIONS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_REQUESTS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_COMMIT_TRANSACTIONS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
        MetricsPool.get(MetricIds.PROXY_ROLLBACK_TRANSACTIONS).ifPresent(optional -> ((FixtureWrapper) optional).reset());
    }
}
