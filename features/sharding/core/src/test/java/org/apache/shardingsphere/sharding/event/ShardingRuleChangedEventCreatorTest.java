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

package org.apache.shardingsphere.sharding.event;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.AlterShardingAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.AlterKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.AlterShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.cache.AlterShardingCacheEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.database.AlterDefaultDatabaseShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.table.auto.AlterShardingAutoTableEvent;
import org.apache.shardingsphere.sharding.event.table.binding.AlterShardingTableReferenceEvent;
import org.apache.shardingsphere.sharding.event.table.sharding.AlterShardingTableEvent;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShardingRuleChangedEventCreatorTest {
    
    private ShardingRuleChangedEventCreator shardingRuleChangedEventCreator;
    
    private DataChangedEvent dataChangedEvent;
    
    @BeforeEach
    void setUp() {
        shardingRuleChangedEventCreator = new ShardingRuleChangedEventCreator();
        dataChangedEvent = mock(DataChangedEvent.class);
        when(dataChangedEvent.getType()).thenReturn(Type.ADDED);
    }
    
    @Test
    void assertAlterShardingTableEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.TABLES);
        assertTrue(governanceEvent instanceof AlterShardingTableEvent);
    }
    
    @Test
    void assertAlterShardingAutoTableEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.AUTO_TABLES);
        assertTrue(governanceEvent instanceof AlterShardingAutoTableEvent);
    }
    
    @Test
    void assertAlterShardingTableReferenceEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.BINDING_TABLES);
        assertTrue(governanceEvent instanceof AlterShardingTableReferenceEvent);
    }
    
    @Test
    void assertAlterShardingAlgorithmEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.ALGORITHMS);
        assertTrue(governanceEvent instanceof AlterShardingAlgorithmEvent);
    }
    
    @Test
    void assertAlterKeyGeneratorEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.KEY_GENERATORS);
        assertTrue(governanceEvent instanceof AlterKeyGeneratorEvent);
    }
    
    @Test
    void assertAlterShardingAuditorEvent() {
        GovernanceEvent governanceEvent = createGovernanceEventWithEventName(ShardingRuleNodePathProvider.AUDITORS);
        assertTrue(governanceEvent instanceof AlterShardingAuditorEvent);
    }
    
    @Test
    void assertAlterDefaultDatabaseShardingStrategyEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY);
        assertTrue(governanceEvent instanceof AlterDefaultDatabaseShardingStrategyEvent);
    }
    
    @Test
    void assertAlterDefaultTableShardingStrategyEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY);
        assertTrue(governanceEvent instanceof AlterDefaultTableShardingStrategyEvent);
    }
    
    @Test
    void assertAlterDefaultKeyGenerateStrategyEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY);
        assertTrue(governanceEvent instanceof AlterDefaultKeyGenerateStrategyEvent);
    }
    
    @Test
    void assertAlterDefaultShardingAuditorStrategyEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY);
        assertTrue(governanceEvent instanceof AlterDefaultShardingAuditorStrategyEvent);
    }
    
    @Test
    void assertAlterDefaultShardingColumnEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN);
        assertTrue(governanceEvent instanceof AlterDefaultShardingColumnEvent);
    }
    
    @Test
    void assertAlterShardingCacheEvent() {
        GovernanceEvent governanceEvent = createGovernanceEvent(ShardingRuleNodePathProvider.SHARDING_CACHE);
        assertTrue(governanceEvent instanceof AlterShardingCacheEvent);
    }
    
    @Test
    void assertUnsupportedOperationExceptionWhenIncorrectItemType() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> createGovernanceEventWithEventName("unsupported_item_type"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> createGovernanceEvent("unsupported_item_type"));
    }
    
    private GovernanceEvent createGovernanceEvent(final String itemType) {
        return shardingRuleChangedEventCreator.create("test_db", dataChangedEvent, itemType);
    }
    
    private GovernanceEvent createGovernanceEventWithEventName(final String itemType) {
        return shardingRuleChangedEventCreator.create("test_db", dataChangedEvent, itemType, "test_item_name");
    }
}
