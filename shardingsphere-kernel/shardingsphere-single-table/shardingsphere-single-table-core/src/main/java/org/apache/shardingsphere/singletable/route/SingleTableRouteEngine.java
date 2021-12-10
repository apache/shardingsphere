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

package org.apache.shardingsphere.singletable.route;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Single table route engine.
 */
@RequiredArgsConstructor
public final class SingleTableRouteEngine {
    
    private final Collection<String> singleTableNames;
    
    private final SQLStatement sqlStatement;
    
    /**
     * Route for single table.
     *
     * @param routeContext route context
     * @param rule single table rule
     */
    public void route(final RouteContext routeContext, final SingleTableRule rule) {
        if (routeContext.getRouteUnits().isEmpty() || sqlStatement instanceof SelectStatement) {
            route0(routeContext, rule);
        } else {
            RouteContext newRouteContext = new RouteContext();
            route0(newRouteContext, rule);
            combineRouteContext(routeContext, newRouteContext);
        }
    }
    
    private void combineRouteContext(final RouteContext routeContext, final RouteContext newRouteContext) {
        Map<String, RouteUnit> dataSourceRouteUnits = getDataSourceRouteUnits(newRouteContext);
        routeContext.getRouteUnits().removeIf(each -> !dataSourceRouteUnits.containsKey(each.getDataSourceMapper().getLogicName()));
        for (Entry<String, RouteUnit> entry : dataSourceRouteUnits.entrySet()) {
            routeContext.putRouteUnit(entry.getValue().getDataSourceMapper(), entry.getValue().getTableMappers());
        }
    }
    
    private Map<String, RouteUnit> getDataSourceRouteUnits(final RouteContext newRouteContext) {
        return newRouteContext.getRouteUnits().stream().collect(Collectors.toMap(each -> each.getDataSourceMapper().getLogicName(), Function.identity(), (oldValue, currentValue) -> oldValue));
    }
    
    private void route0(final RouteContext routeContext, final SingleTableRule rule) {
        if (isDDLTableStatement() || rule.isAllTablesInSameDataSource(routeContext, singleTableNames)) {
            Collection<String> existSingleTables = rule.getSingleTableNames(singleTableNames);
            if (!existSingleTables.isEmpty()) {
                fillRouteContext(rule, routeContext, existSingleTables);
            } else {
                RouteUnit routeUnit = rule.getDefaultDataSource().isPresent() ? getDefaultRouteUnit(rule.getDefaultDataSource().get()) : getRandomRouteUnit(rule);
                routeContext.getRouteUnits().add(routeUnit);
            }
        } else {
            decorateRouteContextForFederate(routeContext);
            fillRouteContext(rule, routeContext, singleTableNames);
        }
    }
    
    private void decorateRouteContextForFederate(final RouteContext routeContext) {
        RouteContext newRouteContext = new RouteContext();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            newRouteContext.putRouteUnit(each.getDataSourceMapper(), each.getTableMappers());
        }
        routeContext.setFederated(true);
        routeContext.getRouteUnits().clear();
        routeContext.getOriginalDataNodes().clear();
        routeContext.getRouteUnits().addAll(newRouteContext.getRouteUnits());
        routeContext.getOriginalDataNodes().addAll(newRouteContext.getOriginalDataNodes());
    }
    
    private boolean isDDLTableStatement() {
        return sqlStatement instanceof CreateTableStatement || sqlStatement instanceof AlterTableStatement || sqlStatement instanceof DropTableStatement;
    }
    
    private RouteUnit getRandomRouteUnit(final SingleTableRule singleTableRule) {
        Collection<String> dataSourceNames = singleTableRule.getDataSourceNames();
        String dataSource = new ArrayList<>(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
        String table = singleTableNames.iterator().next();
        return new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singleton(new RouteMapper(table, table)));
    }
    
    private RouteUnit getDefaultRouteUnit(final String dataSource) {
        String table = singleTableNames.iterator().next();
        return new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singleton(new RouteMapper(table, table)));
    }
    
    private void fillRouteContext(final SingleTableRule singleTableRule, final RouteContext routeContext, final Collection<String> logicTables) {
        Map<String, Collection<DataNode>> singleTableDataNodes = singleTableRule.getSingleTableDataNodes();
        for (String each : logicTables) {
            Collection<DataNode> dataNodes = singleTableDataNodes.get(each);
            if (null == dataNodes || dataNodes.isEmpty()) {
                throw new ShardingSphereException("`%s` single table does not exist.", each);
            }
            String dataSource = dataNodes.iterator().next().getDataSourceName();
            routeContext.putRouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(each, each)));
        }
    }
}
