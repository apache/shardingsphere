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
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Single table engine.
 */
@RequiredArgsConstructor
public final class SingleTablesRoutingEngine implements ShardingRouteEngine {
    
    private final Collection<String> logicTables;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        if (isDDLTableStatement() || shardingRule.isAllTablesInSameDataSource(logicTables)) {
            Set<String> existSingleTables = Sets.intersection(shardingRule.getSingleTableRules().keySet(), Sets.newHashSet(logicTables));
            if (!existSingleTables.isEmpty()) {
                fillRouteContext(shardingRule, routeContext, existSingleTables);
            } else {
                routeContext.getRouteUnits().add(getRandomRouteUnit(shardingRule));
            }
        } else {
            fillRouteContext(shardingRule, routeContext, logicTables);
            if (1 < routeContext.getRouteUnits().size()) {
                routeContext.setFederated(true);
            }
        }
    }
    
    private boolean isDDLTableStatement() {
        return sqlStatement instanceof CreateTableStatement || sqlStatement instanceof AlterTableStatement || sqlStatement instanceof DropTableStatement;
    }
    
    private RouteUnit getRandomRouteUnit(final ShardingRule shardingRule) {
        Collection<String> dataSourceNames = shardingRule.getDataSourceNames();
        String dataSource = Lists.newArrayList(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
        String table = logicTables.iterator().next();
        return new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singleton(new RouteMapper(table, table)));
    }
    
    private void fillRouteContext(final ShardingRule shardingRule, final RouteContext routeContext, final Collection<String> logicTables) {
        for (String each : logicTables) {
            if (!shardingRule.getSingleTableRules().containsKey(each)) {
                throw new ShardingSphereException("`%s` single table does not exist.", each);
            }
            String dataSource = shardingRule.getSingleTableRules().get(each).getDataSourceName();
            routeContext.putRouteUnit(new RouteMapper(dataSource, dataSource), new RouteMapper(each, each));
        }
    }
}
