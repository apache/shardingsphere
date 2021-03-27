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

package org.apache.shardingsphere.infra.executor.exec;

import com.google.common.collect.Lists;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.rel2sql.SqlImplementor.Result;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.executor.exec.sql.ShardingSqlImplementor;
import org.apache.shardingsphere.infra.executor.exec.tool.MetaDataConverter;
import org.apache.shardingsphere.infra.executor.exec.tool.SqlDialects;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.apache.shardingsphere.infra.optimize.sql.ExtractTableNameSqlShuttle;
import org.apache.shardingsphere.infra.optimize.sql.SqlDynamicValueParam;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <code>Executor</code> instance for relation operator <code>LogicalScan</code>.
 */
public final class ScanExecutorBuilder {

    
    /**
     * Build <code>Executor</code> instance for <code>LogicalScan</code>.
     * @param scan <code>LogicalScan</code> rational operator.
     * @param executorBuilder see {@link ExecutorBuilder}
     * @return <code>LogicalScanExecutor</code>
     */
    public static Executor build(final SSScan scan, final ExecutorBuilder executorBuilder) {
        ExecContext execContext = executorBuilder.getExecContext();
        RouteContext routeContext = scan.route(execContext.getShardingRule());
        RelNode relNode = scan.getPushdownRelNode();
    
        SqlDialect sqlDialect = SqlDialects.toSqlDialect(execContext.getDatabaseType());
        SqlNode sqlNode = convertRelNodeToSqlNode(sqlDialect, relNode);
        Collection<ExecutionUnit> executionUnits = generateExecutionUnit(sqlNode, sqlDialect, routeContext, execContext);
    
        QueryResultMetaData metaData = MetaDataConverter.buildMetaData(relNode);
        
        List<Executor> executors = executionUnits.stream().map(executionUnit -> 
                new JDBCQueryExecutor(executionUnit, routeContext, execContext, metaData)).collect(Collectors.toList());
    
        if (executors.size() > 1) {
            return new MultiExecutor(executors, execContext);
        }
        return executors.get(0);
    }
    
    private static SqlNode convertRelNodeToSqlNode(final SqlDialect sqlDialect, final RelNode relNode) {
        RelToSqlConverter relToSqlConverter = new ShardingSqlImplementor(sqlDialect);
        Result result = relToSqlConverter.visitRoot(relNode);
        return result.asStatement();
    }
    
    private static Collection<ExecutionUnit> generateExecutionUnit(final SqlNode sqlNode, final SqlDialect sqlDialect, 
                                                                   final RouteContext routeContext, final ExecContext execContext) {
        ExtractTableNameSqlShuttle extractTableNameSqlShuttle = new ExtractTableNameSqlShuttle();
        SqlNode sqlTemplate = sqlNode.accept(extractTableNameSqlShuttle);
        List<SqlDynamicValueParam<String>> tables = extractTableNameSqlShuttle.getTableNames();
        
        List<Object> parameters = execContext.getParameters();
        return routeContext.getRouteUnits().stream().map(routeUnit -> {
            // if all binding table 
            RouteMapper oneTableRout = routeUnit.getTableMappers().iterator().next();
            Map<String, String> routeTableMap = routeUnit.getTableMappers().stream().collect(Collectors.toMap(RouteMapper::getLogicName, RouteMapper::getActualName));
            tables.forEach(table -> {
                if (routeTableMap.containsKey(table.getOriginal())) {
                    table.setActual(routeTableMap.get(table.getOriginal()));
                } else {
                    RouteMapper dbRoute = routeUnit.getDataSourceMapper();
                    Map<String, String> bindingTableMap = execContext.getShardingRule().getLogicAndActualTablesFromBindingTable(dbRoute.getActualName(),
                            oneTableRout.getLogicName(), oneTableRout.getActualName(), Arrays.asList(oneTableRout.getLogicName(), table.getOriginal()));
                    table.setActual(bindingTableMap.get(table.getOriginal()));
                }
            });
            String sql = sqlTemplate.toSqlString(sqlDialect).getSql();
            SQLUnit sqlUnit = new SQLUnit(sql, parameters, Lists.newArrayList(routeUnit.getTableMappers()));
            return new ExecutionUnit(routeUnit.getDataSourceMapper().getActualName(), sqlUnit);
        }).collect(Collectors.toList());
    }
}
