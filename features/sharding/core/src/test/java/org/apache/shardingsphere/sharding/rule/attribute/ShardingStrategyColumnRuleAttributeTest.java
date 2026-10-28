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

package org.apache.shardingsphere.sharding.rule.attribute;

import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingStrategyColumnRuleAttributeTest {
    
    @Test
    void assertIsShardingColumn() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.findShardingColumn("foo_col", "foo_tbl")).thenReturn(Optional.of("foo_col"));
        assertTrue(new ShardingStrategyColumnRuleAttribute(rule).isShardingColumn("foo_col", "foo_tbl"));
    }
    
    @Test
    void assertIsNotShardingColumn() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.findShardingColumn("bar_col", "foo_tbl")).thenReturn(Optional.empty());
        assertFalse(new ShardingStrategyColumnRuleAttribute(rule).isShardingColumn("bar_col", "foo_tbl"));
    }
}
