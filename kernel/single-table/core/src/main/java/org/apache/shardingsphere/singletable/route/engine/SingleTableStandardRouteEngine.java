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

package org.apache.shardingsphere.singletable.route.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Single table standard route engine.
 */
@RequiredArgsConstructor
public final class SingleTableStandardRouteEngine implements SingleTableRouteEngine {
    
    private final Collection<QualifiedTable> singleTableNames;
    
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
        return newRouteContext.getRouteUnits().stream().collect(Collectors.toMap(each -> each.getDataSourceMapper().getLogicName(), Function.identity()));
    }
    
    private void route0(final RouteContext routeContext, final SingleTableRule rule) {
        if (sqlStatement instanceof CreateTableStatement) {
            String dataSourceName = rule.assignNewDataSourceName();
            QualifiedTable table = singleTableNames.iterator().next();
            if (isTableExists(table, rule)) {
                throw new TableExistsException(table.getTableName());
            }
            routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singleton(new RouteMapper(table.getTableName(), table.getTableName()))));
        } else if (sqlStatement instanceof AlterTableStatement || sqlStatement instanceof DropTableStatement || rule.isAllTablesInSameDataSource(routeContext, singleTableNames)) {
            fillRouteContext(rule, routeContext, rule.getSingleTableNames(singleTableNames));
        }
    }
    
    private boolean isTableExists(final QualifiedTable table, final SingleTableRule rule) {
        return rule.findSingleTableDataNode(table.getSchemaName(), table.getTableName()).isPresent();
    }
    
    private void fillRouteContext(final SingleTableRule singleTableRule, final RouteContext routeContext, final Collection<QualifiedTable> logicTables) {
        for (QualifiedTable each : logicTables) {
            String tableName = each.getTableName();
            Optional<DataNode> dataNode = singleTableRule.findSingleTableDataNode(each.getSchemaName(), tableName);
            if (!dataNode.isPresent()) {
                throw new SingleTableNotFoundException(tableName);
            }
            String dataSource = dataNode.get().getDataSourceName();
            routeContext.putRouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(tableName, tableName)));
        }
    }
}
