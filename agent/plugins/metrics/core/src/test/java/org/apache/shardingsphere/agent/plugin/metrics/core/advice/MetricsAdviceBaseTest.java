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
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_INSERT_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_UPDATE_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_DELETE_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_SELECT_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_DDL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_DCL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_DAL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_TCL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_RQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_RDL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PARSED_RAL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_INSERT_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_UPDATE_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_DELETE_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_SELECT_SQL)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_DATA_SOURCES)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.ROUTED_TABLES)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_ERRORS)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_CURRENT_CONNECTIONS)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_REQUESTS)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_COMMIT_TRANSACTIONS)).reset();
        ((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_ROLLBACK_TRANSACTIONS)).reset();
    }
}
