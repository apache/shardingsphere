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
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConditionsTest {

    @Mock
    private List<ShardingCondition> conditions;

    private ShardingConditions shardingConditions = new ShardingConditions(conditions, mock(SQLStatementContext.class), mock(ShardingRule.class));

    @Test
    public void assertIsAlwaysFalse() {
        assertFalse(shardingConditions.isAlwaysFalse());
        when(shardingConditions.isAlwaysFalse()).thenReturn(true);
        assertTrue(shardingConditions.isAlwaysFalse());
    }

    @Test
    public void assertIsNeedMerge() {
        assertTrue(shardingConditions.isNeedMerge());
    }

    @Test
    public void isSameShardingCondition() {
        assertFalse(shardingConditions.isSameShardingCondition());
    }

    @Test
    public void assertMerge() {
        createShardingConditions("t_order").merge();
    }

    private ShardingConditions createShardingConditions(final String tableName) {
        LinkedList<ShardingCondition> result = new LinkedList<>();
        ShardingConditionValue shardingConditionValue = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue);
        result.add(shardingCondition);
        return new ShardingConditions(result, mock(SQLStatementContext.class), mock(ShardingRule.class));
    }
}
