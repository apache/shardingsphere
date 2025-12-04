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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeInfo;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simplified SQL token generator for optimizing sharding IN predicates.
 * This version only supports:
 * - Literal values (no parameter markers)
 * - Standard sharding algorithms
 * - Simple WHERE clauses (no nested expressions)
 * 
 * @author yinh
 */
@RequiredArgsConstructor
public final class ShardingInPredicateTokenGenerator
        implements
            CollectionSQLTokenGenerator<SQLStatementContext>,
            RouteContextAware,
            IgnoreForSingleRoute {
    
    private final ShardingRule shardingRule;
    
    @Setter
    private RouteContext routeContext;
    
    /**
     * Check if SQL token should be generated for the given SQL statement context.
     * Only generates tokens for SELECT statements with WHERE clauses containing optimizable IN expressions.
     *
     * @param sqlStatementContext SQL statement context
     * @return true if token should be generated, false otherwise
     */
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof SelectStatementContext)) {
            return false;
        }
        SelectStatementContext selectContext = (SelectStatementContext) sqlStatementContext;
        Collection<WhereSegment> whereSegments = selectContext.getWhereSegments();
        if (whereSegments == null || whereSegments.isEmpty()) {
            return false;
        }
        // Check if there are any optimizable IN expressions
        for (WhereSegment whereSegment : whereSegments) {
            if (hasOptimizableInExpression(whereSegment.getExpr(), selectContext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generate SQL tokens for optimizing IN predicates in the given SQL statement.
     * Extracts all optimizable IN expressions and creates corresponding tokens.
     *
     * @param sqlStatementContext SQL statement context
     * @return collection of generated SQL tokens
     */
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        SelectStatementContext selectContext = (SelectStatementContext) sqlStatementContext;
        Collection<SQLToken> result = new LinkedList<>();
        Collection<WhereSegment> whereSegments = selectContext.getWhereSegments();
        if (whereSegments == null || whereSegments.isEmpty()) {
            return result;
        }
        for (WhereSegment whereSegment : whereSegments) {
            extractInExpressions(whereSegment.getExpr(), selectContext, result);
        }
        return result;
    }
    
    /**
     * Check if the given expression is an optimizable IN expression.
     * An IN expression is optimizable if it's on a sharding key column with literal values.
     *
     * @param expression expression segment to check
     * @param selectContext SELECT statement context
     * @return true if the expression is optimizable, false otherwise
     */
    private boolean hasOptimizableInExpression(final ExpressionSegment expression, final SelectStatementContext selectContext) {
        if (!(expression instanceof InExpression)) {
            return false;
        }
        InExpression inExpression = (InExpression) expression;
        // Check if it's a sharding key IN predicate with literal values
        if (!(inExpression.getLeft() instanceof ColumnSegment)) {
            return false;
        }
        if (!(inExpression.getRight() instanceof ListExpression)) {
            return false;
        }
        ColumnSegment column = (ColumnSegment) inExpression.getLeft();
        String columnName = column.getIdentifier().getValue();
        // Get table name from context
        String tableName = getTableName(selectContext);
        if (tableName == null || !shardingRule.isShardingTable(tableName)) {
            return false;
        }
        // Check if it's a sharding column
        return isShardingColumn(tableName, columnName);
    }
    
    /**
     * Extract IN expressions from the given expression and create corresponding tokens.
     *
     * @param expression expression segment to extract from
     * @param selectContext SELECT statement context
     * @param result collection to add generated tokens to
     */
    private void extractInExpressions(final ExpressionSegment expression, final SelectStatementContext selectContext, final Collection<SQLToken> result) {
        if (!(expression instanceof InExpression)) {
            return;
        }
        InExpression inExpression = (InExpression) expression;
        if (!hasOptimizableInExpression(expression, selectContext)) {
            return;
        }
        SQLToken token = createToken(inExpression, selectContext);
        if (token != null) {
            result.add(token);
        }
    }
    
    /**
     * Create a ShardingInPredicateToken from the given IN expression.
     * Extracts literal values and calculates target routes for each value.
     *
     * @param inExpression IN expression to create token from
     * @param selectContext SELECT statement context
     * @return created SQL token, or null if token cannot be created
     */
    private SQLToken createToken(final InExpression inExpression, final SelectStatementContext selectContext) {
        ColumnSegment column = (ColumnSegment) inExpression.getLeft();
        String columnName = column.getIdentifier().getValue();
        String tableName = getTableName(selectContext);
        if (tableName == null) {
            return null;
        }
        ListExpression listExpression = (ListExpression) inExpression.getRight();
        List<ShardingInPredicateValue> values = new ArrayList<>();
        int paramIndex = 0;
        for (ExpressionSegment item : listExpression.getItems()) {
            if (!(item instanceof LiteralExpressionSegment)) {
                // Skip non-literal values in this simplified version
                continue;
            }
            LiteralExpressionSegment literal = (LiteralExpressionSegment) item;
            Object literalValue = literal.getLiterals();
            if (!(literalValue instanceof Comparable)) {
                continue;
            }
            Comparable<?> value = (Comparable<?>) literalValue;
            // Calculate target routes for this value
            Set<RouteUnit> targetRoutes = calculateTargetRoutes(tableName, columnName, value);
            values.add(new ShardingInPredicateValue(paramIndex++, value, false, targetRoutes));
        }
        if (values.isEmpty()) {
            return null;
        }
        return new ShardingInPredicateToken(
                inExpression.getStartIndex(),
                inExpression.getStopIndex(),
                columnName,
                values);
    }
    
    /**
     * Calculate target route units for a given sharding value.
     * Uses the standard sharding algorithm to determine which shard(s) the value belongs to.
     *
     * @param tableName logical table name
     * @param columnName sharding column name
     * @param value sharding value
     * @return set of route units that the value belongs to
     */
    private Set<RouteUnit> calculateTargetRoutes(final String tableName, final String columnName, final Comparable<?> value) {
        Set<RouteUnit> result = new HashSet<>();
        // Get sharding algorithm
        StandardShardingStrategyConfiguration strategyConfig = getStandardShardingStrategy(tableName);
        if (strategyConfig == null) {
            // If not standard strategy, mark as orphan (empty set)
            return result;
        }
        StandardShardingAlgorithm<Comparable<?>> algorithm = getStandardAlgorithm(strategyConfig);
        if (algorithm == null) {
            return result;
        }
        // Get available target names
        Collection<String> availableTargetNames = getAvailableTargetNames(tableName);
        // Call sharding algorithm
        String targetName = algorithm.doSharding(availableTargetNames,
                new PreciseShardingValue<>(tableName, columnName, new DataNodeInfo("", 1, (char) 0), value));
        if (targetName == null) {
            return result;
        }
        // Map target name to RouteUnit
        for (RouteUnit routeUnit : routeContext.getRouteUnits()) {
            for (RouteMapper tableMapper : routeUnit.getTableMappers()) {
                if (targetName.equals(tableMapper.getActualName())) {
                    result.add(routeUnit);
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Get the table name from the SELECT statement context.
     * For simplicity, returns the first table name.
     *
     * @param selectContext SELECT statement context
     * @return table name, or null if no tables found
     */
    private String getTableName(final SelectStatementContext selectContext) {
        Collection<String> tableNames = selectContext.getTablesContext().getTableNames();
        if (tableNames.isEmpty()) {
            return null;
        }
        // For simplicity, use the first table
        return tableNames.iterator().next();
    }
    
    /**
     * Check if the given column is a sharding column for the table.
     *
     * @param tableName logical table name
     * @param columnName column name to check
     * @return true if the column is a sharding column, false otherwise
     */
    private boolean isShardingColumn(final String tableName, final String columnName) {
        StandardShardingStrategyConfiguration strategyConfig = getStandardShardingStrategy(tableName);
        if (strategyConfig == null) {
            return false;
        }
        return columnName.equalsIgnoreCase(strategyConfig.getShardingColumn());
    }
    
    /**
     * Get the standard sharding strategy configuration for the given table.
     *
     * @param tableName logical table name
     * @return standard sharding strategy configuration, or null if not found or not standard strategy
     */
    private StandardShardingStrategyConfiguration getStandardShardingStrategy(final String tableName) {
        return (StandardShardingStrategyConfiguration) shardingRule.findShardingTable(tableName)
                .map(shardingRule::getTableShardingStrategyConfiguration)
                .filter(strategy -> strategy instanceof StandardShardingStrategyConfiguration)
                .orElse(null);
    }
    
    /**
     * Get the standard sharding algorithm from the strategy configuration.
     *
     * @param strategy standard sharding strategy configuration
     * @return standard sharding algorithm, or null if not found or not standard algorithm
     */
    @SuppressWarnings("unchecked")
    private StandardShardingAlgorithm<Comparable<?>> getStandardAlgorithm(final StandardShardingStrategyConfiguration strategy) {
        Object algorithm = shardingRule.getShardingAlgorithms().get(strategy.getShardingAlgorithmName());
        return algorithm instanceof StandardShardingAlgorithm ? (StandardShardingAlgorithm<Comparable<?>>) algorithm : null;
    }
    
    /**
     * Get available target table names for the given logical table.
     *
     * @param tableName logical table name
     * @return collection of actual table names
     */
    private Collection<String> getAvailableTargetNames(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(ShardingTable::getActualDataNodes)
                .map(nodes -> nodes.stream().map(DataNode::getTableName).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
}
