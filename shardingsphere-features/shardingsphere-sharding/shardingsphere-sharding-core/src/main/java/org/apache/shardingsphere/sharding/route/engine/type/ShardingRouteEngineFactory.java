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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Sharding routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteEngineFactory {
    
    /**
     * Create new instance of routing engine.
     *
     * @param shardingRule sharding rule
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @param shardingConditions shardingConditions
     * @param props ShardingSphere properties
     * @param connectionContext connection context
     * @return created instance
     */
    public static ShardingRouteEngine newInstance(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext,
                                                  final ShardingConditions shardingConditions, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRoutingEngine(shardingRule, database, sqlStatementContext, shardingConditions, props, connectionContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(shardingRule, database, sqlStatementContext, connectionContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(shardingRule, database, sqlStatementContext);
        }
        return getDQLRoutingEngine(shardingRule, database, sqlStatementContext, shardingConditions, props, connectionContext);
    }
    
    private static ShardingRouteEngine getDDLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext,
                                                           final ShardingConditions shardingConditions, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return new ShardingInstanceBroadcastRoutingEngine(database.getResource());
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        String sqlFederationType = props.getValue(ConfigurationPropertyKey.SQL_FEDERATION_TYPE);
        // TODO remove this logic when jdbc adapter can support executing create logic view
        if (!"NONE".equals(sqlFederationType) && (sqlStatement instanceof CreateViewStatement || sqlStatement instanceof AlterViewStatement || sqlStatement instanceof DropViewStatement)) {
            return new ShardingUnicastRoutingEngine(sqlStatementContext, shardingRuleTableNames, connectionContext);
        }
        if (!tableNames.isEmpty() && shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatementContext instanceof CursorAvailable) {
            return getCursorRouteEngine(shardingRule, database, sqlStatementContext, shardingConditions, props, tableNames, connectionContext);
        }
        return new ShardingTableBroadcastRoutingEngine(database, sqlStatementContext, shardingRuleTableNames);
    }
    
    private static ShardingRouteEngine getCursorRouteEngine(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext,
                                                            final ShardingConditions shardingConditions, final ConfigurationProperties props, final Collection<String> tableNames,
                                                            final ConnectionContext connectionContext) {
        if (sqlStatementContext instanceof CloseStatementContext && ((CloseStatementContext) sqlStatementContext).getSqlStatement().isCloseAll()) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return new ShardingUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext);
        }
        Collection<String> logicTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        boolean allBindingTables = logicTableNames.size() > 1 && shardingRule.isAllBindingTables(database, sqlStatementContext, logicTableNames);
        if (isShardingStandardQuery(shardingRule, logicTableNames, allBindingTables)) {
            return new ShardingStandardRoutingEngine(getLogicTableName(shardingConditions, logicTableNames), shardingConditions, props);
        }
        return new ShardingIgnoreRoutingEngine();
    }
    
    private static ShardingRouteEngine getDALRoutingEngine(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext,
                                                           final ConnectionContext connectionContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof ResetParameterStatement
                || sqlStatement instanceof MySQLShowDatabasesStatement || sqlStatement instanceof LoadStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (isResourceGroupStatement(sqlStatement)) {
            return new ShardingInstanceBroadcastRoutingEngine(database.getResource());
        }
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        if (!tableNames.isEmpty() && shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof MySQLOptimizeTableStatement) {
            return new ShardingTableBroadcastRoutingEngine(database, sqlStatementContext, shardingRuleTableNames);
        }
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return shardingRuleTableNames.isEmpty() ? new ShardingDatabaseBroadcastRoutingEngine()
                    : new ShardingTableBroadcastRoutingEngine(database, sqlStatementContext, shardingRuleTableNames);
        }
        if (!shardingRuleTableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(sqlStatementContext, shardingRuleTableNames, connectionContext);
        }
        return new ShardingDataSourceGroupBroadcastRoutingEngine();
    }
    
    private static boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        // TODO add dropResourceGroupStatement, alterResourceGroupStatement
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLSetResourceGroupStatement;
    }
    
    private static ShardingRouteEngine getDCLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext) {
        if (isDCLForSingleTable(sqlStatementContext)) {
            Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(sqlStatementContext.getTablesContext().getTableNames());
            return !shardingRuleTableNames.isEmpty()
                    ? new ShardingTableBroadcastRoutingEngine(database, sqlStatementContext, shardingRuleTableNames)
                    : new ShardingIgnoreRoutingEngine();
        } else {
            return new ShardingInstanceBroadcastRoutingEngine(database.getResource());
        }
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext<?> sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private static ShardingRouteEngine getDQLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereDatabase database, final SQLStatementContext<?> sqlStatementContext,
                                                           final ShardingConditions shardingConditions, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement
                    ? new ShardingUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext)
                    : new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(sqlStatementContext, tableNames, connectionContext);
        }
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (shardingLogicTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        return getDQLRouteEngineForShardingTable(shardingRule, database, sqlStatementContext, shardingConditions, props, shardingLogicTableNames);
    }
    
    private static ShardingRouteEngine getDQLRouteEngineForShardingTable(final ShardingRule shardingRule, final ShardingSphereDatabase database,
                                                                         final SQLStatementContext<?> sqlStatementContext, final ShardingConditions shardingConditions,
                                                                         final ConfigurationProperties props, final Collection<String> tableNames) {
        boolean allBindingTables = tableNames.size() > 1 && shardingRule.isAllBindingTables(database, sqlStatementContext, tableNames);
        if (isShardingStandardQuery(shardingRule, tableNames, allBindingTables)) {
            return new ShardingStandardRoutingEngine(getLogicTableName(shardingConditions, tableNames), shardingConditions, props);
        }
        // TODO config for cartesian set
        return new ShardingComplexRoutingEngine(shardingConditions, props, tableNames);
    }
    
    private static String getLogicTableName(final ShardingConditions shardingConditions, final Collection<String> tableNames) {
        for (ShardingCondition each : shardingConditions.getConditions()) {
            for (ShardingConditionValue shardingConditionValue : each.getValues()) {
                return shardingConditionValue.getTableName();
            }
        }
        return tableNames.iterator().next();
    }
    
    private static boolean isShardingStandardQuery(final ShardingRule shardingRule, final Collection<String> tableNames, final boolean allBindingTables) {
        return 1 == tableNames.size() && shardingRule.isAllShardingTables(tableNames) || allBindingTables;
    }
}
