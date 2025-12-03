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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingInPredicateTokenTest {
    
    @Test
    void assertBasicProperties() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(value));
        
        assertThat(token.getStartIndex(), is(10));
        assertThat(token.getStopIndex(), is(30));
        assertThat(token.getColumnName(), is("user_id"));
    }
    
    @Test
    void assertToStringWithSingleParameterValue() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(value));
        assertThat(token.toString(routeUnit), is("user_id = ?"));
    }
    
    @Test
    void assertToStringWithSingleLiteralValue() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(value));
        assertThat(token.toString(routeUnit), is("user_id = 1"));
    }
    
    @Test
    void assertToStringWithMultipleParameterValues() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 3, true, Collections.singleton(routeUnit));
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 5, true, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        assertThat(token.toString(routeUnit), is("user_id IN (?, ?, ?)"));
    }
    
    @Test
    void assertToStringWithMultipleLiteralValues() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, false, Collections.singleton(routeUnit));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 3, false, Collections.singleton(routeUnit));
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 5, false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        assertThat(token.toString(routeUnit), is("user_id IN (1, 3, 5)"));
    }
    
    @Test
    void assertToStringWithMixedValues() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, false, Collections.singleton(routeUnit));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 3, true, Collections.singleton(routeUnit));
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 5, false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        assertThat(token.toString(routeUnit), is("user_id IN (1, ?, 5)"));
    }
    
    @Test
    void assertToStringWithStringLiteralValue() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, "test", false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "name", Collections.singletonList(value));
        assertThat(token.toString(routeUnit), is("name = 'test'"));
    }
    
    @Test
    void assertToStringWithStringContainingQuotes() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, "test's", false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "name", Collections.singletonList(value));
        assertThat(token.toString(routeUnit), is("name = 'test''s'"));
    }
    
    @Test
    void assertToStringWithNullValue() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, null, false, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(value));
        assertThat(token.toString(routeUnit), is("user_id = NULL"));
    }
    
    @Test
    void assertToStringWithFilteredValuesByRoute() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit1));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 2, true, Collections.singleton(routeUnit2));
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 3, true, Collections.singleton(routeUnit1));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        
        // routeUnit1 should get values 1 and 3
        assertThat(token.toString(routeUnit1), is("user_id IN (?, ?)"));
        // routeUnit2 should get only value 2
        assertThat(token.toString(routeUnit2), is("user_id = ?"));
        // non-matching route should return empty
        RouteUnit routeUnit3 = createRouteUnit("ds_2", "t_order_2");
        assertThat(token.toString(routeUnit3), is(""));
    }
    
    @Test
    void assertToStringWithValuesForMultipleRoutes() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        Set<RouteUnit> bothRoutes = new HashSet<>(Arrays.asList(routeUnit1, routeUnit2));
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, bothRoutes);
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 2, true, Collections.singleton(routeUnit1));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2));
        
        assertThat(token.toString(routeUnit1), is("user_id IN (?, ?)"));
        assertThat(token.toString(routeUnit2), is("user_id = ?"));
    }
    
    @Test
    void assertGetRemovedParameterIndices() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit1));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 2, true, Collections.singleton(routeUnit2));
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 3, true, Collections.singleton(routeUnit1));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        
        // For routeUnit1: value2 (index 1) should be removed
        Set<Integer> removedForRoute1 = token.getRemovedParameterIndices(routeUnit1);
        assertThat(removedForRoute1.size(), is(1));
        assertTrue(removedForRoute1.contains(1));
        
        // For routeUnit2: value1 (index 0) and value3 (index 2) should be removed
        Set<Integer> removedForRoute2 = token.getRemovedParameterIndices(routeUnit2);
        assertThat(removedForRoute2.size(), is(2));
        assertTrue(removedForRoute2.contains(0));
        assertTrue(removedForRoute2.contains(2));
    }
    
    @Test
    void assertGetRemovedParameterIndicesWithOrphanParameters() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit));
        ShardingInPredicateValue value2 = ShardingInPredicateValue.createOrphan(1, 99, true);
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 3, true, Collections.singleton(routeUnit));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        
        Set<Integer> removedIndices = token.getRemovedParameterIndices(routeUnit);
        assertThat(removedIndices.size(), is(1));
        assertTrue(removedIndices.contains(1));
    }
    
    @Test
    void assertGetRemovedParameterIndicesIgnoresLiterals() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        ShardingInPredicateValue value1 = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit1));
        ShardingInPredicateValue value2 = new ShardingInPredicateValue(1, 2, false, Collections.singleton(routeUnit2)); // literal
        ShardingInPredicateValue value3 = new ShardingInPredicateValue(2, 3, true, Collections.singleton(routeUnit2));
        ShardingInPredicateToken token = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(value1, value2, value3));
        
        Set<Integer> removedIndices = token.getRemovedParameterIndices(routeUnit1);
        assertThat(removedIndices.size(), is(1));
        assertTrue(removedIndices.contains(2));
        // Literal (index 1) should not be in removed indices
        assertFalse(removedIndices.contains(1));
    }
    
    @Test
    void assertIsParameterFilterable() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        
        // With parameters - should be filterable
        ShardingInPredicateValue paramValue = new ShardingInPredicateValue(0, 1, true, Collections.singleton(routeUnit));
        ShardingInPredicateToken paramToken = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(paramValue));
        assertTrue(paramToken.isParameterFilterable());
        
        // Only literals - should not be filterable
        ShardingInPredicateValue literalValue = new ShardingInPredicateValue(0, 1, false, Collections.singleton(routeUnit));
        ShardingInPredicateToken literalToken = new ShardingInPredicateToken(10, 30, "user_id", Collections.singletonList(literalValue));
        assertFalse(literalToken.isParameterFilterable());
        
        // Mixed values - should be filterable (has at least one parameter)
        ShardingInPredicateToken mixedToken = new ShardingInPredicateToken(10, 30, "user_id", Arrays.asList(literalValue, paramValue));
        assertTrue(mixedToken.isParameterFilterable());
    }
    
    private RouteUnit createRouteUnit(final String dataSource, final String table) {
        return new RouteUnit(new RouteMapper(dataSource, dataSource), Collections.singletonList(new RouteMapper("t_order", table)));
    }
}
