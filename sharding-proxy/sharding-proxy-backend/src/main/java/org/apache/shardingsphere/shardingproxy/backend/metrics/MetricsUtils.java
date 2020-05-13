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

package org.apache.shardingsphere.shardingproxy.backend.metrics;

import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.facade.MetricsTrackerFacade;
import org.apache.shardingsphere.underlying.route.context.RouteMapper;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.util.Collection;

/**
 * Metrics utils.
 */
public final class MetricsUtils {
    
    /**
     * Buried sharding metrics.
     *
     * @param routeUnits route units
     */
    public static void buriedShardingMetrics(final Collection<RouteUnit> routeUnits) {
        if (!routeUnits.isEmpty()) {
            for (RouteUnit each : routeUnits) {
                Collection<RouteMapper> tableMappers = each.getTableMappers();
                RouteMapper dataSourceMapper = each.getDataSourceMapper();
                for (RouteMapper table : tableMappers) {
                    MetricsTrackerFacade.getInstance().counterInc(MetricsLabelEnum.SHARDING.getName(), dataSourceMapper.getActualName(), table.getActualName());
                }
            }
        }
    }
    
    /**
     * Buried transaction metric.
     *
     * @param labelValue label value
     */
    public static void buriedTransactionMetric(final String labelValue) {
        MetricsTrackerFacade.getInstance().counterInc(MetricsLabelEnum.TRANSACTION.getName(), labelValue);
    }
}

