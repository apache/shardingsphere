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

package org.apache.shardingsphere.core.rewrite.builder.parameter.group;

import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GroupedParameterBuilderTest {
    
    private GroupedParameterBuilder parameterBuilder;
    
    @Before
    public void setUp() {
        parameterBuilder = new GroupedParameterBuilder(Arrays.<Object>asList(1, 2), createGroupedParameters(), createShardingConditions());
    }
    
    private List<List<Object>> createGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        result.add(Arrays.<Object>asList(3, 4));
        result.add(Arrays.<Object>asList(5, 6));
        return result;
    }
    
    private ShardingConditions createShardingConditions() {
        ShardingCondition shardingCondition1 = new ShardingCondition();
        shardingCondition1.getDataNodes().add(new DataNode("db1.tb1"));
        ShardingCondition shardingCondition2 = new ShardingCondition();
        shardingCondition2.getDataNodes().add(new DataNode("db2.tb2"));
        return new ShardingConditions(Arrays.asList(shardingCondition1, shardingCondition2));
    }
    
    @Test
    public void assertGetParameters() {
        assertThat(parameterBuilder.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6)));
    }
    
    @Test
    public void assertGetParametersWithRoutingUnit() {
        RoutingUnit routingUnit = new RoutingUnit("db1");
        routingUnit.getTableUnits().add(new TableUnit("tb1", "tb1"));
        assertThat(parameterBuilder.getParameters(routingUnit), is(Arrays.<Object>asList(3, 4)));
    }
    
    @Test
    public void assertGetOriginalParameters() {
        assertThat(parameterBuilder.getOriginalParameters(), is(Arrays.<Object>asList(1, 2)));
    }
}
