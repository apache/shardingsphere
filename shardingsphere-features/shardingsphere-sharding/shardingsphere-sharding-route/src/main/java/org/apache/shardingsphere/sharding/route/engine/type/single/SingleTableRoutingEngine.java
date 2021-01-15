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

package org.apache.shardingsphere.sharding.route.engine.type.single;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Single table engine.
 */
@RequiredArgsConstructor
public final class SingleTableRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        if (sqlStatement instanceof CreateTableStatement) {
            routeContext.getRouteUnits().add(getRandomRouteUnit(shardingRule));
        } else {
            Collection<RouteUnit> routeUnits = getTargetRouteUnits(shardingRule);
            routeContext.getRouteUnits().addAll(routeUnits);
            if (1 < routeUnits.size()) {
                routeContext.setToCalcite(true);
            }
        }
    }
    
    private RouteUnit getRandomRouteUnit(final ShardingRule shardingRule) {
        Collection<String> dataSourceNames = shardingRule.getDataSourceNames();
        String dataSource = Lists.newArrayList(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
        String table = logicTables.iterator().next();
        return new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(table, table)));
    }
    
    private Collection<RouteUnit> getTargetRouteUnits(final ShardingRule shardingRule) {
        Map<RouteMapper, Collection<RouteMapper>> result = new LinkedHashMap<>();
        for (String each : logicTables) {
            if (shardingRule.getSingleTableRules().containsKey(each)) {
                fillRouteUnits(each, shardingRule.getSingleTableRules().get(each).getDataSourceName(), result);
            } else {
                throw new ShardingSphereException("`%s` single table does not exist.", each);
            }
        }
        return result.entrySet().stream().map(entry -> new RouteUnit(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
    
    private void fillRouteUnits(final String each, final String dataSource, final Map<RouteMapper, Collection<RouteMapper>> routeMappers) {
        RouteMapper dataSourceMapper = new RouteMapper(dataSource, dataSource);
        if (!routeMappers.containsKey(dataSourceMapper)) {
            routeMappers.put(dataSourceMapper, new LinkedHashSet<>());
        }
        routeMappers.get(dataSourceMapper).add(new RouteMapper(each, each));
    }
}
