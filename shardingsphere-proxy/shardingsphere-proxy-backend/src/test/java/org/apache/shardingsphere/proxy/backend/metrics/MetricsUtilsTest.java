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

package org.apache.shardingsphere.proxy.backend.metrics;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsUtilsTest {
    
    @Test
    public void testBuriedShardingMetrics() {
        RouteMapper dataSourceMapper = new RouteMapper("ds", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", "t_order_0");
        RouteUnit unit = new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper));
        MetricsUtils.collectRouteUnitMetrics(Collections.singleton(unit));
    }
    
    @Test
    public void testBuriedTransactionMetric() {
        MetricsUtils.collectTransactionMetric("begin");
        MetricsUtils.collectTransactionMetric("commit");
        MetricsUtils.collectTransactionMetric("rollback");
    }
    
}

