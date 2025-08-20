package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a value within a sharding IN predicate, which can be either a parameter marker or a literal value.
 * This class encapsulates the information needed to identify and process values in IN clauses during SQL rewriting.
 *
 * <p>In ShardingSphere's SQL rewriting process, IN predicates need special handling for sharding optimization.
 * Each value in an IN clause can be either:
 * <ul>
 *   <li>A parameter marker (?): represented with parameterIndex and isParameter=true</li>
 *   <li>A literal value: represented with the actual value and isParameter=false</li>
 * </ul>
 *
 * <p>This distinction is crucial for:
 * <ul>
 *   <li>Parameter filtering during route-specific SQL generation</li>
 *   <li>Correct SQL syntax generation (with or without quotes)</li>
 *   <li>Maintaining parameter index mapping for prepared statements</li>
 * </ul>
 *
 * @author yinh
 * @see org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInPredicateToken
 * @see org.apache.shardingsphere.sharding.rewrite.token.generator.impl.ShardingInPredicateTokenGenerator
 */
@RequiredArgsConstructor
@Getter
public final class ShardingInPredicateValue {

    /**
     * The index of the parameter marker in the original prepared statement.
     * Only meaningful when {@link #isParameter} is true.
     */
    private final int parameterIndex;

    /**
     * The actual value of this predicate component.
     * For parameter markers, this represents the bound parameter value.
     * For literals, this represents the literal value from the SQL.
     * Must implement Comparable for sharding algorithm processing.
     */
    private final Comparable<?> value;

    /**
     * Indicates whether this value originates from a parameter marker (?) in the SQL.
     * When true, this value should be represented as "?" in rewritten SQL.
     * When false, this value should be formatted as a literal in rewritten SQL.
     */
    private final boolean isParameter;
}