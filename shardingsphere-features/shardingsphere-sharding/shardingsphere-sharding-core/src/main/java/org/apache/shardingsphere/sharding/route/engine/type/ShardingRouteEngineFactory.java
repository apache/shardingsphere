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
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
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
     * @return new instance of routing engine
     */
    public static ShardingRouteEngine newInstance(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext, 
                                                  final ShardingConditions shardingConditions, final ConfigurationProperties props) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof TCLStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLRoutingEngine(shardingRule, metaData, sqlStatementContext);
        }
        if (sqlStatement instanceof DALStatement) {
            return getDALRoutingEngine(shardingRule, metaData, sqlStatementContext);
        }
        if (sqlStatement instanceof DCLStatement) {
            return getDCLRoutingEngine(shardingRule, metaData, sqlStatementContext);
        }
        return getDQLRoutingEngine(shardingRule, sqlStatementContext, shardingConditions, props);
    }
    
    private static ShardingRouteEngine getDDLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        boolean functionStatement = sqlStatement instanceof CreateFunctionStatement || sqlStatement instanceof AlterFunctionStatement || sqlStatement instanceof DropFunctionStatement;
        boolean procedureStatement = sqlStatement instanceof CreateProcedureStatement || sqlStatement instanceof AlterProcedureStatement || sqlStatement instanceof DropProcedureStatement;
        if (functionStatement || procedureStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatement instanceof CreateTablespaceStatement || sqlStatement instanceof AlterTablespaceStatement || sqlStatement instanceof DropTablespaceStatement) {
            return new ShardingInstanceBroadcastRoutingEngine(metaData.getResource().getDataSourcesMetaData());
        }
        Collection<String> tableNames = sqlStatementContext instanceof TableAvailable
                ? ((TableAvailable) sqlStatementContext).getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toSet())
                : sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        if (!tableNames.isEmpty() && shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        return new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext, shardingRuleTableNames);
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
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        if (!tableNames.isEmpty() && shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof AnalyzeTableStatement) {
            return shardingRuleTableNames.isEmpty() ? new ShardingDatabaseBroadcastRoutingEngine() 
                    : new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext, shardingRuleTableNames);
        }
        if (!shardingRuleTableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(shardingRuleTableNames);
        }
        return new ShardingDataSourceGroupBroadcastRoutingEngine();
    }
    
    private static ShardingRouteEngine getDCLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        if (isDCLForSingleTable(sqlStatementContext)) {
            Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(sqlStatementContext.getTablesContext().getTableNames());
            return !shardingRuleTableNames.isEmpty()
                    ? new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext, shardingRuleTableNames)
                    : new ShardingIgnoreRoutingEngine();
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
                                                           final ShardingConditions shardingConditions, final ConfigurationProperties props) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement ? new ShardingUnicastRoutingEngine(tableNames) : new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        if (shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        return getDQLRouteEngineForShardingTable(shardingRule, sqlStatementContext, shardingConditions, shardingRuleTableNames, props);
    }
    
    private static ShardingRouteEngine getDQLRouteEngineForShardingTable(final ShardingRule shardingRule, final SQLStatementContext<?> sqlStatementContext, 
                                                                         final ShardingConditions shardingConditions, final Collection<String> tableNames, final ConfigurationProperties props) {
        if (isShardingFederatedQuery(sqlStatementContext, tableNames, shardingRule, shardingConditions)) {
            return new ShardingFederatedRoutingEngine(tableNames);
        }
        if (isShardingStandardQuery(tableNames, shardingRule)) {
            return new ShardingStandardRoutingEngine(getLogicTableName(shardingConditions, tableNames), shardingConditions, props);
        }
        // TODO config for cartesian set
        return new ShardingComplexRoutingEngine(tableNames, shardingConditions, props);
    }
    
    private static String getLogicTableName(final ShardingConditions shardingConditions, final Collection<String> tableNames) {
        return shardingConditions.getConditions().stream().flatMap(each -> each.getValues().stream())
                .map(ShardingConditionValue::getTableName).findFirst().orElseGet(() -> tableNames.iterator().next());
    }
    
    private static boolean isShardingStandardQuery(final Collection<String> tableNames, final ShardingRule shardingRule) {
        return 1 == tableNames.size() && shardingRule.isAllShardingTables(tableNames) || shardingRule.isAllBindingTables(tableNames);
    }
    
    private static boolean isShardingFederatedQuery(final SQLStatementContext<?> sqlStatementContext, final Collection<String> tableNames, 
                                                    final ShardingRule shardingRule, final ShardingConditions shardingConditions) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        if (select.getPaginationContext().isHasPagination() || (shardingConditions.isNeedMerge() && shardingConditions.isSameShardingCondition())) {
            return false;
        }
        if (select.isContainsSubquery() || select.isContainsHaving() || select.isContainsPartialDistinctAggregation()) {
            return true;
        }
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (!select.isContainsJoinQuery() || shardingRule.isAllTablesInSameDataSource(shardingTableNames)) {
            return false;
        }
        return shardingTableNames.size() > 1 && !shardingRule.isAllBindingTables(shardingTableNames);
    }
}
