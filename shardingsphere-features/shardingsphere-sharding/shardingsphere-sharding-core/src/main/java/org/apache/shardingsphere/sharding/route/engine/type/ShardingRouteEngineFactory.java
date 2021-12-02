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
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
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
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
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
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.WhereExtractUtil;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLOptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetResourceGroupStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding routing engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRouteEngineFactory {
    
    private static final String EQUAL = "=";
    
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
        return getDQLRoutingEngine(shardingRule, metaData.getSchema(), sqlStatementContext, shardingConditions, props);
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
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof ResetParameterStatement 
                || sqlStatement instanceof MySQLShowDatabasesStatement || sqlStatement instanceof LoadStatement) {
            return new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (isResourceGroupStatement(sqlStatement)) {
            return new ShardingInstanceBroadcastRoutingEngine(metaData.getResource().getDataSourcesMetaData());
        }
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<String> shardingRuleTableNames = shardingRule.getShardingRuleTableNames(tableNames);
        if (!tableNames.isEmpty() && shardingRuleTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        if (sqlStatement instanceof MySQLOptimizeTableStatement) {
            return new ShardingTableBroadcastRoutingEngine(metaData.getSchema(), sqlStatementContext, shardingRuleTableNames);
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
    
    private static boolean isResourceGroupStatement(final SQLStatement sqlStatement) {
        // TODO add dropResourceGroupStatement, alterResourceGroupStatement
        return sqlStatement instanceof MySQLCreateResourceGroupStatement || sqlStatement instanceof MySQLSetResourceGroupStatement;
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
    
    private static ShardingRouteEngine getDQLRoutingEngine(final ShardingRule shardingRule, final ShardingSphereSchema schema, final SQLStatementContext<?> sqlStatementContext, 
                                                           final ShardingConditions shardingConditions, final ConfigurationProperties props) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatementContext.getSqlStatement() instanceof SelectStatement ? new ShardingUnicastRoutingEngine(tableNames) : new ShardingDatabaseBroadcastRoutingEngine();
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty()) {
            return new ShardingUnicastRoutingEngine(tableNames);
        }
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tableNames);
        if (shardingLogicTableNames.isEmpty()) {
            return new ShardingIgnoreRoutingEngine();
        }
        return getDQLRouteEngineForShardingTable(shardingRule, schema, sqlStatementContext, shardingConditions, props, shardingLogicTableNames);
    }
    
    private static ShardingRouteEngine getDQLRouteEngineForShardingTable(final ShardingRule shardingRule, final ShardingSphereSchema schema, final SQLStatementContext<?> sqlStatementContext, 
                                                                         final ShardingConditions shardingConditions, final ConfigurationProperties props, final Collection<String> tableNames) {
        boolean allBindingTables = tableNames.size() > 1 && isAllBindingTables(shardingRule, schema, sqlStatementContext, tableNames);
        if (isShardingFederatedQuery(shardingRule, sqlStatementContext, shardingConditions, props, tableNames, allBindingTables)) {
            return new ShardingFederatedRoutingEngine(tableNames);
        }
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
    
    private static boolean isShardingFederatedQuery(final ShardingRule shardingRule, final SQLStatementContext<?> sqlStatementContext, final ShardingConditions shardingConditions, 
                                                    final ConfigurationProperties props, final Collection<String> tableNames, final boolean allBindingTables) {
        boolean sqlFederationEnabled = props.getValue(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED);
        if (!sqlFederationEnabled || !(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext select = (SelectStatementContext) sqlStatementContext;
        if (select.getPaginationContext().isHasPagination() || (shardingConditions.isNeedMerge() && shardingConditions.isSameShardingCondition())) {
            return false;
        }
        if (select.isContainsSubquery() || select.isContainsHaving() || select.isContainsPartialDistinctAggregation()) {
            return true;
        }
        if (!select.isContainsJoinQuery() || shardingRule.isAllTablesInSameDataSource(tableNames)) {
            return false;
        }
        return tableNames.size() > 1 && !allBindingTables;
    }
    
    private static boolean isAllBindingTables(final ShardingRule shardingRule, final ShardingSphereSchema schema, 
                                              final SQLStatementContext<?> sqlStatementContext, final Collection<String> tableNames) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return shardingRule.isAllBindingTables(tableNames); 
        }
        return shardingRule.isAllBindingTables(tableNames) && isJoinConditionContainsShardingColumns(shardingRule, schema, (SelectStatementContext) sqlStatementContext, tableNames);
    }
    
    private static boolean isJoinConditionContainsShardingColumns(final ShardingRule shardingRule, final ShardingSphereSchema schema,
                                                                  final SelectStatementContext select, final Collection<String> tableNames) {
        Collection<String> databaseJoinConditionTables = new HashSet<>(tableNames.size());
        Collection<String> tableJoinConditionTables = new HashSet<>(tableNames.size());
        for (WhereSegment each : WhereExtractUtil.getJoinWhereSegments(select.getSqlStatement())) {
            Collection<AndPredicate> andPredicates = ExpressionExtractUtil.getAndPredicates(each.getExpr());
            if (andPredicates.size() > 1) {
                return false;
            }
            for (AndPredicate andPredicate : andPredicates) {
                databaseJoinConditionTables.addAll(getJoinConditionTables(shardingRule, schema, select, andPredicate.getPredicates(), true));
                tableJoinConditionTables.addAll(getJoinConditionTables(shardingRule, schema, select, andPredicate.getPredicates(), false));
            }
        }
        TableRule tableRule = shardingRule.getTableRule(tableNames.iterator().next());
        boolean containsDatabaseShardingColumns = !(tableRule.getDatabaseShardingStrategyConfig() instanceof StandardShardingStrategyConfiguration) 
                || databaseJoinConditionTables.containsAll(tableNames);
        boolean containsTableShardingColumns = !(tableRule.getTableShardingStrategyConfig() instanceof StandardShardingStrategyConfiguration) || tableJoinConditionTables.containsAll(tableNames);
        return containsDatabaseShardingColumns && containsTableShardingColumns;
    }
    
    private static Collection<String> getJoinConditionTables(final ShardingRule shardingRule, final ShardingSphereSchema schema, final SelectStatementContext select, 
                                                             final Collection<ExpressionSegment> predicates, final boolean isDatabaseJoinCondition) {
        Collection<String> result = new LinkedList<>();
        for (ExpressionSegment expression : predicates) {
            if (!isJoinTableConditionExpression(expression)) {
                continue;
            }
            ColumnProjection leftColumn = buildColumnProjection((ColumnSegment) ((BinaryOperationExpression) expression).getLeft());
            ColumnProjection rightColumn = buildColumnProjection((ColumnSegment) ((BinaryOperationExpression) expression).getRight());
            Map<String, String> columnTableNames = select.getTablesContext().findTableName(Arrays.asList(leftColumn, rightColumn), schema);
            Optional<TableRule> leftTableRule = shardingRule.findTableRule(columnTableNames.get(leftColumn.getExpression()));
            Optional<TableRule> rightTableRule = shardingRule.findTableRule(columnTableNames.get(rightColumn.getExpression()));
            if (!leftTableRule.isPresent() || !rightTableRule.isPresent()) {
                continue;
            }
            ShardingStrategyConfiguration leftConfiguration = isDatabaseJoinCondition
                    ? shardingRule.getDatabaseShardingStrategyConfiguration(leftTableRule.get()) : shardingRule.getTableShardingStrategyConfiguration(leftTableRule.get());
            ShardingStrategyConfiguration rightConfiguration = isDatabaseJoinCondition
                    ? shardingRule.getDatabaseShardingStrategyConfiguration(rightTableRule.get()) : shardingRule.getTableShardingStrategyConfiguration(rightTableRule.get());
            if (shardingRule.isShardingColumn(leftConfiguration, leftColumn.getName()) && shardingRule.isShardingColumn(rightConfiguration, rightColumn.getName())) {
                result.add(columnTableNames.get(leftColumn.getExpression()));
                result.add(columnTableNames.get(rightColumn.getExpression()));
            }
        }
        return result;
    }
    
    private static ColumnProjection buildColumnProjection(final ColumnSegment segment) {
        String owner = segment.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        return new ColumnProjection(owner, segment.getIdentifier().getValue(), null);
    }
    
    private static boolean isJoinTableConditionExpression(final ExpressionSegment expression) {
        if (!(expression instanceof BinaryOperationExpression)) {
            return false;
        }
        BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
        return binaryExpression.getLeft() instanceof ColumnSegment && binaryExpression.getRight() instanceof ColumnSegment && EQUAL.equals(binaryExpression.getOperator());
    }
}
