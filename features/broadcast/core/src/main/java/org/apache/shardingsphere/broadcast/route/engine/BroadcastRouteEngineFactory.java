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
import org.apache.shardingsphere.broadcast.route.engine.type.broadcast.BroadcastTableBroadcastRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.ignore.BroadcastIgnoreRoutingEngine;
import org.apache.shardingsphere.broadcast.route.engine.type.unicast.BroadcastUnicastRoutingEngine;
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
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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
            return new BroadcastDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return sqlStatementContext instanceof CursorAvailable
                    ? getCursorRouteEngine(rule, sqlStatementContext, queryContext.getConnectionContext())
                    : getDDLRoutingEngine(rule, database, sqlStatementContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(rule, sqlStatementContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(rule, sqlStatementContext);
        }
        return getDMLRoutingEngine(rule, sqlStatementContext, queryContext.getConnectionContext());
    }
    
    private static BroadcastRouteEngine getCursorRouteEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return new BroadcastDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext instanceof TableAvailable) {
            Collection<String> tableNames = ((TableAvailable) sqlStatementContext)
                    .getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet());
            return rule.isAllBroadcastTables(tableNames) ? new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext) : new BroadcastIgnoreRoutingEngine();
        }
        return new BroadcastIgnoreRoutingEngine();
    }
    
    private static BroadcastRouteEngine getDDLRoutingEngine(final BroadcastRule rule, final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = SQLStatementContextExtractor.getTableNames(database, sqlStatementContext);
        return rule.isAllBroadcastTables(tableNames) ? new BroadcastTableBroadcastRoutingEngine(tableNames) : new BroadcastIgnoreRoutingEngine();
    }
    
    private static BroadcastRouteEngine getDALRoutingEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return new BroadcastIgnoreRoutingEngine();
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames() : Collections.emptyList();
        return tableNames.isEmpty() ? new BroadcastIgnoreRoutingEngine() : new BroadcastTableBroadcastRoutingEngine(rule.filterBroadcastTableNames(tableNames));
    }
    
    private static BroadcastRouteEngine getDCLRoutingEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames() : Collections.emptyList();
        Collection<String> broadcastTableNames = rule.filterBroadcastTableNames(tableNames);
        return broadcastTableNames.isEmpty() ? new BroadcastIgnoreRoutingEngine() : new BroadcastTableBroadcastRoutingEngine(broadcastTableNames);
    }
    
    private static BroadcastRouteEngine getDMLRoutingEngine(final BroadcastRule rule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames() : Collections.emptyList();
        if (rule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                    ? new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext)
                    : new BroadcastDatabaseBroadcastRoutingEngine();
        }
        return new BroadcastIgnoreRoutingEngine();
    }
}
