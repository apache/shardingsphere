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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test class for ShardingInPredicateToken with updated constructor and data structure.
 */
class ShardingInPredicateTokenTest {
    
    private ShardingInPredicateToken shardingInPredicateToken;

    private RouteUnit routeUnit1;

    private RouteUnit routeUnit2;
    
    @BeforeEach
    void setUp() {
        // Create route units
        routeUnit1 = createRouteUnit("t_order_0");
        routeUnit2 = createRouteUnit("t_order_1");
        
        // Create values with route distribution information
        List<ShardingInPredicateValue> values = Arrays.asList(
                createValueForRoute(-1, 1L, false, routeUnit2),
                createValueForRoute(-1, 2L, false, routeUnit1),
                createValueForRoute(-1, 3L, false, routeUnit2),
                createValueForRoute(-1, 4L, false, routeUnit1)
        );
        
        // Create token with new constructor
        shardingInPredicateToken = new ShardingInPredicateToken(10, 30, "order_id", values);
    }
    
    /**
     * Test toString method for route unit 1 (should contain values 2 and 4).
     */
    @Test
    void assertToStringWithOptimizedValues() {
        String result = shardingInPredicateToken.toString(routeUnit1);
        assertThat(result, is("order_id IN (2, 4)"));
    }
    
    /**
     * Test toString method for route unit 2 (should contain values 1 and 3).
     */
    @Test
    void assertToStringWithDifferentValues() {
        String result = shardingInPredicateToken.toString(routeUnit2);
        assertThat(result, is("order_id IN (1, 3)"));
    }
    
    /**
     * Test toString method when no values belong to the route unit.
     */
    @Test
    void assertToStringWithEmptyParameters() {
        // Create a token with no values for any route
        List<ShardingInPredicateValue> emptyValues = Collections.emptyList();
        ShardingInPredicateToken emptyToken = new ShardingInPredicateToken(10, 30, "order_id", emptyValues);
        
        String result = emptyToken.toString(routeUnit1);
        assertThat(result, is("order_id IN (NULL) AND 1 = 0"));
    }
    
    /**
     * Test single value scenario (should use = instead of IN).
     */
    @Test
    void assertToStringWithSingleValue() {
        List<ShardingInPredicateValue> singleValue = Collections.singletonList(
                createValueForRoute(-1, 100L, false, routeUnit1));
        
        ShardingInPredicateToken singleToken = new ShardingInPredicateToken(10, 30, "order_id", singleValue);
        String result = singleToken.toString(routeUnit1);
        
        assertThat(result, is("order_id = 100"));
    }
    
    /**
     * Test parameter marker handling.
     */
    @Test
    void assertToStringWithParameterMarkers() {
        List<ShardingInPredicateValue> parameterValues = Arrays.asList(
                createValueForRoute(0, 100L, true, routeUnit1),
                createValueForRoute(-1, 200L, false, routeUnit1)
        );
        
        ShardingInPredicateToken parameterToken = new ShardingInPredicateToken(10, 30, "order_id", parameterValues);
        String result = parameterToken.toString(routeUnit1);
        
        assertThat(result, is("order_id IN (?, 200)"));
    }
    
    /**
     * Test start and stop index getters.
     */
    @Test
    void assertGetStartAndStopIndex() {
        assertThat(shardingInPredicateToken.getStartIndex(), is(10));
        assertThat(shardingInPredicateToken.getStopIndex(), is(30));
    }
    
    /**
     * Test column name getter.
     */
    @Test
    void assertGetColumnName() {
        assertThat(shardingInPredicateToken.getColumnName(), is("order_id"));
    }
    
    /**
     * Test removed parameter indices calculation with orphan parameters.
     */
    @Test
    void assertGetRemovedParameterIndicesWithOrphanParameters() {
        List<ShardingInPredicateValue> mixedValues = Arrays.asList(
                createValueForRoute(0, 100L, true, routeUnit1),
                createValueForRoute(1, 200L, true, routeUnit2),
                ShardingInPredicateValue.createOrphan(2, 300L, true),
                createValueForRoute(3, 400L, true, routeUnit1)
        );
        
        ShardingInPredicateToken mixedToken = new ShardingInPredicateToken(10, 30, "order_id", mixedValues);
        Set<Integer> removedIndices = mixedToken.getRemovedParameterIndices(routeUnit1);
        
        // For route unit 1, parameters 1 and 2 should be removed (1 belongs to route 2, 2 is orphan)
        Set<Integer> expectedRemoved = new HashSet<>(Arrays.asList(1, 2));
        assertThat(removedIndices, is(expectedRemoved));
    }
    
    /**
     * Test parameter filterable check.
     */
    @Test
    void assertIsParameterFilterable() {
        // Token with parameter markers should be filterable
        assertThat(createTokenWithParameters().isParameterFilterable(), is(true));
        
        // Token with only literal values should not be filterable
        assertThat(createTokenWithLiteralsOnly().isParameterFilterable(), is(false));
    }
    
    /**
     * Test string value formatting with proper escaping.
     */
    @Test
    void assertToStringWithStringValues() {
        List<ShardingInPredicateValue> stringValues = Arrays.asList(
                createValueForRoute(-1, "test'value", false, routeUnit1),
                createValueForRoute(-1, "normal", false, routeUnit1));
        
        ShardingInPredicateToken stringToken = new ShardingInPredicateToken(10, 30, "name", stringValues);
        String result = stringToken.toString(routeUnit1);
        
        assertThat(result, is("name IN ('test''value', 'normal')"));
    }
    
    /**
     * Test null value formatting.
     */
    @Test
    void assertToStringWithNullValue() {
        List<ShardingInPredicateValue> nullValues = Collections.singletonList(
                createValueForRoute(-1, null, false, routeUnit1));
        
        ShardingInPredicateToken nullToken = new ShardingInPredicateToken(10, 30, "order_id", nullValues);
        String result = nullToken.toString(routeUnit1);
        
        assertThat(result, is("order_id = NULL"));
    }

    /**
     * Create a route unit with specified table name.
     *
     * @param actualTableName actual table name
     * @return route unit
     */
    private RouteUnit createRouteUnit(final String actualTableName) {
        RouteMapper dataSourceMapper = new RouteMapper("ds", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", actualTableName);
        return new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper));
    }
    
    /**
     * Create a ShardingInPredicateValue that belongs to specific route unit.
     *
     * @param parameterIndex parameter index
     * @param value value
     * @param isParameter is parameter
     * @param routeUnit route unit
     * @return sharding in predicate value
     */
    private ShardingInPredicateValue createValueForRoute(final int parameterIndex, final Comparable<?> value,
                                                         final boolean isParameter, final RouteUnit routeUnit) {
        Set<RouteUnit> targetRoutes = new HashSet<>(Collections.singletonList(routeUnit));
        return new ShardingInPredicateValue(parameterIndex, value, isParameter, targetRoutes);
    }
    
    /**
     * Create token with parameter markers for testing parameter filtering.
     *
     * @return token with parameter markers
     */
    private ShardingInPredicateToken createTokenWithParameters() {
        List<ShardingInPredicateValue> parameterValues = Collections.singletonList(
                createValueForRoute(0, 100L, true, routeUnit1));
        return new ShardingInPredicateToken(10, 30, "order_id", parameterValues);
    }
    
    /**
     * Create token with only literal values for testing parameter filtering.
     *
     * @return token with only literal values
     */
    private ShardingInPredicateToken createTokenWithLiteralsOnly() {
        List<ShardingInPredicateValue> literalValues = Collections.singletonList(
                createValueForRoute(-1, 100L, false, routeUnit1));
        return new ShardingInPredicateToken(10, 30, "order_id", literalValues);
    }
}
