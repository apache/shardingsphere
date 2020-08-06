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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.control.panel.spi.engine.SingletonFacadeEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;

/**
 * Metrics utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
                SingletonFacadeEngine.buildMetrics()
                        .ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.SHARDING_DATASOURCE.getName(), dataSourceMapper.getActualName()));
                for (RouteMapper table : tableMappers) {
                    SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.SHARDING_TABLE.getName(), table.getActualName()));
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
        SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.TRANSACTION.getName(), labelValue));
    }
    
    /**
     * Buried sharding rule metrics.
     *
     * @param routeContext route context
     * @param rules rules
     */
    public static void buriedShardingRuleMetrics(final RouteContext routeContext, final Collection<ShardingSphereRule> rules) {
        routeContext.getRouteResult().getActualDataSourceNames().forEach(dataSourceName -> rules.forEach(each -> {
            if (each instanceof ShadowRule && ((ShadowRule) each).getShadowMappings().containsValue(dataSourceName)) {
                SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.SHADOW_HIT_TOTAL.getName()));
            }
        }));
    }
}

