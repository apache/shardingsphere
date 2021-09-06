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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Single table SQL router.
 */
public final class SingleTableSQLRouter implements SQLRouter<SingleTableRule> {
    
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final SingleTableRule rule, final ConfigurationProperties props) {
        RouteContext result = new RouteContext();
        route(logicSQL, rule, result);
        return result;
    }
    
    private void route(final LogicSQL logicSQL, final SingleTableRule rule, final RouteContext result) {
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        Collection<String> singleTableNames = getSingleTableNames(sqlStatementContext, rule, result);
        if (!singleTableNames.isEmpty()) {
            validateSameDataSource(rule, sqlStatementContext, singleTableNames);
            new SingleTableRouteEngine(singleTableNames, sqlStatementContext.getSqlStatement()).route(result, rule);
        }
    }
    
    private Collection<String> getSingleTableNames(final SQLStatementContext<?> sqlStatementContext, final SingleTableRule rule, final RouteContext result) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        return result.getRouteUnits().isEmpty() && sqlStatementContext.getSqlStatement() instanceof CreateTableStatement ? tableNames : rule.getSingleTableNames(tableNames); 
    }
    
    private void validateSameDataSource(final SingleTableRule rule, final SQLStatementContext<?> sqlStatementContext, final Collection<String> singleTableNames) {
        if (!(sqlStatementContext instanceof SelectStatementContext || rule.isSingleTableInSameDataSource(singleTableNames))) {
            throw new ShardingSphereException("Single tables must be in the same datasource.");
        }
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData,
                                     final SingleTableRule rule, final ConfigurationProperties props) {
        route(logicSQL, rule, routeContext);
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
    
    @Override
    public Class<SingleTableRule> getTypeClass() {
        return SingleTableRule.class;
    }
}
