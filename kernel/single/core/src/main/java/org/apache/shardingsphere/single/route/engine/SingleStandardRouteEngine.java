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
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateTableStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Single standard route engine.
 */
@RequiredArgsConstructor
public final class SingleStandardRouteEngine implements SingleRouteEngine {
    
    private final Collection<QualifiedTable> singleTableNames;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public void route(final RouteContext routeContext, final SingleRule singleRule) {
        if (routeContext.getRouteUnits().isEmpty() || sqlStatement instanceof SelectStatement) {
            route0(routeContext, singleRule);
        } else {
            RouteContext newRouteContext = new RouteContext();
            route0(newRouteContext, singleRule);
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
    
    private void route0(final RouteContext routeContext, final SingleRule rule) {
        if (sqlStatement instanceof CreateTableStatement) {
            QualifiedTable table = singleTableNames.iterator().next();
            Optional<DataNode> dataNodeOptional = rule.findTableDataNode(table.getSchemaName(), table.getTableName());
            boolean containsIfNotExists = CreateTableStatementHandler.ifNotExists((CreateTableStatement) sqlStatement);
            if (dataNodeOptional.isPresent() && containsIfNotExists) {
                String dataSourceName = dataNodeOptional.map(DataNode::getDataSourceName).orElse(null);
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singleton(new RouteMapper(table.getTableName(), table.getTableName()))));
            } else if (dataNodeOptional.isPresent()) {
                throw new TableExistsException(table.getTableName());
            } else {
                String dataSourceName = rule.assignNewDataSourceName();
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singleton(new RouteMapper(table.getTableName(), table.getTableName()))));
            }
        } else if (sqlStatement instanceof AlterTableStatement || sqlStatement instanceof DropTableStatement || rule.isAllTablesInSameDataSource(routeContext, singleTableNames)) {
            fillRouteContext(rule, routeContext, rule.getSingleTableNames(singleTableNames));
        }
    }
    
    private void fillRouteContext(final SingleRule singleRule, final RouteContext routeContext, final Collection<QualifiedTable> logicTables) {
        for (QualifiedTable each : logicTables) {
            String tableName = each.getTableName();
            Optional<DataNode> dataNode = singleRule.findTableDataNode(each.getSchemaName(), tableName);
            if (!dataNode.isPresent()) {
                throw new SingleTableNotFoundException(tableName);
            }
            String dataSource = dataNode.get().getDataSourceName();
            routeContext.putRouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper(tableName, tableName)));
        }
    }
}
