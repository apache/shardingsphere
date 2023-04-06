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

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class RouteResultCountAdviceTest {
    
    private final MetricConfiguration routedResultConfig = new MetricConfiguration("routed_result_total", MetricCollectorType.COUNTER, null, Arrays.asList("object", "name"));
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(routedResultConfig, "FIXTURE")).reset();
    }
    
    @Test
    void assertCountRouteResult() {
        RouteContext routeContext = new RouteContext();
        RouteMapper dataSourceMapper = new RouteMapper("logic_db", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", "t_order_0");
        routeContext.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.singleton(tableMapper)));
        new RouteResultCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(Method.class), new Object[]{}, routeContext, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(routedResultConfig, "FIXTURE").toString(), is("data_source.ds_0=1, table.t_order_0=1"));
    }
}
