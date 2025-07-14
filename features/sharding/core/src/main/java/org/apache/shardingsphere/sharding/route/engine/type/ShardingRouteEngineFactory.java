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

package org.apache.shardingsphere.sharding.route.engine.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRouteEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.AlterProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.DropProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;

/**
 * Sharding routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteEngineFactory {
    
    /**
     * Create new instance of routing engine.
     *
     * @param rule sharding rule
     * @param database database
     * @param queryContext query context
     * @param shardingConditions shardingConditions
     * @param logicTableNames logic table names
     * @param props ShardingSphere properties
     * @return created instance
     */
    public static ShardingRouteEngine newInstance(final ShardingRule rule, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                  final ShardingConditions shardingConditions, final Collection<String> logicTableNames, final ConfigurationProperties props) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof DDLStatement) {
            return queryContext.getSqlStatementContext().getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()
                    ? getCursorRouteEngine(rule, sqlStatementContext, queryContext.getHintValueContext(), shardingConditions, logicTableNames, props)
                    : getDDLRouteEngine(database, sqlStatement, logicTableNames);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRouteEngine(database, sqlStatementContext.getSqlStatement(), queryContext.getConnectionContext(), logicTableNames);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRouteEngine(database, sqlStatementContext, logicTableNames);
        }
        return getDQLRouteEngine(rule, sqlStatementContext, queryContext, shardingConditions, logicTableNames, props);
    }
    
    private static ShardingRouteEngine getDDLRouteEngine(final ShardingSphereDatabase database, final SQLStatement sqlStatement, final Collection<String> logicTableNames) {
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (procedureStatement) {
            return new ShardingDatabaseBroadcastRouteEngine();
        }
        return new ShardingTableBroadcastRouteEngine(database, sqlStatement, logicTableNames);
    }
    
    private static ShardingRouteEngine getCursorRouteEngine(final ShardingRule rule, final SQLStatementContext sqlStatementContext,
                                                            final HintValueContext hintValueContext, final ShardingConditions shardingConditions, final Collection<String> logicTableNames,
                                                            final ConfigurationProperties props) {
        boolean allBindingTables = logicTableNames.size() > 1 && rule.isBindingTablesUseShardingColumnsJoin(sqlStatementContext, logicTableNames);
        if (isShardingStandardQuery(rule, logicTableNames, allBindingTables)) {
            return new ShardingStandardRouteEngine(getLogicTableName(shardingConditions, logicTableNames), shardingConditions, sqlStatementContext, hintValueContext, props);
        }
        return new ShardingIgnoreRouteEngine();
    }
    
    private static ShardingRouteEngine getDALRouteEngine(final ShardingSphereDatabase database, final SQLStatement sqlStatement,
                                                         final ConnectionContext connectionContext, final Collection<String> logicTableNames) {
        if (sqlStatement.getAttributes().findAttribute(TableBroadcastRouteSQLStatementAttribute.class).isPresent()) {
            return new ShardingTableBroadcastRouteEngine(database, sqlStatement, logicTableNames);
        }
        return new ShardingUnicastRouteEngine(sqlStatement, logicTableNames, connectionContext);
    }
    
    private static ShardingRouteEngine getDCLRouteEngine(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final Collection<String> logicTableNames) {
        return isDCLForSingleTable(sqlStatementContext) ? new ShardingTableBroadcastRouteEngine(database, sqlStatementContext.getSqlStatement(), logicTableNames)
                : new ShardingInstanceBroadcastRouteEngine(database.getResourceMetaData());
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext sqlStatementContext) {
        return 1 == sqlStatementContext.getTablesContext().getSimpleTables().size()
                && !"*".equals(sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue());
    }
    
    private static ShardingRouteEngine getDQLRouteEngine(final ShardingRule rule, final SQLStatementContext sqlStatementContext,
                                                         final QueryContext queryContext, final ShardingConditions shardingConditions, final Collection<String> logicTableNames,
                                                         final ConfigurationProperties props) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRouteEngine(sqlStatementContext.getSqlStatement(), tableNames, queryContext.getConnectionContext());
        }
        return getDQLRouteEngineForShardingTable(rule, sqlStatementContext, queryContext.getHintValueContext(), shardingConditions, logicTableNames, props);
    }
    
    private static ShardingRouteEngine getDQLRouteEngineForShardingTable(final ShardingRule rule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext,
                                                                         final ShardingConditions shardingConditions, final Collection<String> logicTableNames, final ConfigurationProperties props) {
        boolean allBindingTables = logicTableNames.size() > 1 && rule.isBindingTablesUseShardingColumnsJoin(sqlStatementContext, logicTableNames);
        if (isShardingStandardQuery(rule, logicTableNames, allBindingTables)) {
            return new ShardingStandardRouteEngine(getLogicTableName(shardingConditions, logicTableNames), shardingConditions, sqlStatementContext, hintValueContext, props);
        }
        // TODO config for cartesian set
        return new ShardingComplexRouteEngine(shardingConditions, sqlStatementContext, hintValueContext, props, logicTableNames);
    }
    
    private static String getLogicTableName(final ShardingConditions shardingConditions, final Collection<String> tableNames) {
        if (shardingConditions.getConditions().isEmpty()) {
            return tableNames.iterator().next();
        }
        ShardingCondition shardingCondition = shardingConditions.getConditions().iterator().next();
        return shardingCondition.getValues().isEmpty() ? tableNames.iterator().next() : shardingCondition.getValues().iterator().next().getTableName();
    }
    
    private static boolean isShardingStandardQuery(final ShardingRule rule, final Collection<String> logicTableNames, final boolean allBindingTables) {
        return 1 == logicTableNames.size() && rule.isAllShardingTables(logicTableNames) || allBindingTables;
    }
}
