package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.ParameterFilterable;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL rewrite token for optimizing sharding IN predicates across multiple columns and route units.
 *
 * <p>This token handles the complex rewriting of IN predicates in sharded environments where:
 * <ul>
 *   <li>Different route units (database shards) may need different subsets of IN values</li>
 *   <li>Multiple sharding columns may be involved in complex sharding strategies</li>
 *   <li>Both parameter markers and literal values need proper handling</li>
 *   <li>SQL syntax optimization (converting single-value IN to equality) is applied</li>
 * </ul>
 *
 * <p>The token supports both standard and complex sharding strategies:
 * <ul>
 *   <li><strong>Standard sharding</strong>: Single sharding column, values distributed by algorithm</li>
 *   <li><strong>Complex sharding</strong>: Multiple sharding columns, cartesian product distribution</li>
 * </ul>
 *
 * <p>Key optimization features:
 * <ul>
 *   <li>Route-specific value filtering: Only relevant values sent to each shard</li>
 *   <li>SQL syntax optimization: Single values converted from IN to equality conditions</li>
 *   <li>Parameter index management: Maintains correct parameter positions</li>
 *   <li>Empty clause handling: Generates "IN (NULL) AND 1 = 0" for impossible conditions</li>
 * </ul>
 *
 * @author yinh
 * @see org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingInPredicateTokenGenerator
 * @see org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateValue
 */
public final class ShardingInPredicateToken extends SQLToken implements Substitutable, RouteUnitAware, ParameterFilterable {

    @Getter
    private final int stopIndex;

    /**
     * Maps route units to their respective column parameters for optimized IN predicate rewriting.
     *
     * <p>Structure: RouteUnit -> ColumnName -> List of Values
     * <p>This nested mapping enables:
     * <ul>
     *   <li>Route-specific value filtering: Each route unit only gets relevant values</li>
     *   <li>Multi-column support: Complex sharding strategies with multiple columns</li>
     *   <li>Efficient lookup: Direct access to values for specific route and column</li>
     * </ul>
     *
     * <p>Example for a query "WHERE user_id IN (1,2,3) AND tenant_id IN ('a','b')":
     * <pre>
     * RouteUnit1 -> {
     *   "user_id" -> [Value(1), Value(3)],
     *   "tenant_id" -> [Value('a')]
     * }
     * RouteUnit2 -> {
     *   "user_id" -> [Value(2)],
     *   "tenant_id" -> [Value('b')]
     * }
     * </pre>
     */
    private final Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> columnParameterMap;

    /**
     * Constructs a new sharding IN predicate token with distributed parameters.
     *
     * @param startIndex the start position of the original IN predicate in the SQL
     * @param stopIndex the end position of the original IN predicate in the SQL
     * @param columnParameterMap the distributed parameters mapped by route unit and column name
     */
    public ShardingInPredicateToken(final int startIndex,
                                    final int stopIndex,
                                    final Map<RouteUnit, Map<String, List<ShardingInPredicateValue>>> columnParameterMap) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.columnParameterMap = columnParameterMap;
    }

    /**
     * Generates the optimized SQL fragment for the specified route unit.
     *
     * <p>This method creates route-specific IN clauses by:
     * <ul>
     *   <li>Retrieving only the values relevant to the given route unit</li>
     *   <li>Building separate IN clauses for each column</li>
     *   <li>Optimizing single-value IN clauses to equality conditions</li>
     *   <li>Handling empty conditions with "IN (NULL) AND 1 = 0"</li>
     *   <li>Combining multiple column conditions with AND</li>
     * </ul>
     *
     * @param routeUnit the target route unit (database shard) for SQL generation
     * @return the optimized SQL fragment, or empty string if no conditions apply
     */
    @Override
    public String toString(final RouteUnit routeUnit) {
        Map<String, List<ShardingInPredicateValue>> routeUnitParams = columnParameterMap.get(routeUnit);
        if (routeUnitParams == null || routeUnitParams.isEmpty()) {
            return "";
        }

        return buildOptimizedClauses(routeUnitParams);
    }

    /**
     * Identifies parameter indices that should be removed for the specified route unit.
     *
     * <p>In sharded environments, each route unit only needs a subset of the original parameters.
     * This method calculates which parameter indices from the original SQL should be filtered out
     * for the given route unit, enabling proper parameter binding in prepared statements.
     *
     * <p>The calculation process:
     * <ol>
     *   <li>Collect all parameter indices from all route units</li>
     *   <li>Identify which parameter indices are kept for the specified route unit</li>
     *   <li>Return the difference as indices to be removed</li>
     * </ol>
     *
     * @param routeUnit the route unit to calculate removed parameters for
     * @return a set of parameter indices that should be removed for this route unit
     */
    @Override
    public Set<Integer> getRemovedParameterIndices(final RouteUnit routeUnit) {
        Map<String, List<ShardingInPredicateValue>> routeUnitParams = columnParameterMap.get(routeUnit);
        if (routeUnitParams == null) {
            return Collections.emptySet();
        }

        Set<Integer> allIndices = getAllParameterIndices();
        Set<Integer> keptIndices = getKeptParameterIndices(routeUnitParams);
        Set<Integer> result = new HashSet<>(allIndices);
        result.removeAll(keptIndices);
        return result;
    }

    /**
     * Determines if this token involves parameter filtering.
     *
     * <p>Returns true if any of the predicate values are parameter markers (?),
     * indicating that this token will affect parameter binding and requires
     * parameter index management during SQL rewriting.
     *
     * @return true if parameter filtering is needed, false otherwise
     */
    @Override
    public boolean isParameterFilterable() {
        return columnParameterMap.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(List::stream)
                .anyMatch(ShardingInPredicateValue::isParameter);
    }

    /**
     * Collects all parameter indices from across all route units and columns.
     *
     * @return set of all parameter indices that appear in any route unit
     */
    private Set<Integer> getAllParameterIndices() {
        return columnParameterMap.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(List::stream)
                .filter(ShardingInPredicateValue::isParameter)
                .map(ShardingInPredicateValue::getParameterIndex)
                .collect(Collectors.toSet());
    }

    /**
     * Collects parameter indices that should be kept for the specified parameter map.
     *
     * @param parameterMap the parameter map for a specific route unit
     * @return set of parameter indices that should be retained
     */
    private Set<Integer> getKeptParameterIndices(Map<String, List<ShardingInPredicateValue>> parameterMap) {
        return parameterMap.values().stream()
                .flatMap(List::stream)
                .filter(ShardingInPredicateValue::isParameter)
                .map(ShardingInPredicateValue::getParameterIndex)
                .collect(Collectors.toSet());
    }

    /**
     * Builds optimized WHERE clauses by combining multiple column conditions.
     *
     * <p>Each column with non-empty values gets its own IN clause (or equality condition),
     * and all column conditions are combined with AND operators.
     *
     * @param parameterMap mapping of column names to their respective values
     * @return combined SQL conditions, or empty string if no valid conditions
     */
    private String buildOptimizedClauses(Map<String, List<ShardingInPredicateValue>> parameterMap) {
        return parameterMap.entrySet().stream()
                .map(entry -> buildInClause(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" AND "));
    }

    /**
     * Builds an optimized IN clause for a specific column and its values.
     *
     * <p>Optimization logic:
     * <ul>
     *   <li>Empty list: "column IN (NULL) AND 1 = 0" (impossible condition)</li>
     *   <li>Single value: "column = value" (equality is more efficient than IN)</li>
     *   <li>Multiple values: "column IN (value1, value2, ...)" (standard IN clause)</li>
     * </ul>
     *
     * @param column the column name for the condition
     * @param infos the list of values for this column
     * @return the optimized SQL condition for this column
     */
    private String buildInClause(final String column, final List<ShardingInPredicateValue> infos) {
        if (infos.isEmpty()) {
            return column + " IN (NULL) AND 1 = 0";
        }
        if (infos.size() == 1) {
            ShardingInPredicateValue single = infos.get(0);
            return String.format("%s = %s", column,
                    single.isParameter() ? "?" : formatValue(single.getValue()));
        }
        String values = infos.stream()
                .map(info -> info.isParameter() ? "?" : formatValue(info.getValue()))
                .collect(Collectors.joining(", "));
        return String.format("%s IN (%s)", column, values);
    }

    /**
     * Formats a literal value for SQL generation with proper quoting and escaping.
     *
     * <p>Formatting rules:
     * <ul>
     *   <li>null values: "NULL"</li>
     *   <li>String values: Single-quoted with internal quotes escaped</li>
     *   <li>Other types: toString() representation</li>
     * </ul>
     *
     * @param value the value to format
     * @return the SQL-safe string representation of the value
     */
    private String formatValue(final Comparable<?> value) {
        if (null == value) {
            return "NULL";
        }
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        return value.toString();
    }
}