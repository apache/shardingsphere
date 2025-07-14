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

package org.apache.shardingsphere.sharding.route.engine.type.unicast;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sharding.exception.syntax.DataSourceIntersectionNotFoundException;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Sharding unicast route engine.
 */
@RequiredArgsConstructor
public final class ShardingUnicastRouteEngine implements ShardingRouteEngine {
    
    private final SQLStatement sqlStatement;
    
    private final Collection<String> logicTables;
    
    private final ConnectionContext connectionContext;
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        String dataSourceName = getDataSourceName(shardingRule.getDataSourceNames());
        RouteMapper dataSourceMapper = new RouteMapper(dataSourceName, dataSourceName);
        if (logicTables.isEmpty()) {
            result.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
        } else if (1 == logicTables.size()) {
            String logicTableName = logicTables.iterator().next();
            if (!shardingRule.findShardingTable(logicTableName).isPresent()) {
                result.getRouteUnits().add(new RouteUnit(dataSourceMapper, Collections.emptyList()));
                return result;
            }
            DataNode dataNode = shardingRule.getDataNode(logicTableName);
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataNode.getDataSourceName(), dataNode.getDataSourceName()),
                    Collections.singletonList(new RouteMapper(logicTableName, dataNode.getTableName()))));
        } else {
            routeWithMultipleTables(result, shardingRule);
        }
        return result;
    }
    
    private String getDataSourceName(final Collection<String> dataSourceNames) {
        return sqlStatement.getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent() || isViewStatementContext(sqlStatement)
                ? dataSourceNames.iterator().next()
                : getRandomDataSourceName(dataSourceNames);
    }
    
    private boolean isViewStatementContext(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateViewStatement || sqlStatement instanceof AlterViewStatement || sqlStatement instanceof DropViewStatement;
    }
    
    private void routeWithMultipleTables(final RouteContext routeContext, final ShardingRule shardingRule) {
        List<RouteMapper> tableMappers = new ArrayList<>(logicTables.size());
        Set<String> availableDataSourceNames = Collections.emptySet();
        boolean first = true;
        for (String each : logicTables) {
            ShardingTable shardingTable = shardingRule.getShardingTable(each);
            DataNode dataNode = shardingTable.getActualDataNodes().get(0);
            tableMappers.add(new RouteMapper(each, dataNode.getTableName()));
            Set<String> currentDataSourceNames = shardingTable.getActualDataNodes().stream().map(DataNode::getDataSourceName).collect(
                    Collectors.toCollection(() -> new LinkedHashSet<>(shardingTable.getActualDataSourceNames().size(), 1F)));
            if (first) {
                availableDataSourceNames = currentDataSourceNames;
                first = false;
            } else {
                availableDataSourceNames = Sets.intersection(availableDataSourceNames, currentDataSourceNames);
            }
        }
        if (availableDataSourceNames.isEmpty()) {
            throw new DataSourceIntersectionNotFoundException(logicTables);
        }
        String dataSourceName = getDataSourceName(availableDataSourceNames);
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), tableMappers));
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        Collection<String> usedDataSourceNames = connectionContext.getUsedDataSourceNames();
        List<String> availableDataSourceNames = new ArrayList<>(usedDataSourceNames.isEmpty() ? dataSourceNames : usedDataSourceNames);
        return availableDataSourceNames.get(ThreadLocalRandom.current().nextInt(availableDataSourceNames.size()));
    }
}
