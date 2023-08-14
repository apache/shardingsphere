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

package org.apache.shardingsphere.broadcast.route;

import org.apache.shardingsphere.broadcast.constant.BroadcastOrder;
import org.apache.shardingsphere.broadcast.route.engine.BroadcastRouteEngineFactory;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Broadcast SQL router.
 */
public final class BroadcastSQLRouter implements SQLRouter<BroadcastRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database,
                                           final BroadcastRule rule, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        RouteContext result = new RouteContext();
        BroadcastRouteEngineFactory.newInstance(rule, database, queryContext, connectionContext).route(result, rule);
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database, final BroadcastRule broadcastRule,
                                     final ConfigurationProperties props, final ConnectionContext connectionContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            routeToAllDatabase(routeContext, broadcastRule);
        }
        if (sqlStatement instanceof DDLStatement) {
            decorateRouteContextWhenDDLStatement(routeContext, queryContext, database, broadcastRule);
        }
        if (sqlStatement instanceof DALStatement && isResourceGroupStatement(sqlStatement)) {
            routeToAllDatabaseInstance(routeContext, database, broadcastRule);
        }
        if (sqlStatement instanceof DCLStatement && !isDCLForSingleTable(queryContext.getSqlStatementContext())) {
            routeToAllDatabaseInstance(routeContext, database, broadcastRule);
        }
    }
    
    private void decorateRouteContextWhenDDLStatement(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database, final BroadcastRule broadcastRule) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        if (sqlStatementContext instanceof CursorAvailable) {
            if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
                routeToAllDatabase(routeContext, broadcastRule);
            }
            return;
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            routeToAllDatabase(routeContext, broadcastRule);
            return;
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            if (broadcastRule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())) {
                routeToAllDatabaseInstance(routeContext, database, broadcastRule);
            }
            return;
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            routeToAllDatabaseInstance(routeContext, database, broadcastRule);
        }
    }
    
    private static boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        // TODO add dropResourceGroupStatement, alterResourceGroupStatement
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLSetResourceGroupStatement;
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private void routeToAllDatabaseInstance(final RouteContext routeContext, final ShardingSphereDatabase database, final BroadcastRule broadcastRule) {
        routeContext.getRouteUnits().clear();
        for (String each : broadcastRule.getAvailableDataSourceNames()) {
            if (database.getResourceMetaData().getAllInstanceDataSourceNames().contains(each)) {
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
            }
        }
    }
    
    private void routeToAllDatabase(final RouteContext routeContext, final BroadcastRule broadcastRule) {
        routeContext.getRouteUnits().clear();
        for (String each : broadcastRule.getAvailableDataSourceNames()) {
            routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
        }
    }
    
    @Override
    public int getOrder() {
        return BroadcastOrder.ORDER;
    }
    
    @Override
    public Class<BroadcastRule> getTypeClass() {
        return BroadcastRule.class;
    }
}
