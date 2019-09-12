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

package org.apache.shardingsphere.core.rewrite.builder;

import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.DataNode;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertParameterBuilderTest {
    
    private InsertParameterBuilder insertParameterBuilder;
    
    @Before
    public void setUp() {
        insertParameterBuilder = new InsertParameterBuilder(Arrays.<Object>asList(1, 2), createInsertOptimizedStatement());
    }
    
    private InsertOptimizedStatement createInsertOptimizedStatement() {
        InsertValue insertValue1 = mock(InsertValue.class);
        when(insertValue1.getParameters()).thenReturn(Arrays.<Object>asList(3, 4));
        InsertValue insertValue2 = mock(InsertValue.class);
        when(insertValue2.getParameters()).thenReturn(Arrays.<Object>asList(5, 6));
        ShardingInsertOptimizedStatement result = mock(ShardingInsertOptimizedStatement.class);
        when(result.getInsertValues()).thenReturn(Arrays.asList(insertValue1, insertValue2));
        ShardingCondition shardingCondition1 = new ShardingCondition();
        shardingCondition1.getDataNodes().add(new DataNode("db1.tb1"));
        ShardingCondition shardingCondition2 = new ShardingCondition();
        shardingCondition2.getDataNodes().add(new DataNode("db2.tb2"));
        ShardingConditions shardingConditions = new ShardingConditions(Arrays.asList(shardingCondition1, shardingCondition2));
        when(result.getShardingConditions()).thenReturn(shardingConditions);
        return result;
    }
    
    @Test
    public void assertGetParameters() {
        assertThat(insertParameterBuilder.getParameters(), is(Arrays.<Object>asList(3, 4, 5, 6)));
    }
    
    @Test
    public void assertGetParametersWithRoutingUnit() {
        RoutingUnit routingUnit = new RoutingUnit("db1");
        routingUnit.getTableUnits().add(new TableUnit("tb1", "tb1"));
        assertThat(insertParameterBuilder.getParameters(routingUnit), is(Arrays.<Object>asList(3, 4)));
    }
    
    @Test
    public void assertGetOriginalParameters() {
        assertThat(insertParameterBuilder.getOriginalParameters(), is(Arrays.<Object>asList(1, 2)));
    }
    
    @Test
    public void assertGetInsertParameterUnits() {
        assertThat(insertParameterBuilder.getInsertParameterUnits().size(), is(2));
    }
}
