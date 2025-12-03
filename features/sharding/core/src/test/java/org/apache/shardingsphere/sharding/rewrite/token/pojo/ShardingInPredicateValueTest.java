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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingInPredicateValueTest {
    
    @Test
    void assertConstructorAndGetters() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        Set<RouteUnit> targetRoutes = Collections.singleton(routeUnit);
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, targetRoutes);
        assertThat(value.getParameterIndex(), is(0));
        assertThat(value.getValue(), is(1));
        assertTrue(value.isParameter());
        assertThat(value.getTargetRoutes(), is(targetRoutes));
        assertFalse(value.isOrphan());
    }
    
    @Test
    void assertFullConstructor() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        Set<RouteUnit> targetRoutes = Collections.singleton(routeUnit);
        ShardingInPredicateValue value = new ShardingInPredicateValue(1, "test", false, targetRoutes, false);
        assertThat(value.getParameterIndex(), is(1));
        assertThat(value.getValue(), is("test"));
        assertFalse(value.isParameter());
        assertThat(value.getTargetRoutes(), is(targetRoutes));
        assertFalse(value.isOrphan());
    }
    
    @Test
    void assertCreateOrphan() {
        ShardingInPredicateValue orphan = ShardingInPredicateValue.createOrphan(2, 100, true);
        assertThat(orphan.getParameterIndex(), is(2));
        assertThat(orphan.getValue(), is(100));
        assertTrue(orphan.isParameter());
        assertTrue(orphan.getTargetRoutes().isEmpty());
        assertTrue(orphan.isOrphan());
    }
    
    @Test
    void assertBelongsToRouteWhenMatches() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        Set<RouteUnit> targetRoutes = Collections.singleton(routeUnit);
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, targetRoutes);
        assertTrue(value.belongsToRoute(routeUnit));
    }
    
    @Test
    void assertBelongsToRouteWhenNotMatches() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        Set<RouteUnit> targetRoutes = Collections.singleton(routeUnit1);
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, targetRoutes);
        assertFalse(value.belongsToRoute(routeUnit2));
    }
    
    @Test
    void assertBelongsToRouteWhenOrphan() {
        RouteUnit routeUnit = createRouteUnit("ds_0", "t_order_0");
        ShardingInPredicateValue orphan = ShardingInPredicateValue.createOrphan(0, 1, true);
        assertFalse(orphan.belongsToRoute(routeUnit));
    }
    
    @Test
    void assertBelongsToRouteWithMultipleTargets() {
        RouteUnit routeUnit1 = createRouteUnit("ds_0", "t_order_0");
        RouteUnit routeUnit2 = createRouteUnit("ds_1", "t_order_1");
        RouteUnit routeUnit3 = createRouteUnit("ds_2", "t_order_2");
        Set<RouteUnit> targetRoutes = new HashSet<>();
        targetRoutes.add(routeUnit1);
        targetRoutes.add(routeUnit2);
        ShardingInPredicateValue value = new ShardingInPredicateValue(0, 1, true, targetRoutes);
        assertTrue(value.belongsToRoute(routeUnit1));
        assertTrue(value.belongsToRoute(routeUnit2));
        assertFalse(value.belongsToRoute(routeUnit3));
    }
    
    private RouteUnit createRouteUnit(final String dataSource, final String table) {
        return new RouteUnit(new RouteMapper("ds", dataSource), Collections.singletonList(new RouteMapper("t_order", table)));
    }
}
