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
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastDatabaseBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastTableBroadcastRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.ignore.BroadcastIgnoreRouteEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.unicast.BroadcastUnicastRouteEngine;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.extractor.SQLStatementContextExtractor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.TCLStatement;

import java.util.Collection;

/**
 * Broadcast routing engine factory.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastRouteEngineFactory {
    
    /**
     * Create new instance of broadcast routing engine.
     *
     * @param rule broadcast rule
     * @param database database
     * @param queryContext query context
     * @return broadcast route engine
     */
    public static BroadcastRouteEngine newInstance(final BroadcastRule rule, final ShardingSphereDatabase database, final QueryContext queryContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return new BroadcastDatabaseBroadcastRouteEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return sqlStatementContext instanceof CursorAvailable
                    ? getCursorRouteEngine(rule, sqlStatementContext, queryContext.getConnectionContext())
                    : getDDLRouteEngine(rule, database, sqlStatementContext);
        }
        if (!(sqlStatementContext instanceof TableAvailable)) {
            return new BroadcastIgnoreRouteEngine();
        }
        Collection<String> tableNames = ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames();
        if (tableNames.isEmpty()) {
            return new BroadcastIgnoreRouteEngine();
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRouteEngine(rule, tableNames);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRouteEngine(rule, tableNames);
        }
        return getDMLRouteEngine(rule, sqlStatementContext, queryContext.getConnectionContext(), tableNames);
    }
    
    private static BroadcastRouteEngine getCursorRouteEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return new BroadcastDatabaseBroadcastRouteEngine();
        }
        if (sqlStatementContext instanceof TableAvailable) {
            Collection<String> tableNames = ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames();
            return rule.isAllBroadcastTables(tableNames) ? new BroadcastUnicastRouteEngine(sqlStatementContext, tableNames, connectionContext) : new BroadcastIgnoreRouteEngine();
        }
        return new BroadcastIgnoreRouteEngine();
    }
    
    private static BroadcastRouteEngine getDDLRouteEngine(final BroadcastRule rule, final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = SQLStatementContextExtractor.getTableNames(database, sqlStatementContext);
        return rule.isAllBroadcastTables(tableNames) ? new BroadcastTableBroadcastRouteEngine(tableNames) : new BroadcastIgnoreRouteEngine();
    }
    
    private static BroadcastRouteEngine getDALRouteEngine(final BroadcastRule rule, final Collection<String> tableNames) {
        return new BroadcastTableBroadcastRouteEngine(rule.filterBroadcastTableNames(tableNames));
    }
    
    private static BroadcastRouteEngine getDCLRouteEngine(final BroadcastRule rule, final Collection<String> tableNames) {
        Collection<String> broadcastTableNames = rule.filterBroadcastTableNames(tableNames);
        return broadcastTableNames.isEmpty() ? new BroadcastIgnoreRouteEngine() : new BroadcastTableBroadcastRouteEngine(broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDMLRouteEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext,
                                                          final ConnectionContext connectionContext, final Collection<String> tableNames) {
        if (rule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                    ? new BroadcastUnicastRouteEngine(sqlStatementContext, tableNames, connectionContext)
                    : new BroadcastDatabaseBroadcastRouteEngine();
        }
        return new BroadcastIgnoreRouteEngine();
    }
}
