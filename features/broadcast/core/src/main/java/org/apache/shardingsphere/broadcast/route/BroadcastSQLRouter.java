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
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.DecorateSQLRouter;
import org.apache.shardingsphere.infra.route.EntranceSQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLSetResourceGroupStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Broadcast SQL router.
 */
@HighFrequencyInvocation
public final class BroadcastSQLRouter implements EntranceSQLRouter<BroadcastRule>, DecorateSQLRouter<BroadcastRule> {
    
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
        if (sqlStatementContext instanceof IndexAvailable && !routeContext.getRouteUnits().isEmpty()) {
            putAllBroadcastTables(routeContext, broadcastRule, sqlStatementContext);
        }
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            routeToAllDatabase(routeContext, broadcastRule);
            return;
        }
        // TODO BEGIN extract db route logic to common database router, eg: DCL in instance route @duanzhengqiang
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            routeToAllDatabaseInstance(routeContext, database, broadcastRule);
        }
        // TODO END extract db route logic to common database router, eg: DCL in instance route
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? getTableNames((TableAvailable) sqlStatementContext) : Collections.emptyList();
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            routeToAllDatabaseInstance(routeContext, database, broadcastRule);
        }
    }
    
    private Collection<String> getTableNames(final TableAvailable sqlStatementContext) {
        Collection<SimpleTableSegment> tableSegments = sqlStatementContext.getTablesContext().getSimpleTables();
        Collection<String> result = new LinkedHashSet<>(tableSegments.size());
        for (SimpleTableSegment each : tableSegments) {
            result.add(each.getTableName().getIdentifier().getValue());
        }
        return result;
    }
    
    private void putAllBroadcastTables(final RouteContext routeContext, final BroadcastRule broadcastRule, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames() : Collections.emptyList();
        for (String each : broadcastRule.getBroadcastRuleTableNames(tableNames)) {
            for (RouteUnit routeUnit : routeContext.getRouteUnits()) {
                routeUnit.getTableMappers().add(new RouteMapper(each, each));
            }
        }
    }
    
    private boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        // TODO add dropResourceGroupStatement, alterResourceGroupStatement
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLSetResourceGroupStatement;
    }
    
    private boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getTablesContext().getSimpleTables().size()
                    && !"*".equals(tableSegmentsAvailable.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private void routeToAllDatabaseInstance(final RouteContext routeContext, final ShardingSphereDatabase database, final BroadcastRule broadcastRule) {
        routeContext.getRouteUnits().clear();
        for (String each : broadcastRule.getDataSourceNames()) {
            if (database.getResourceMetaData().getAllInstanceDataSourceNames().contains(each)) {
                routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
            }
        }
    }
    
    private void routeToAllDatabase(final RouteContext routeContext, final BroadcastRule broadcastRule) {
        routeContext.getRouteUnits().clear();
        for (String each : broadcastRule.getDataSourceNames()) {
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
