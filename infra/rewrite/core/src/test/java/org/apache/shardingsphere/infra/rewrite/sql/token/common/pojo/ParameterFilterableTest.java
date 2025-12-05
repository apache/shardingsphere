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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link ParameterFilterable}.
 *
 * @author yinh
 */
class ParameterFilterableTest {
    
    @Test
    void assertDefaultIsParameterFilterableReturnsTrue() {
        ParameterFilterable filterable = new TestParameterFilterable();
        assertTrue(filterable.isParameterFilterable());
    }
    
    @Test
    void assertGetRemovedParameterIndicesCanBeImplemented() {
        RouteUnit routeUnit = createTestRouteUnit();
        ParameterFilterable filterable = new TestParameterFilterable();
        Set<Integer> removedIndices = filterable.getRemovedParameterIndices(routeUnit);
        assertThat(removedIndices.size(), is(2));
        assertTrue(removedIndices.contains(0));
        assertTrue(removedIndices.contains(1));
    }
    
    @Test
    void assertDefaultMethodCanBeOverridden() {
        ParameterFilterable filterable = new TestParameterFilterableWithOverride();
        assertThat(filterable.isParameterFilterable(), is(false));
    }
    
    private RouteUnit createTestRouteUnit() {
        RouteMapper dataSourceMapper = new RouteMapper("ds_0", "ds_0");
        RouteMapper tableMapper = new RouteMapper("t_order", "t_order_0");
        return new RouteUnit(dataSourceMapper, Collections.singletonList(tableMapper));
    }
    
    /**
     * Test implementation of ParameterFilterable.
     */
    private static class TestParameterFilterable implements ParameterFilterable {
        
        @Override
        public Set<Integer> getRemovedParameterIndices(final RouteUnit routeUnit) {
            Set<Integer> result = new HashSet<>();
            result.add(0);
            result.add(1);
            return result;
        }
    }
    
    /**
     * Test implementation with overridden default method.
     */
    private static class TestParameterFilterableWithOverride implements ParameterFilterable {
        
        @Override
        public Set<Integer> getRemovedParameterIndices(final RouteUnit routeUnit) {
            return Collections.emptySet();
        }
        
        @Override
        public boolean isParameterFilterable() {
            return false;
        }
    }
}
