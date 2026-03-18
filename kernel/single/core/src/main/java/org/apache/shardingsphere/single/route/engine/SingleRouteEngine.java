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

package org.apache.shardingsphere.single.route.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Single route engine.
 */
@RequiredArgsConstructor
public final class SingleRouteEngine {
    
    private final Collection<QualifiedTable> singleTables;
    
    private final SQLStatement sqlStatement;
    
    private final HintValueContext hintValueContext;
    
    /**
     * Route.
     *
     * @param routeContext route context
     * @param singleRule single rule
     * @return route context
     */
    public RouteContext route(final RouteContext routeContext, final SingleRule singleRule) {
        if (routeContext.getRouteUnits().isEmpty() || sqlStatement instanceof SelectStatement) {
            routeStatement(routeContext, singleRule);
        } else {
            RouteContext newRouteContext = new RouteContext();
            routeStatement(newRouteContext, singleRule);
            combineRouteContext(routeContext, newRouteContext);
        }
        return routeContext;
    }
    
    private void routeStatement(final RouteContext routeContext, final SingleRule rule) {
        if (sqlStatement instanceof DDLStatement) {
            routeDDLStatement(routeContext, rule);
        } else {
            boolean allTablesInSameComputeNode = rule.isAllTablesInSameComputeNode(getDataNodes(routeContext), singleTables);
            ShardingSpherePreconditions.checkState(allTablesInSameComputeNode, () -> new UnsupportedSQLOperationException("all tables must be in the same compute node"));
            fillRouteContext(rule, routeContext, singleTables);
        }
    }
    
    private Collection<DataNode> getDataNodes(final RouteContext routeContext) {
        Collection<DataNode> result = new LinkedList<>();
        for (Collection<DataNode> each : routeContext.getOriginalDataNodes()) {
            result.addAll(each);
        }
        return result;
    }
    
    private void routeDDLStatement(final RouteContext routeContext, final SingleRule rule) {
        if (sqlStatement instanceof CreateTableStatement) {
            QualifiedTable table = singleTables.iterator().next();
            Optional<DataNode> dataNode = rule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(table.getSchemaName(), table.getTableName());
            boolean containsIfNotExists = ((CreateTableStatement) sqlStatement).isIfNotExists();
            if (dataNode.isPresent()) {
                routeDDLStatementWithExistTable(routeContext, containsIfNotExists, dataNode.get(), table);
            } else {
                String dataSourceName = rule.assignNewDataSourceName();
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singleton(new RouteMapper(table.getTableName(), table.getTableName()))));
            }
        } else {
            fillRouteContext(rule, routeContext, singleTables);
        }
    }
    
    private void routeDDLStatementWithExistTable(final RouteContext routeContext, final boolean containsIfNotExists, final DataNode dataNode, final QualifiedTable table) {
        if (containsIfNotExists || hintValueContext.isSkipMetadataValidate()) {
            String dataSourceName = dataNode.getDataSourceName();
            routeContext.getRouteUnits()
                    .add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singleton(new RouteMapper(table.getTableName(), table.getTableName()))));
        } else {
            throw new TableExistsException(table.getTableName());
        }
    }
    
    private void fillRouteContext(final SingleRule singleRule, final RouteContext routeContext, final Collection<QualifiedTable> logicTables) {
        for (QualifiedTable each : logicTables) {
            String tableName = each.getTableName();
            DataNode dataNode = singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(each.getSchemaName(), tableName)
                    .orElseThrow(() -> new SingleTableNotFoundException(tableName));
            String dataSource = dataNode.getDataSourceName();
            routeContext.putRouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(tableName, tableName)));
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
        return newRouteContext.getRouteUnits().stream().collect(Collectors.toMap(each -> each.getDataSourceMapper().getLogicName(), Function.identity()));
    }
}
