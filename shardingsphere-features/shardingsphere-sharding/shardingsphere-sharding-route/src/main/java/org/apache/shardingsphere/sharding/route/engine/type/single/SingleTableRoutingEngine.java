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
import java.util.LinkedHashSet;
import java.util.concurrent.ThreadLocalRandom;

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
            if (isInSameDataSource(routeUnits)) {
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
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (String each : logicTables) {
            if (shardingRule.getSingleTableRules().containsKey(each)) {
                String dataSource = shardingRule.getSingleTableRules().get(each).getDataSourceName();
                result.add(new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(each, each))));
            } else {
                throw new ShardingSphereException("`%s` single table does not exist.", each);
            }
        }
        return result;
    }
    
    private boolean isInSameDataSource(final Collection<RouteUnit> routeUnits) {
        RouteUnit sample = routeUnits.iterator().next();
        for (RouteUnit each : routeUnits) {
            if (sample.getDataSourceMapper().equals(each.getDataSourceMapper())) {
                sample = each;
            } else {
                return false;
            }
        }
        return true;
    }
}
