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
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.ParametersAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SQL token generator for optimizing sharding IN predicates with value filtering.
 * Refactored for enhanced readability, maintainability, and performance with Java 8 compatibility.
 */
@RequiredArgsConstructor
public final class ShardingInPredicateTokenGenerator
        implements
            CollectionSQLTokenGenerator<SQLStatementContext>,
            RouteContextAware,
            IgnoreForSingleRoute,
            ParametersAware {
    
    private final ShardingRule shardingRule;
    
    @Setter
    private RouteContext routeContext;
    
    @Setter
    private List<Object> parameters = Collections.emptyList();
    
    /**
     * Check if SQL token generation is needed for the given statement context.
     */
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext
                && hasOptimizableInConditions((SelectStatementContext) sqlStatementContext);
    }
    
    /**
     * Generate SQL tokens for optimizing IN predicates in sharding scenarios.
     */
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        return extractOptimizableInExpressions((SelectStatementContext) sqlStatementContext)
                .stream()
                .map(inExpr -> createTokenForExpression(inExpr, (SelectStatementContext) sqlStatementContext))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if there are optimizable IN conditions in the SELECT statement.
     *
     * @param selectContext Select statement context
     * @return true if optimizable IN conditions exist, false otherwise
     */
    private boolean hasOptimizableInConditions(final SelectStatementContext selectContext) {
        return !extractOptimizableInExpressions(selectContext).isEmpty();
    }
    
    /**
     * Extract all IN expressions that can be optimized for sharding.
     *
     * @param selectContext Select statement context
     * @return List of optimizable IN expressions
     */
    private List<InExpression> extractOptimizableInExpressions(final SelectStatementContext selectContext) {
        Collection<WhereSegment> whereSegments = selectContext.getWhereSegments();
        if (whereSegments == null || whereSegments.isEmpty()) {
            return Collections.emptyList();
        }
        return whereSegments
                .stream()
                .flatMap(segment -> extractInExpressionsRecursively(segment.getExpr()))
                .filter(this::isOptimizable)
                .filter(inExpr -> isShardingRelated(inExpr, selectContext))
                .collect(Collectors.toList());
    }
    
    /**
     * Recursively extract IN expressions from nested binary operations.
     *
     * @param expression Expression segment
     * @return Stream of IN expressions
     */
    private Stream<InExpression> extractInExpressionsRecursively(final ExpressionSegment expression) {
        if (expression instanceof InExpression) {
            return Stream.of((InExpression) expression);
        }
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpr = (BinaryOperationExpression) expression;
            return Stream.concat(
                    extractInExpressionsRecursively(binaryExpr.getLeft()),
                    extractInExpressionsRecursively(binaryExpr.getRight()));
        }
        return Stream.empty();
    }
    
    /**
     * Check if an IN expression is optimizable (has list with multiple items).
     *
     * @param inExpression IN expression
     * @return true if optimizable, false otherwise
     */
    private boolean isOptimizable(final InExpression inExpression) {
        if (!(inExpression.getRight() instanceof ListExpression)) {
            return false;
        }
        ListExpression listExpr = (ListExpression) inExpression.getRight();
        return listExpr.getItems().size() > 1;
    }
    
    /**
     * Check if an IN expression is related to sharding (involves sharded table and column).
     *
     * @param inExpr       IN expression
     * @param selectContext Select statement context
     * @return true if sharding related, false otherwise
     */
    private boolean isShardingRelated(final InExpression inExpr, final SelectStatementContext selectContext) {
        String tableName = resolveTableName(inExpr, selectContext);
        String columnName = resolveColumnName(inExpr);
        return tableName != null && columnName != null
                && shardingRule.isShardingTable(tableName)
                && getShardingColumns(tableName).contains(columnName);
    }
    
    /**
     * Create a sharding token for a single IN expression.
     *
     * @param inExpr       IN expression
     * @param selectContext Select statement context
     * @return Sharding token for the expression
     */
    private SQLToken createTokenForExpression(final InExpression inExpr, final SelectStatementContext selectContext) {
        String tableName = resolveTableName(inExpr, selectContext);
        String columnName = resolveColumnName(inExpr);
        
        if (tableName == null || columnName == null) {
            return null;
        }
        
        List<ShardingInPredicateValue> distributedValues = extractAndDistributeValues(inExpr, tableName, columnName);
        if (distributedValues.isEmpty()) {
            return null;
        }
        return new ShardingInPredicateToken(inExpr.getStartIndex(), inExpr.getStopIndex(), columnName, distributedValues);
    }
    
    /**
     * Extract values from IN expression and distribute them across shards.
     *
     * @param inExpr       IN expression
     * @param tableName    Name of the sharded table
     * @param columnName   Name of the sharded column
     * @return List of distributed sharding values
     */
    private List<ShardingInPredicateValue> extractAndDistributeValues(final InExpression inExpr,
                                                                      final String tableName, final String columnName) {
        if (!(inExpr.getRight() instanceof ListExpression)) {
            return Collections.emptyList();
        }
        
        ListExpression listExpr = (ListExpression) inExpr.getRight();
        return listExpr.getItems()
                .stream()
                .map(item -> processExpressionItem(item, tableName, columnName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Process a single expression item and determine its sharding distribution.
     *
     * @param item         Expression item
     * @param tableName    Name of the sharded table
     * @param columnName   Name of the sharded column
     * @return Sharding value for the item
     */
    private ShardingInPredicateValue processExpressionItem(final ExpressionSegment item,
                                                           final String tableName, final String columnName) {
        ValueInfo valueInfo = extractValueFromExpression(item);
        if (valueInfo == null) {
            return null;
        }
        return createShardingInPredicateValue(valueInfo, tableName, columnName);
    }
    
    /**
     * Extract value information from literal or parameter marker expressions.
     *
     * @param item Expression item
     * @return Value information if available
     */
    private ValueInfo extractValueFromExpression(final ExpressionSegment item) {
        if (item instanceof LiteralExpressionSegment) {
            Object literal = ((LiteralExpressionSegment) item).getLiterals();
            if (literal instanceof Comparable) {
                return new ValueInfo(-1, (Comparable<?>) literal, false);
            }
        } else if (item instanceof ParameterMarkerExpressionSegment) {
            int index = ((ParameterMarkerExpressionSegment) item).getParameterMarkerIndex();
            Comparable<?> paramValue = extractParameterValue(index);
            if (paramValue != null) {
                return new ValueInfo(index, paramValue, true);
            }
        }
        return null;
    }
    
    /**
     * Extract parameter value by index with bounds checking.
     *
     * @param index Index of the parameter
     * @return Parameter value if available
     */
    private Comparable<?> extractParameterValue(final int index) {
        if (index < 0 || index >= parameters.size()) {
            return null;
        }
        Object paramValue = parameters.get(index);
        return paramValue instanceof Comparable ? (Comparable<?>) paramValue : null;
    }
    
    /**
     * Create sharding predicate value using appropriate strategy.
     *
     * @param valueInfo    Value information
     * @param tableName    Name of the sharded table
     * @param columnName   Name of the sharded column
     * @return Sharding value for the item
     */
    private ShardingInPredicateValue createShardingInPredicateValue(final ValueInfo valueInfo,
                                                                    final String tableName, final String columnName) {
        ShardingStrategyConfiguration strategy = getShardingStrategy(tableName);
        if (strategy == null) {
            return createDefaultValue(valueInfo);
        }
        
        if (strategy instanceof StandardShardingStrategyConfiguration) {
            return processStandardStrategy(valueInfo, tableName, columnName, (StandardShardingStrategyConfiguration) strategy);
        } else if (strategy instanceof ComplexShardingStrategyConfiguration) {
            return processComplexStrategy(valueInfo, tableName, columnName, (ComplexShardingStrategyConfiguration) strategy);
        }
        
        return createDefaultValue(valueInfo);
    }
    
    /**
     * Process value using standard sharding strategy with single column.
     *
     * @param valueInfo    Value information
     * @param tableName    Name of the sharded table
     * @param columnName   Name of the sharded column
     * @param strategy     Standard sharding strategy configuration
     * @return Sharding value for the item
     */
    private ShardingInPredicateValue processStandardStrategy(final ValueInfo valueInfo, final String tableName,
                                                             final String columnName, final StandardShardingStrategyConfiguration strategy) {
        if (!strategy.getShardingColumn().equals(columnName)) {
            return createDefaultValue(valueInfo);
        }
        
        StandardShardingAlgorithm<Comparable<?>> algorithm = getStandardAlgorithm(strategy);
        if (algorithm == null) {
            return createDefaultValue(valueInfo);
        }
        
        String targetTable = calculateTargetTable(tableName, columnName, valueInfo.value, algorithm);
        if (targetTable == null) {
            return ShardingInPredicateValue.createOrphan(valueInfo.parameterIndex, valueInfo.value, valueInfo.isParameter);
        }
        
        return createValueWithRoutes(valueInfo, Collections.singleton(targetTable), tableName);
    }
    
    /**
     * Process value using complex sharding strategy with multiple columns.
     *
     * @param valueInfo    Value information
     * @param tableName    Name of the sharded table
     * @param columnName   Name of the sharded column
     * @param strategy     Complex sharding strategy configuration
     * @return Sharding value for the item
     */
    private ShardingInPredicateValue processComplexStrategy(final ValueInfo valueInfo, final String tableName,
                                                            final String columnName, final ComplexShardingStrategyConfiguration strategy) {
        Set<String> shardingColumns = parseColumns(strategy.getShardingColumns());
        if (!shardingColumns.contains(columnName)) {
            return createDefaultValue(valueInfo);
        }
        
        ComplexKeysShardingAlgorithm<Comparable<?>> algorithm = getComplexAlgorithm(strategy);
        if (algorithm == null) {
            return createDefaultValue(valueInfo);
        }
        
        Collection<String> targetTables = calculateTargetTablesForComplex(tableName, columnName, valueInfo.value, algorithm);
        if (targetTables.isEmpty()) {
            return ShardingInPredicateValue.createOrphan(valueInfo.parameterIndex, valueInfo.value, valueInfo.isParameter);
        }
        
        return createValueWithRoutes(valueInfo, targetTables, tableName);
    }
    
    /**
     * Create predicate value with default distribution (all routes).
     *
     * @param valueInfo Value information
     * @return Default sharding value for the item
     */
    private ShardingInPredicateValue createDefaultValue(final ValueInfo valueInfo) {
        return new ShardingInPredicateValue(valueInfo.parameterIndex, valueInfo.value, valueInfo.isParameter,
                new HashSet<>(routeContext.getRouteUnits()));
    }
    
    /**
     * Create predicate value with specific target routes.
     *
     * @param valueInfo    Value information
     * @param targetTables Target tables for sharding
     * @param tableName    Name of the sharded table
     * @return Sharding value for the item
     */
    private ShardingInPredicateValue createValueWithRoutes(final ValueInfo valueInfo, final Collection<String> targetTables, final String tableName) {
        Set<RouteUnit> targetRoutes = routeContext.getRouteUnits()
                .stream()
                .filter(routeUnit -> {
                    String actualTable = getActualTableName(routeUnit, tableName);
                    return actualTable != null && targetTables.contains(actualTable);
                })
                .collect(Collectors.toSet());
        
        return new ShardingInPredicateValue(valueInfo.parameterIndex, valueInfo.value, valueInfo.isParameter, targetRoutes);
    }
    
    /**
     * Resolve table name from IN expression or context.
     *
     * @param inExpression IN expression
     * @param selectContext Select statement context
     * @return Resolved table name
     */
    private String resolveTableName(final InExpression inExpression, final SelectStatementContext selectContext) {
        if (!(inExpression.getLeft() instanceof ColumnSegment)) {
            return null;
        }
        
        ColumnSegment column = (ColumnSegment) inExpression.getLeft();
        if (column.getOwner().isPresent()) {
            return column.getOwner().get().getIdentifier().getValue();
        }
        
        return findFirstShardingTable(selectContext);
    }
    
    /**
     * Find the first sharding table in the context.
     *
     * @param selectContext Select statement context
     * @return Resolved table name
     */
    private String findFirstShardingTable(final SelectStatementContext selectContext) {
        return selectContext.getTablesContext()
                .getTableNames()
                .stream()
                .filter(shardingRule::isShardingTable)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Resolve column name from IN expression.
     *
     * @param inExpression IN expression
     * @return Resolved column name
     */
    private String resolveColumnName(final InExpression inExpression) {
        if (!(inExpression.getLeft() instanceof ColumnSegment)) {
            return null;
        }
        return ((ColumnSegment) inExpression.getLeft()).getIdentifier().getValue();
    }
    
    /**
     * Get sharding strategy configuration for the given table.
     *
     * @param tableName Name of the sharded table
     * @return Sharding strategy configuration
     */
    private ShardingStrategyConfiguration getShardingStrategy(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(shardingRule::getTableShardingStrategyConfiguration)
                .orElse(null);
    }
    
    /**
     * Get standard sharding algorithm from strategy configuration.
     *
     * @param strategy Standard sharding strategy configuration
     * @return Standard sharding algorithm
     */
    @SuppressWarnings("unchecked")
    private StandardShardingAlgorithm<Comparable<?>> getStandardAlgorithm(final StandardShardingStrategyConfiguration strategy) {
        Object algorithm = shardingRule.getShardingAlgorithms().get(strategy.getShardingAlgorithmName());
        return algorithm instanceof StandardShardingAlgorithm ? (StandardShardingAlgorithm<Comparable<?>>) algorithm : null;
    }
    
    /**
     * Get complex sharding algorithm from strategy configuration.
     *
     * @param strategy Complex sharding strategy configuration
     * @return Complex sharding algorithm
     */
    @SuppressWarnings("unchecked")
    private ComplexKeysShardingAlgorithm<Comparable<?>> getComplexAlgorithm(final ComplexShardingStrategyConfiguration strategy) {
        Object algorithm = shardingRule.getShardingAlgorithms().get(strategy.getShardingAlgorithmName());
        return algorithm instanceof ComplexKeysShardingAlgorithm ? (ComplexKeysShardingAlgorithm<Comparable<?>>) algorithm : null;
    }
    
    /**
     * Calculate target table using standard sharding algorithm.
     *
     * @param tableName Name of the sharded table
     * @param columnName Name of the sharded column
     * @param value Value to be sharded
     * @param algorithm Standard sharding algorithm
     * @return Target table name
     */
    private String calculateTargetTable(final String tableName, final String columnName,
                                        final Comparable<?> value, final StandardShardingAlgorithm<Comparable<?>> algorithm) {
        return algorithm.doSharding(getAvailableTargets(tableName),
                new PreciseShardingValue<>(tableName, columnName, new DataNodeInfo("", 1, (char) 0), value));
    }
    
    /**
     * Calculate target tables using complex sharding algorithm.
     *
     * @param tableName Name of the sharded table
     * @param columnName Name of the sharded column
     * @param value Value to be sharded
     * @param algorithm Complex sharding algorithm
     * @return Target table names
     */
    private Collection<String> calculateTargetTablesForComplex(final String tableName, final String columnName,
                                                               final Comparable<?> value, final ComplexKeysShardingAlgorithm<Comparable<?>> algorithm) {
        Map<String, Collection<Comparable<?>>> shardingValues = Collections.singletonMap(columnName, Collections.singleton(value));
        return algorithm.doSharding(getAvailableTargets(tableName),
                new ComplexKeysShardingValue<>(tableName, shardingValues, Collections.emptyMap()));
    }
    
    /**
     * Get all available target tables for the given logical table.
     *
     * @param tableName Name of the sharded table
     * @return All available target table names
     */
    private Collection<String> getAvailableTargets(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(ShardingTable::getActualDataNodes)
                .map(nodes -> nodes.stream().map(DataNode::getTableName).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }
    
    /**
     * Get actual table name from route unit for the given logical table.
     *
     * @param routeUnit Route unit
     * @param logicalTableName Logical table name
     * @return Actual table name
     */
    private String getActualTableName(final RouteUnit routeUnit, final String logicalTableName) {
        return routeUnit.getTableMappers()
                .stream()
                .filter(mapper -> logicalTableName.equals(mapper.getLogicName()))
                .map(RouteMapper::getActualName)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Parse comma-separated column names into a set.
     *
     * @param columnsStr Comma-separated column names
     * @return Set of column names
     */
    private Set<String> parseColumns(final String columnsStr) {
        return Arrays.stream(columnsStr.split(","))
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    /**
     * Get all sharding columns for the given table.
     *
     * @param tableName Name of the sharded table
     * @return Set of sharding columns
     */
    private Set<String> getShardingColumns(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(shardingRule::getTableShardingStrategyConfiguration)
                .map(this::extractShardingColumns)
                .orElse(Collections.emptySet());
    }
    
    /**
     * Extract sharding columns from strategy configuration.
     *
     * @param strategy Sharding strategy configuration
     * @return Set of sharding columns
     */
    private Set<String> extractShardingColumns(final ShardingStrategyConfiguration strategy) {
        if (strategy instanceof StandardShardingStrategyConfiguration) {
            return Collections.singleton(((StandardShardingStrategyConfiguration) strategy).getShardingColumn());
        }
        if (strategy instanceof ComplexShardingStrategyConfiguration) {
            return parseColumns(((ComplexShardingStrategyConfiguration) strategy).getShardingColumns());
        }
        return Collections.emptySet();
    }
    
    /**
     * Value information holder for cleaner parameter passing.
     */
    private static class ValueInfo {
        
        private final int parameterIndex;
        
        private final Comparable<?> value;
        
        private final boolean isParameter;
        
        ValueInfo(final int parameterIndex, final Comparable<?> value, final boolean isParameter) {
            this.parameterIndex = parameterIndex;
            this.value = value;
            this.isParameter = isParameter;
        }
    }
}
