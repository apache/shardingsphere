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
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingDatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.broadcast.ShardingTableBroadcastRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.complex.ShardingComplexRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.federated.ShardingFederatedRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.ignore.ShardingIgnoreRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.standard.ShardingStandardRoutingEngine;
import org.apache.shardingsphere.sharding.route.engine.type.unicast.ShardingUnicastRoutingEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLLoadStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;

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
     * @param metaData ShardingSphere metaData
     * @param sqlStatementContext SQL statement context
     * @param shardingConditions shardingConditions
     * @param props ShardingSphere properties
     * @param routeUnits route units
     * @return new instance of routing engine
     */
    public static ShardingRouteEngine newInstance(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext, 
                                                  final ShardingConditions shardingConditions, final ConfigurationProperties props, final Collection<RouteUnit> routeUnits) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRoutingEngine(metaData, sqlStatementContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(shardingRule, metaData, sqlStatementContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(metaData, sqlStatementContext);
        }
        return getDQLRoutingEngine(shardingRule, sqlStatementContext, shardingConditions, props, routeUnits);
    }
    
    private static ShardingRouteEngine getDDLRoutingEngine(final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return new ShardingInstanceBroadcastRoutingEngine(metaData.getResource().getDataSourcesMetaData());
        }
        return new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext);
    }
    
    private static ShardingRouteEngine getDALRoutingEngine(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof MySQLUseStatement) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof PostgreSQLResetParameterStatement 
                || sqlStatement instanceof MySQLShowDatabasesStatement || sqlStatement instanceof PostgreSQLLoadStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        Collection<String> tableNames = shardingRule.getShardingBroadcastTableNames(sqlStatementContext.getTablesContext().getTableNames());
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return tableNames.isEmpty() ? new ShardingDatabaseBroadcastRoutingEngine() : new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext);
        }
        if (!tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        return new ShardingDataSourceGroupBroadcastRoutingEngine();
    }
    
    private static ShardingRouteEngine getDCLRoutingEngine(final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        if (isDCLForSingleTable(sqlStatementContext)) {
            return new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext);
        } else {
            return new ShardingInstanceBroadcastRoutingEngine(metaData.getResource().getDataSourcesMetaData());
        }
    }
    
    private static boolean isDCLForSingleTable(final SQLStatementContext<?> sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            TableAvailable tableSegmentsAvailable = (TableAvailable) sqlStatementContext;
            return 1 == tableSegmentsAvailable.getAllTables().size() && !"*".equals(tableSegmentsAvailable.getAllTables().iterator().next().getTableName().getIdentifier().getValue());
        }
        return false;
    }
    
    private static ShardingRouteEngine getDQLRoutingEngine(final ShardingRule shardingRule, final SQLStatementContext<?> sqlStatementContext, 
                                                           final ShardingConditions shardingConditions, final ConfigurationProperties props, final Collection<RouteUnit> routeUnits) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement ? new ShardingUnicastRoutingEngine(tableNames) : new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        if (isShardingStandardQuery(sqlStatementContext, tableNames, shardingRule)) {
            return new ShardingStandardRoutingEngine(tableNames.iterator().next(), shardingConditions, props);
        }
        if (isShardingFederatedQuery(sqlStatementContext, tableNames, shardingRule, routeUnits)) {
            return new ShardingFederatedRoutingEngine(tableNames);
        }
        // TODO config for cartesian set
        return new ShardingComplexRoutingEngine(tableNames, shardingConditions, props);
    }
    
    private static boolean isShardingStandardQuery(final SQLStatementContext<?> sqlStatementContext, final Collection<String> tableNames, final ShardingRule shardingRule) {
        boolean needExecuteByCalcite = sqlStatementContext instanceof SelectStatementContext && isNeedExecuteByCalcite((SelectStatementContext) sqlStatementContext); 
        return (shardingRule.isAllBindingTables(tableNames) || shardingRule.isAllShardingTables(tableNames) && 1 == tableNames.size()) && !needExecuteByCalcite;
    }
    
    private static boolean isShardingFederatedQuery(final SQLStatementContext<?> sqlStatementContext, final Collection<String> tableNames, 
                                                    final ShardingRule shardingRule, final Collection<RouteUnit> routeUnits) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        if (isNeedExecuteByCalcite(select)) {
            return true;
        }
        Collection<String> routeDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
        if ((!select.isContainsJoinQuery() && !select.isContainsSubquery()) || shardingRule.isAllTablesInSameDataSource(tableNames, routeDataSourceNames)) {
            return false;
        }
        return shardingRule.isAllShardingTables(tableNames) || (shardingRule.tableRuleExists(tableNames) && shardingRule.singleTableRuleExists(tableNames));
    }
    
    private static boolean isNeedExecuteByCalcite(final SelectStatementContext select) {
        return select.isContainsHaving() || select.isContainsSubqueyAggregation() || select.isContainsPartialDistinctAggregation();
    }
}
