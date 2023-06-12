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

package org.apache.shardingsphere.broadcast.route.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.broadcast.route.engine.type.BroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastTableBroadcastRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.ignore.BroadcastIgnoreRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.unicast.BroadcastUnicastRoutingEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.LoadStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTablespaceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Broadcast routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastRouteEngineFactory {
    
    /**
     * Create new instance of broadcast routing engine.
     * 
     * @param broadcastRule broadcast rule
     * @param database database
     * @param queryContext query context
     * @param props props
     * @param connectionContext connection context
     * @param globalRuleMetaData global rule metadata
     * @return broadcast route engine
     */
    public static Optional<BroadcastRouteEngine> newInstance(final BroadcastRule broadcastRule, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                             final ConfigurationProperties props, final ConnectionContext connectionContext, final ShardingSphereRuleMetaData globalRuleMetaData) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return Optional.of(new BroadcastDatabaseBroadcastRoutingEngine());
        }
        if (sqlStatement instanceof DDLStatement) {
            if (sqlStatementContext instanceof CursorAvailable) {
                return getCursorRouteEngine(broadcastRule, sqlStatementContext, connectionContext);
            }
            return getDDLRoutingEngine(broadcastRule, database, sqlStatementContext, props, connectionContext, globalRuleMetaData);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(broadcastRule, database, sqlStatementContext, connectionContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(broadcastRule, database, sqlStatementContext);
        }
        return getDQLRoutingEngine(broadcastRule, sqlStatementContext, connectionContext);
    }
    
    private static Optional<BroadcastRouteEngine> getDDLRoutingEngine(final BroadcastRule broadcastRule, final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext,
                                                                      final ConfigurationProperties props, final ConnectionContext connectionContext,
                                                                      final ShardingSphereRuleMetaData globalRuleMetaData) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            return Optional.of(new BroadcastDatabaseBroadcastRoutingEngine());
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return Optional.of(new BroadcastInstanceBroadcastRoutingEngine(database.getResourceMetaData()));
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> broadcastRuleTableNames = broadcastRule.getBroadcastRuleTableNames(tableNames);
        
        boolean sqlFederationEnabled = null != globalRuleMetaData && globalRuleMetaData.getSingleRule(SQLFederationRule.class).getConfiguration().isSqlFederationEnabled();
        // TODO remove this logic when jdbc adapter can support executing create logic view
        if (sqlFederationEnabled && (sqlStatement instanceof CreateViewStatement || sqlStatement instanceof AlterViewStatement || sqlStatement instanceof DropViewStatement)) {
            return Optional.of(new BroadcastUnicastRoutingEngine(sqlStatementContext, broadcastRuleTableNames, connectionContext));
        }
        if (!tableNames.isEmpty() && broadcastRuleTableNames.isEmpty()) {
            return Optional.of(new BroadcastIgnoreRoutingEngine());
        }
        return Optional.of(new BroadcastTableBroadcastRoutingEngine(database, sqlStatementContext, broadcastRuleTableNames));
    }
    
    private static Optional<BroadcastRouteEngine> getCursorRouteEngine(final BroadcastRule broadcastRule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return Optional.of(new BroadcastDatabaseBroadcastRoutingEngine());
        }
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            return Optional.of(new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext));
        }
        return Optional.of(new BroadcastIgnoreRoutingEngine());
    }
    
    private static Optional<BroadcastRouteEngine> getDALRoutingEngine(final BroadcastRule broadcastRule, final ShardingSphereDatabase database,
                                                                      final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return Optional.of(new BroadcastIgnoreRoutingEngine());
        }
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof ResetParameterStatement
                || sqlStatement instanceof MySQLShowDatabasesStatement || sqlStatement instanceof LoadStatement) {
            return Optional.of(new BroadcastDatabaseBroadcastRoutingEngine());
        }
        if (isResourceGroupStatement(sqlStatement)) {
            return Optional.of(new BroadcastInstanceBroadcastRoutingEngine(database.getResourceMetaData()));
        }
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> broadcastRuleTableNames = broadcastRule.getBroadcastRuleTableNames(tableNames);
        if (!tableNames.isEmpty() && broadcastRuleTableNames.isEmpty()) {
            return Optional.of(new BroadcastIgnoreRoutingEngine());
        }
        if (sqlStatement instanceof MySQLOptimizeTableStatement) {
            return Optional.of(new BroadcastTableBroadcastRoutingEngine(database, sqlStatementContext, broadcastRuleTableNames));
        }
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return broadcastRuleTableNames.isEmpty() ? Optional.of(new BroadcastDatabaseBroadcastRoutingEngine())
                    : Optional.of(new BroadcastTableBroadcastRoutingEngine(database, sqlStatementContext, broadcastRuleTableNames));
        }
        if (!broadcastRuleTableNames.isEmpty()) {
            return Optional.of(new BroadcastUnicastRoutingEngine(sqlStatementContext, broadcastRuleTableNames, connectionContext));
        }
        return Optional.of(new BroadcastIgnoreRoutingEngine());
    }
    
    private static boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        // TODO add dropResourceGroupStatement, alterResourceGroupStatement
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLSetResourceGroupStatement;
    }
    
    private static Optional<BroadcastRouteEngine> getDCLRoutingEngine(final BroadcastRule broadcastRule, final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        if (isDCLForSingleTable(sqlStatementContext)) {
            Collection<String> broadcastRuleTableNames = broadcastRule.getBroadcastRuleTableNames(sqlStatementContext.getTablesContext().getTableNames());
            return broadcastRuleTableNames.isEmpty() ? Optional.of(new BroadcastIgnoreRoutingEngine())
                    : Optional.of(new BroadcastTableBroadcastRoutingEngine(database, sqlStatementContext, broadcastRuleTableNames));
        }
        return Optional.of(new BroadcastInstanceBroadcastRoutingEngine(database.getResourceMetaData()));
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private static Optional<BroadcastRouteEngine> getDQLRoutingEngine(final BroadcastRule broadcastRule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                    ? Optional.of(new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext))
                    : Optional.of(new BroadcastDatabaseBroadcastRoutingEngine());
        }
        return Optional.of(new BroadcastIgnoreRoutingEngine());
    }
}
