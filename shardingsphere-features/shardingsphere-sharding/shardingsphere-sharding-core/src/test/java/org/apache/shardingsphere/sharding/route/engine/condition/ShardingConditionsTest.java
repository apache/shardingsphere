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

package org.apache.shardingsphere.sharding.route.engine.condition;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class ShardingConditionsTest {
    
    @Test
    public void assertIsAlwaysFalse() {
        ShardingConditions shardingConditions = new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class));
        assertFalse(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertIsAlwaysFalseTrue() {
        ShardingConditions shardingConditions = createSingleShardingConditions();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertIsNeedMerge() {
        assertFalse(createSingleShardingConditions().isNeedMerge());
    }
    
    @Test
    public void isSameShardingConditionTrue() {
        ShardingConditions shardingConditions = createSingleShardingConditions();
        assertTrue(shardingConditions.isSameShardingCondition());
    }
    
    @Test
    public void isSameShardingConditionFalse() {
        ShardingConditions shardingConditions = createMultipleShardingConditions();
        assertFalse(shardingConditions.isSameShardingCondition());
    }
    
    @Test
    public void assertMerge() {
        ShardingConditions multipleShardingConditions = createMultipleShardingConditions();
        multipleShardingConditions.merge();
        assertThat(multipleShardingConditions.getConditions().size(), is(2));
    }
    
    private ShardingConditions createSingleShardingConditions() {
        List<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue = new ListShardingConditionValue<>("order_id", "t_order", Collections.singleton(1L));
        AlwaysFalseShardingCondition shardingCondition = new AlwaysFalseShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue);
        result.add(shardingCondition);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        return new ShardingConditions(result, sqlStatementContext, mock(ShardingRule.class));
    }
    
    private ShardingConditions createMultipleShardingConditions() {
        List<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", "t_order", Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", "t_order", Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        ShardingCondition shardingCondition2 = new ShardingCondition();
        shardingCondition2.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        result.add(shardingCondition2);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        return new ShardingConditions(result, sqlStatementContext, mock(ShardingRule.class));
    }
}
