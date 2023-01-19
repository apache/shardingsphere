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

import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.MetricsCollectorFixture;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class RouteResultCountAdviceTest {
    
    private final MetricConfiguration routedDataSourcesConfig = new MetricConfiguration("routed_data_sources_total",
            MetricCollectorType.COUNTER, null, Collections.singletonList("name"), Collections.emptyMap());
    
    private final MetricConfiguration routedTablesConfig = new MetricConfiguration("routed_tables_total",
            MetricCollectorType.COUNTER, null, Collections.singletonList("name"), Collections.emptyMap());
    
    @After
    public void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(routedDataSourcesConfig, "FIXTURE")).reset();
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(routedTablesConfig, "FIXTURE")).reset();
    }
    
    @Test
    public void assertCountRouteResult() {
        RouteContext routeContext = new RouteContext();
        RouteMapper dataSourceMapper = new RouteMapper("logic_db", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", "t_order_0");
        routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.singleton(tableMapper)));
        new RouteResultCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(Method.class), new Object[]{}, routeContext, "FIXTURE");
        MetricsCollectorFixture wrapper = MetricsCollectorRegistry.get(routedDataSourcesConfig, "FIXTURE");
        assertThat(wrapper.getValue(), is(1d));
        wrapper = MetricsCollectorRegistry.get(routedTablesConfig, "FIXTURE");
        assertThat(wrapper.getValue(), is(1d));
    }
}
