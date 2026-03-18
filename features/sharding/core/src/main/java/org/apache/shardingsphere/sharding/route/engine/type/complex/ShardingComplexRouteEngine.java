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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingTableRuleNotFoundException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Sharding complex route engine.
 */
@RequiredArgsConstructor
public final class ShardingComplexRouteEngine implements ShardingRouteEngine {
    
    private final ShardingConditions shardingConditions;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final HintValueContext hintValueContext;
    
    private final ConfigurationProperties props;
    
    private final Collection<String> logicTables;
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        Collection<String> bindingTableNames = new CaseInsensitiveSet<>();
        Collection<RouteContext> routeContexts = new LinkedList<>();
        for (String each : logicTables) {
            Optional<ShardingTable> shardingTable = shardingRule.findShardingTable(each);
            if (shardingTable.isPresent()) {
                if (!bindingTableNames.contains(each)) {
                    routeContexts.add(new ShardingStandardRouteEngine(shardingTable.get().getLogicTable(), shardingConditions, sqlStatementContext, hintValueContext, props).route(shardingRule));
                }
                shardingRule.findBindingTableRule(each).ifPresent(optional -> bindingTableNames.addAll(optional.getShardingTables().keySet()));
            }
        }
        if (routeContexts.isEmpty()) {
            throw new ShardingTableRuleNotFoundException(logicTables);
        }
        RouteContext result = new RouteContext();
        if (1 == routeContexts.size()) {
            RouteContext newRouteContext = routeContexts.iterator().next();
            result.getOriginalDataNodes().addAll(newRouteContext.getOriginalDataNodes());
            result.getRouteUnits().addAll(newRouteContext.getRouteUnits());
        } else {
            RouteContext routeContext = new ShardingCartesianRouteEngine(routeContexts).route(shardingRule);
            result.getOriginalDataNodes().addAll(routeContext.getOriginalDataNodes());
            result.getRouteUnits().addAll(routeContext.getRouteUnits());
        }
        return result;
    }
}
