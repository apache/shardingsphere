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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
     * @param connectionContext connection context
     * @return broadcast route engine
     */
    public static BroadcastRouteEngine newInstance(final BroadcastRule broadcastRule, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                   final ConnectionContext connectionContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return new BroadcastDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            if (sqlStatementContext instanceof CursorAvailable) {
                return getCursorRouteEngine(broadcastRule, sqlStatementContext, connectionContext);
            }
            return getDDLRoutingEngine(broadcastRule, database, queryContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(broadcastRule, queryContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(broadcastRule, queryContext);
        }
        return getDQLRoutingEngine(broadcastRule, queryContext, connectionContext);
    }
    
    private static BroadcastRouteEngine getCursorRouteEngine(final BroadcastRule broadcastRule, final SQLStatementContext sqlStatementContext, final ConnectionContext connectionContext) {
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return new BroadcastDatabaseBroadcastRoutingEngine();
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            return new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext);
        }
        return new BroadcastIgnoreRoutingEngine();
    }
    
    private static BroadcastRouteEngine getDDLRoutingEngine(final BroadcastRule broadcastRule, final ShardingSphereDatabase database, final QueryContext queryContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<String> tableNames = getTableNames(database, sqlStatementContext);
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            return new BroadcastTableBroadcastRoutingEngine(tableNames);
        }
        return new BroadcastIgnoreRoutingEngine();
    }
    
    private static Collection<String> getTableNames(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        if (!tableNames.isEmpty()) {
            return tableNames;
        }
        return sqlStatementContext instanceof IndexAvailable
                ? getTableNames(database, sqlStatementContext.getDatabaseType(), ((IndexAvailable) sqlStatementContext).getIndexes())
                : Collections.emptyList();
    }
    
    private static Collection<String> getTableNames(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<IndexSegment> indexes) {
        Collection<String> result = new LinkedList<>();
        for (QualifiedTable each : IndexMetaDataUtils.getTableNames(database, databaseType, indexes)) {
            result.add(each.getTableName());
        }
        return result;
    }
    
    private static BroadcastRouteEngine getDALRoutingEngine(final BroadcastRule broadcastRule, final QueryContext queryContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return new BroadcastIgnoreRoutingEngine();
        }
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> broadcastRuleTableNames = broadcastRule.getBroadcastRuleTableNames(tableNames);
        if (broadcastRule.isAllBroadcastTables(broadcastRuleTableNames)) {
            return new BroadcastTableBroadcastRoutingEngine(broadcastRuleTableNames);
        }
        return new BroadcastIgnoreRoutingEngine();
    }
    
    private static BroadcastRouteEngine getDCLRoutingEngine(final BroadcastRule broadcastRule, final QueryContext queryContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<String> broadcastRuleTableNames = broadcastRule.getBroadcastRuleTableNames(sqlStatementContext.getTablesContext().getTableNames());
        if (isDCLForSingleTable(sqlStatementContext) && !broadcastRuleTableNames.isEmpty() || broadcastRule.isAllBroadcastTables(broadcastRuleTableNames)) {
            return new BroadcastTableBroadcastRoutingEngine(broadcastRuleTableNames);
        }
        return new BroadcastIgnoreRoutingEngine();
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private static BroadcastRouteEngine getDQLRoutingEngine(final BroadcastRule broadcastRule, final QueryContext queryContext, final ConnectionContext connectionContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (broadcastRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                    ? new BroadcastUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext)
                    : new BroadcastDatabaseBroadcastRoutingEngine();
        }
        return new BroadcastIgnoreRoutingEngine();
    }
}
