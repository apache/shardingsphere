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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced SQL token generator for optimizing sharding IN predicates with intelligent parameter distribution.
 *
 * <p>This generator analyzes SELECT statements with IN predicates on sharding columns and creates optimized
 * {@link ShardingInPredicateToken} instances that:
 * <ul>
 *   <li>Distribute IN clause values across appropriate database shards</li>
 *   <li>Support both standard (single-column) and complex (multi-column) sharding strategies</li>
 *   <li>Minimize network traffic by sending only relevant values to each shard</li>
 *   <li>Maintain parameter binding correctness in prepared statements</li>
 *   <li>Generate optimized SQL syntax (equality vs IN clauses)</li>
 * </ul>
 *
 * <p><strong>Standard Sharding Strategy Example:</strong><br>
 * Original SQL: {@code SELECT * FROM user WHERE user_id IN (1, 2, 3, 4)}<br>
 * If user_id 1,3 route to shard1 and user_id 2,4 route to shard2:
 * <ul>
 *   <li>Shard1: {@code SELECT * FROM user_0 WHERE user_id IN (1, 3)}</li>
 *   <li>Shard2: {@code SELECT * FROM user_1 WHERE user_id IN (2, 4)}</li>
 * </ul>
 *
 * <p><strong>Complex Sharding Strategy Example:</strong><br>
 * Original SQL: {@code SELECT * FROM order WHERE user_id IN (1, 2) AND tenant_id IN ('a', 'b')}<br>
 * Generates all combinations and distributes based on complex algorithm results.
 *
 * <p><strong>Performance Benefits:</strong>
 * <ul>
 *   <li>Reduced data scanning: Each shard only processes relevant values</li>
 *   <li>Optimized SQL generation: Single values become equality conditions</li>
 *   <li>Parameter efficiency: Unused parameters are filtered out</li>
 *   <li>Network optimization: Smaller SQL statements sent to each shard</li>
 * </ul>
 *
 * @author yinh
 * @see ShardingInPredicateToken
 * @see ShardingInPredicateValue
 */
@RequiredArgsConstructor
public final class ShardingInPredicateTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>,
        RouteContextAware, IgnoreForSingleRoute, ParametersAware {

    /**
     * The sharding rule containing table configurations and algorithms.
     * Used to determine sharding strategies and target destinations for values.
     */
    private final ShardingRule shardingRule;

    @Setter
    private RouteContext routeContext;

    @Setter
    private List<Object> parameters = Collections.emptyList();

    /**
     * Determines if SQL token generation should be applied to the given statement context.
     *
     * <p>Token generation is enabled when:
     * <ul>
     *   <li>The statement is a SELECT statement</li>
     *   <li>The statement contains optimizable IN conditions on sharding columns</li>
     *   <li>The routing context indicates multiple route units (shards)</li>
     * </ul>
     *
     * @param sqlStatementContext the SQL statement context to evaluate
     * @return true if token generation should proceed, false otherwise
     */
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && hasOptimizableInConditions((SelectStatementContext) sqlStatementContext);
    }

    /**
     * Generates a collection of SQL tokens for IN predicate optimization.
     *
     * <p>The generation process:
     * <ol>
     *   <li>Extracts all sharding-related IN expressions from the statement</li>
     *   <li>Groups expressions by table name</li>
     *   <li>Creates optimized tokens for each table with distributed parameters</li>
     *   <li>Filters out null tokens (tables with no valid optimizations)</li>
     * </ol>
     *
     * @param sqlStatementContext the SQL statement context containing IN expressions
     * @return collection of generated SQL tokens for optimization
     */
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        return extractShardingInExpressions((SelectStatementContext) sqlStatementContext).entrySet().stream()
                .map(entry -> createToken(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Creates an optimized sharding IN predicate token for the specified table and expressions.
     *
     * <p>The token creation process:
     * <ol>
     *   <li>Extracts column parameters from IN expressions</li>
     *   <li>Distributes parameters across route units based on sharding strategy</li>
     *   <li>Calculates SQL position boundaries for token replacement</li>
     *   <li>Constructs the final token with distributed parameter mapping</li>
     * </ol>
     *
     * @param tableName the logical table name containing the IN expressions
     * @param columnExpressions mapping of column names to their IN expressions
     * @return the created SQL token, or null if optimization is not applicable
     */
    private SQLToken createToken(final String tableName, final Map<String, InExpression> columnExpressions) {
        Map<String, List<ShardingInPredicateValue>> columnParams = extractColumnParameters(columnExpressions);
        if (columnParams.isEmpty()) {
            return null;
        }

        Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> routeUnitParams = distributeParameters(tableName, columnParams);
        if (routeUnitParams.isEmpty()) {
            return null;
        }

        int[] positions = calculatePositions(columnExpressions.values());
        return new ShardingInPredicateToken(positions[0], positions[1], routeUnitParams);
    }

    /**
     * Distributes parameter values across route units based on the table's sharding strategy.
     *
     * <p>Distribution strategies:
     * <ul>
     *   <li><strong>Standard Strategy</strong>: Uses single-column sharding algorithm</li>
     *   <li><strong>Complex Strategy</strong>: Uses multi-column combination algorithm</li>
     *   <li><strong>Default Distribution</strong>: All parameters sent to all route units</li>
     * </ul>
     *
     * @param tableName the logical table name to get sharding strategy for
     * @param columnParams the extracted column parameters to distribute
     * @return mapping of route units to their assigned parameters
     */
    private Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> distributeParameters(
            final String tableName, final Map<String, List<ShardingInPredicateValue>> columnParams) {

        ShardingStrategyConfiguration strategy = getStrategy(tableName);
        if (null == strategy) {
            return createDefaultDistribution(columnParams);
        }

        return strategy instanceof StandardShardingStrategyConfiguration
                ? distributeByStandard(tableName, columnParams, (StandardShardingStrategyConfiguration) strategy)
                : strategy instanceof ComplexShardingStrategyConfiguration
                ? distributeByComplex(tableName, columnParams, (ComplexShardingStrategyConfiguration) strategy)
                : createDefaultDistribution(columnParams);
    }

    /**
     * Distributes parameters using a complex (multi-column) sharding strategy.
     *
     * <p>Process:
     * <ol>
     *   <li>Identifies all sharding columns and validates availability</li>
     *   <li>Generates cartesian product of all column value combinations</li>
     *   <li>Applies complex sharding algorithm to each combination</li>
     *   <li>Groups combinations by their target table destinations</li>
     *   <li>Maps grouped combinations to corresponding route units</li>
     * </ol>
     *
     * @param tableName the logical table name
     * @param columnParams all column parameters from IN expressions
     * @param strategy the standard sharding strategy configuration
     * @return distributed parameters by route unit
     */
    private Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> distributeByStandard(
            final String tableName, final Map<String, List<ShardingInPredicateValue>> columnParams,
            final StandardShardingStrategyConfiguration strategy) {

        String shardingColumn = strategy.getShardingColumn();
        List<ShardingInPredicateValue> shardingParams = columnParams.get(shardingColumn);
        StandardShardingAlgorithm<Comparable<?>> algorithm = getStandardAlgorithm(strategy);

        if (null == shardingParams || null == algorithm) {
            return createDefaultDistribution(columnParams);
        }

        // Group parameters by target table using sharding algorithm
        Map<String, List<ShardingInPredicateValue>> paramsByTarget = shardingParams.stream()
                .map(param -> new AbstractMap.SimpleEntry<>(getTargetTable(tableName, shardingColumn, param, algorithm), param))
                .filter(entry -> null != entry.getKey())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        // Distribute to route units
        return routeContext.getRouteUnits().stream()
                .collect(Collectors.toMap(
                        routeUnit -> routeUnit,
                        routeUnit -> createRouteUnitParams(routeUnit, tableName, columnParams, shardingColumn, paramsByTarget)))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Distributes parameters using a standard (single-column) sharding strategy.
     *
     * <p>Process:
     * <ol>
     *   <li>Identifies the single sharding column and its values</li>
     *   <li>Applies the sharding algorithm to determine target tables for each value</li>
     *   <li>Groups values by their target table destinations</li>
     *   <li>Maps grouped values to corresponding route units</li>
     * </ol>
     *
     * @param tableName the logical table name
     * @param columnParams all column parameters from IN expressions
     * @param strategy the complex sharding strategy configuration
     * @return distributed parameters by route unit
     */
    private Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> distributeByComplex(
            final String tableName, final Map<String, List<ShardingInPredicateValue>> columnParams,
            final ComplexShardingStrategyConfiguration strategy) {

        Set<String> shardingColumns = parseColumns(strategy.getShardingColumns());
        ComplexKeysShardingAlgorithm<Comparable<?>> algorithm = getComplexAlgorithm(strategy);

        if (!columnParams.keySet().containsAll(shardingColumns) || null == algorithm) {
            return createDefaultDistribution(columnParams);
        }

        // Generate value combinations and group by target table
        Map<String, Map<String, Set<ShardingInPredicateValue>>> paramsByTarget = new HashMap<>();
        generateCombinations(shardingColumns, columnParams, new HashMap<>(), (combination) -> {
            getTargetTables(tableName, combination, algorithm).forEach(target ->
                    combination.forEach((col, param) ->
                            paramsByTarget.computeIfAbsent(target, k -> new HashMap<>())
                                    .computeIfAbsent(col, k -> new LinkedHashSet<>()).add(param)));
        });

        // Distribute to route units
        return routeContext.getRouteUnits().stream()
                .collect(Collectors.toMap(
                        routeUnit -> routeUnit,
                        routeUnit -> createComplexRouteUnitParams(routeUnit, tableName, columnParams, shardingColumns, paramsByTarget)))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Generates all possible combinations of values across multiple sharding columns.
     *
     * <p>This method creates a cartesian product of all column values and applies the provided
     * consumer to each combination. Used for complex sharding strategies where multiple columns
     * work together to determine the target shard.
     *
     * <p>Example: For columns {user_id: [1,2], tenant_id: [a,b]}, generates:
     * <ul>
     *   <li>{user_id: 1, tenant_id: a}</li>
     *   <li>{user_id: 1, tenant_id: b}</li>
     *   <li>{user_id: 2, tenant_id: a}</li>
     *   <li>{user_id: 2, tenant_id: b}</li>
     * </ul>
     *
     * @param columns the set of sharding columns to combine
     * @param columnParams the parameters available for each column
     * @param current the current combination being built (used for recursion)
     * @param consumer the callback to process each complete combination
     */
    private void generateCombinations(final Set<String> columns, final Map<String, List<ShardingInPredicateValue>> columnParams,
                                      final Map<String, ShardingInPredicateValue> current,
                                      final java.util.function.Consumer<Map<String, ShardingInPredicateValue>> consumer) {
        if (current.size() == columns.size()) {
            consumer.accept(new HashMap<>(current));
            return;
        }

        columns.stream().filter(col -> !current.containsKey(col)).findFirst().ifPresent(nextColumn -> columnParams.get(nextColumn).forEach(param -> {
            current.put(nextColumn, param);
            generateCombinations(columns, columnParams, current, consumer);
            current.remove(nextColumn);
        }));
    }

    /**
     * Creates default parameter distribution where all parameters are sent to all route units.
     *
     * <p>This fallback strategy is used when:
     * <ul>
     *   <li>No sharding strategy is configured for the table</li>
     *   <li>The sharding strategy cannot be applied to the available parameters</li>
     *   <li>Algorithm execution fails or returns unexpected results</li>
     * </ul>
     *
     * @param columnParams the original column parameters to distribute
     * @return mapping where every route unit gets all parameters
     */
    private Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> createDefaultDistribution(
            final Map<String, List<ShardingInPredicateValue>> columnParams) {
        return routeContext.getRouteUnits().stream()
                .collect(Collectors.toMap(routeUnit -> routeUnit, routeUnit -> new LinkedHashMap<>(columnParams)));
    }

    /**
     * Creates route-specific parameter mapping for standard sharding strategy.
     *
     * <p>This method maps the globally distributed parameters to a specific route unit
     * by looking up the actual table name and filtering the sharding column parameters
     * while preserving non-sharding column parameters.
     *
     * @param routeUnit the target route unit
     * @param tableName the logical table name
     * @param originalParams the original parameters for all columns
     * @param shardingColumn the column used for sharding decisions
     * @param paramsByTarget the parameters grouped by target table name
     * @return route-specific parameter mapping, or empty map if route is not applicable
     */
    private Map<String, List<ShardingInPredicateValue>> createRouteUnitParams(
            final RouteUnit routeUnit, final String tableName,
            final Map<String, List<ShardingInPredicateValue>> originalParams,
            final String shardingColumn, final Map<String, List<ShardingInPredicateValue>> paramsByTarget) {

        String actualTable = getActualTableName(routeUnit, tableName);
        if (null == actualTable) {
            return Collections.emptyMap();
        }

        Map<String, List<ShardingInPredicateValue>> result = new LinkedHashMap<>(originalParams);
        List<ShardingInPredicateValue> filtered = paramsByTarget.get(actualTable);
        if (null != filtered) {
            result.put(shardingColumn, filtered);
        }
        return result;
    }

    /**
     * Creates route-specific parameter mapping for complex sharding strategy.
     *
     * <p>This method handles multi-column sharding by:
     * <ol>
     *   <li>Looking up the actual table name for the route unit</li>
     *   <li>Retrieving filtered parameters for all sharding columns</li>
     *   <li>Adding back any non-sharding columns from the original parameters</li>
     * </ol>
     *
     * @param routeUnit the target route unit
     * @param tableName the logical table name
     * @param originalParams the original parameters for all columns
     * @param shardingColumns the set of columns used for sharding decisions
     * @param paramsByTarget the parameters grouped by target table and column
     * @return route-specific parameter mapping, or empty map if route is not applicable
     */
    private Map<String, List<ShardingInPredicateValue>> createComplexRouteUnitParams(
            final RouteUnit routeUnit, final String tableName,
            final Map<String, List<ShardingInPredicateValue>> originalParams,
            final Set<String> shardingColumns,
            final Map<String, Map<String, Set<ShardingInPredicateValue>>> paramsByTarget) {

        String actualTable = getActualTableName(routeUnit, tableName);
        if (null == actualTable) {
            return Collections.emptyMap();
        }

        Map<String, List<ShardingInPredicateValue>> result = new LinkedHashMap<>();
        Map<String, Set<ShardingInPredicateValue>> targetParams = paramsByTarget.get(actualTable);

        if (null != targetParams) {
            shardingColumns.forEach(col -> {
                Set<ShardingInPredicateValue> params = targetParams.get(col);
                if (null != params && !params.isEmpty()) {
                    result.put(col, new ArrayList<>(params));
                }
            });
        }

        // Add non-sharding columns back to the result
        originalParams.entrySet().stream()
                .filter(entry -> !shardingColumns.contains(entry.getKey()))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        return result;
    }

    /**
     * Determines the target table for a parameter value using standard sharding algorithm.
     *
     * <p>This method applies the sharding algorithm to a single parameter value to determine
     * which physical table it should be routed to. Handles exceptions gracefully by returning
     * null for values that cannot be processed.
     *
     * @param tableName the logical table name
     * @param column the sharding column name
     * @param param the parameter value to route
     * @param algorithm the standard sharding algorithm
     * @return the target table name, or null if routing fails
     */
    private String getTargetTable(final String tableName, final String column, final ShardingInPredicateValue param,
                                  final StandardShardingAlgorithm<Comparable<?>> algorithm) {
        try {
            return algorithm.doSharding(getAvailableTargets(tableName),
                    new PreciseShardingValue<>(tableName, column, new DataNodeInfo("", 1, (char) 0), param.getValue()));
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Determines target tables for a parameter combination using complex sharding algorithm.
     *
     * <p>This method applies the complex sharding algorithm to a combination of column values
     * to determine which physical tables the combination should be routed to. Returns a collection
     * because complex algorithms may route to multiple tables.
     *
     * @param tableName the logical table name
     * @param combination the combination of column-value pairs
     * @param algorithm the complex sharding algorithm
     * @return collection of target table names, or empty collection if routing fails
     */
    private Collection<String> getTargetTables(final String tableName, final Map<String, ShardingInPredicateValue> combination,
                                               final ComplexKeysShardingAlgorithm<Comparable<?>> algorithm) {
        try {
            Map<String, Collection<Comparable<?>>> shardingValues = combination.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> Collections.singleton(entry.getValue().getValue())));
            return algorithm.doSharding(getAvailableTargets(tableName),
                    new ComplexKeysShardingValue<>(tableName, shardingValues, Collections.emptyMap()));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    /**
     * Calculates the start and stop positions for token replacement in the original SQL.
     *
     * <p>The positions define the range in the original SQL that should be replaced with
     * the optimized token output. Calculated by finding the minimum start index and
     * maximum stop index across all IN expressions.
     *
     * @param expressions the collection of IN expressions to analyze
     * @return array containing [startIndex, stopIndex] for token replacement
     */
    private int[] calculatePositions(final Collection<InExpression> expressions) {
        return new int[]{
                expressions.stream().mapToInt(InExpression::getStartIndex).min().orElse(0),
                expressions.stream().mapToInt(InExpression::getStopIndex).max().orElse(0)
        };
    }

    // Utility methods for strategy and algorithm access

    /**
     * Retrieves the sharding strategy configuration for the specified table.
     *
     * @param tableName the logical table name
     * @return the sharding strategy configuration, or null if not found
     */
    private ShardingStrategyConfiguration getStrategy(final String tableName) {
        return shardingRule.findShardingTable(tableName).map(shardingRule::getTableShardingStrategyConfiguration).orElse(null);
    }

    /**
     * Retrieves and casts the standard sharding algorithm from the strategy configuration.
     *
     * @param strategy the standard sharding strategy configuration
     * @return the standard sharding algorithm, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    private StandardShardingAlgorithm<Comparable<?>> getStandardAlgorithm(final StandardShardingStrategyConfiguration strategy) {
        Object algorithm = shardingRule.getShardingAlgorithms().get(strategy.getShardingAlgorithmName());
        return algorithm instanceof StandardShardingAlgorithm ? (StandardShardingAlgorithm<Comparable<?>>) algorithm : null;
    }

    /**
     * Retrieves and casts the complex sharding algorithm from the strategy configuration.
     *
     * @param strategy the complex sharding strategy configuration
     * @return the complex sharding algorithm, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    private ComplexKeysShardingAlgorithm<Comparable<?>> getComplexAlgorithm(final ComplexShardingStrategyConfiguration strategy) {
        Object algorithm = shardingRule.getShardingAlgorithms().get(strategy.getShardingAlgorithmName());
        return algorithm instanceof ComplexKeysShardingAlgorithm ? (ComplexKeysShardingAlgorithm<Comparable<?>>) algorithm : null;
    }

    /**
     * Gets the available target table names for the specified logical table.
     *
     * @param tableName the logical table name
     * @return collection of actual table names that can be targeted
     */
    private Collection<String> getAvailableTargets(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(ShardingTable::getActualDataNodes)
                .map(nodes -> nodes.stream().map(DataNode::getTableName).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    /**
     * Retrieves the actual table name for the specified route unit and logical table.
     *
     * @param routeUnit the route unit containing table mappings
     * @param logicalTableName the logical table name to look up
     * @return the actual table name, or null if not found in the route unit
     */
    private String getActualTableName(final RouteUnit routeUnit, final String logicalTableName) {
        return routeUnit.getTableMappers().stream()
                .filter(mapper -> logicalTableName.equals(mapper.getLogicName()))
                .map(RouteMapper::getActualName)
                .findFirst().orElse(null);
    }

    /**
     * Parses a comma-separated column string into a set of column names.
     *
     * @param columnsStr the comma-separated column string
     * @return ordered set of trimmed column names
     */
    private Set<String> parseColumns(final String columnsStr) {
        return Arrays.stream(columnsStr.split(",")).map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Retrieves all sharding columns for the specified table based on its strategy.
     *
     * @param tableName the logical table name
     * @return set of sharding column names, or empty set if not a sharding table
     */
    private Set<String> getShardingColumns(final String tableName) {
        return shardingRule.findShardingTable(tableName)
                .map(shardingRule::getTableShardingStrategyConfiguration)
                .map(strategy -> {
                    if (strategy instanceof StandardShardingStrategyConfiguration) {
                        return Collections.singleton(((StandardShardingStrategyConfiguration) strategy).getShardingColumn());
                    } else if (strategy instanceof ComplexShardingStrategyConfiguration) {
                        return parseColumns(((ComplexShardingStrategyConfiguration) strategy).getShardingColumns());
                    }
                    return Collections.<String>emptySet();
                }).orElse(Collections.emptySet());
    }

    /**
     * Checks if the SELECT statement contains optimizable IN conditions on sharding columns.
     *
     * @param selectContext the SELECT statement context to analyze
     * @return true if optimizable conditions are found, false otherwise
     */
    private boolean hasOptimizableInConditions(final SelectStatementContext selectContext) {
        return extractShardingInExpressions(selectContext).values().stream().anyMatch(map -> !map.isEmpty());
    }

    /**
     * Extracts all IN expressions that can be optimized for sharding from the SELECT statement.
     *
     * <p>This method performs comprehensive analysis:
     * <ol>
     *   <li>Extracts all IN expressions from WHERE clauses</li>
     *   <li>Identifies the table name for each expression</li>
     *   <li>Filters to only sharding tables and sharding columns</li>
     *   <li>Checks if expressions are optimizable (multiple values)</li>
     *   <li>Groups results by table name for token creation</li>
     * </ol>
     *
     * @param selectContext the SELECT statement context to analyze
     * @return mapping of table names to their optimizable IN expressions by column
     */
    private Map<String, Map<String, InExpression>> extractShardingInExpressions(final SelectStatementContext selectContext) {
        Map<String, InExpression> allInExpressions = new LinkedHashMap<>();
        selectContext.getWhereSegments().forEach(segment -> extractInExpressions(segment.getExpr(), allInExpressions));

        return allInExpressions.entrySet().stream()
                .map(entry -> {
                    String tableName = getTableName(entry.getValue(), selectContext);
                    return tableName != null ?
                            new AbstractMap.SimpleEntry<>(tableName, entry) : null;
                })
                .filter(Objects::nonNull)
                .filter(entry -> shardingRule.isShardingTable(entry.getKey()))
                .filter(entry -> {
                    String tableName = entry.getKey();
                    String columnName = entry.getValue().getKey();
                    InExpression inExpression = entry.getValue().getValue();
                    return getShardingColumns(tableName).contains(columnName) && isOptimizable(inExpression);
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        LinkedHashMap::new,
                        Collectors.toMap(
                                entry -> entry.getValue().getKey(),
                                entry -> entry.getValue().getValue(),
                                (e1, e2) -> e1,
                                LinkedHashMap::new)));
    }

    /**
     * Recursively extracts IN expressions from expression segments.
     *
     * <p>Handles both direct IN expressions and nested binary operations that may contain
     * IN expressions (such as AND/OR combinations).
     *
     * @param expression the expression segment to analyze
     * @param result the map to collect IN expressions (column name -> IN expression)
     */
    private void extractInExpressions(final ExpressionSegment expression, final Map<String, InExpression> result) {
        if (expression instanceof InExpression) {
            InExpression inExpr = (InExpression) expression;
            if (inExpr.getLeft() instanceof ColumnSegment) {
                result.put(((ColumnSegment) inExpr.getLeft()).getIdentifier().getValue(), inExpr);
            }
        } else if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpr = (BinaryOperationExpression) expression;
            extractInExpressions(binaryExpr.getLeft(), result);
            extractInExpressions(binaryExpr.getRight(), result);
        }
    }

    /**
     * Determines if an IN expression is optimizable for sharding distribution.
     *
     * <p>An expression is optimizable when:
     * <ul>
     *   <li>It contains a ListExpression (IN clause with multiple values)</li>
     *   <li>The list has more than one item (single items are already optimal)</li>
     * </ul>
     *
     * @param inExpression the IN expression to evaluate
     * @return true if the expression can be optimized, false otherwise
     */
    private boolean isOptimizable(final InExpression inExpression) {
        return inExpression.getRight() instanceof ListExpression &&
                ((ListExpression) inExpression.getRight()).getItems().size() > 1;
    }

    /**
     * Extracts parameter information from column IN expressions.
     *
     * <p>Processes each column's IN expression to create a list of {@link ShardingInPredicateValue}
     * instances, filtering out columns with no valid parameters.
     *
     * @param columnExpressions mapping of column names to their IN expressions
     * @return mapping of column names to their parameter value lists
     */
    private Map<String, List<ShardingInPredicateValue>> extractColumnParameters(final Map<String, InExpression> columnExpressions) {
        return columnExpressions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> extractParameterInfos(entry.getValue())))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Extracts parameter information from a single IN expression.
     *
     * <p>Processes each item in the IN clause to create {@link ShardingInPredicateValue} instances:
     * <ul>
     *   <li>Literal values: Creates non-parameter instances with the literal value</li>
     *   <li>Parameter markers: Creates parameter instances with bound parameter values</li>
     * </ul>
     *
     * @param inExpression the IN expression to process
     * @return list of parameter values, or empty list if expression is not processable
     */
    private List<ShardingInPredicateValue> extractParameterInfos(final InExpression inExpression) {
        if (!(inExpression.getRight() instanceof ListExpression)) {
            return Collections.emptyList();
        }

        return ((ListExpression) inExpression.getRight()).getItems().stream()
                .map(item -> {
                    if (item instanceof LiteralExpressionSegment) {
                        Object literal = ((LiteralExpressionSegment) item).getLiterals();
                        return literal instanceof Comparable ?
                                new ShardingInPredicateValue(-1, (Comparable<?>) literal, false) : null;
                    } else if (item instanceof ParameterMarkerExpressionSegment) {
                        int index = ((ParameterMarkerExpressionSegment) item).getParameterMarkerIndex();
                        if (index >= 0 && index < parameters.size()) {
                            Object value = parameters.get(index);
                            return value instanceof Comparable ?
                                    new ShardingInPredicateValue(index, (Comparable<?>) value, true) : null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Determines the table name for an IN expression within the SELECT context.
     *
     * <p>Resolution strategy:
     * <ol>
     *   <li>If column has explicit table qualifier, use that</li>
     *   <li>Otherwise, find the first sharding table in the statement context</li>
     * </ol>
     *
     * @param inExpression the IN expression to analyze
     * @param selectContext the SELECT statement context
     * @return the resolved table name, or null if resolution fails
     */
    private String getTableName(final InExpression inExpression, final SelectStatementContext selectContext) {
        if (!(inExpression.getLeft() instanceof ColumnSegment)) {
            return null;
        }

        ColumnSegment column = (ColumnSegment) inExpression.getLeft();
        if (column.getOwner().isPresent()) {
            return column.getOwner().get().getIdentifier().getValue();
        }

        return selectContext.getTablesContext().getTableNames().stream()
                .filter(shardingRule::isShardingTable)
                .findFirst().orElse(null);
    }
}