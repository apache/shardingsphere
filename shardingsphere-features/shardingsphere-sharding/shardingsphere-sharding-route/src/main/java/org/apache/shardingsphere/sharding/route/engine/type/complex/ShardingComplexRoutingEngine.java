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

package org.apache.shardingsphere.sharding.route.engine.type.complex;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Sharding complex routing engine.
 */
@RequiredArgsConstructor
public final class ShardingComplexRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final ShardingConditions shardingConditions;
    
    private final ConfigurationProperties props;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        Collection<RouteContext> routeContexts = new ArrayList<>(logicTables.size());
        Collection<String> bindingTableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String each : logicTables) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent()) {
                if (!bindingTableNames.contains(each)) {
                    new ShardingStandardRoutingEngine(tableRule.get().getLogicTable(), shardingConditions, props).route(routeContext, shardingRule);
                    routeContexts.add(routeContext);
                }
                shardingRule.findBindingTableRule(each).ifPresent(bindingTableRule -> bindingTableNames.addAll(
                    bindingTableRule.getTableRules().stream().map(TableRule::getLogicTable).collect(Collectors.toList())));
            }
        }
        if (routeContexts.isEmpty()) {
            throw new ShardingSphereException("Cannot find table rule and default data source with logic tables: '%s'", logicTables);
        }
        if (1 != routeContexts.size()) {
            new ShardingCartesianRoutingEngine(routeContexts).route(routeContext, shardingRule);
        }
    }
}
