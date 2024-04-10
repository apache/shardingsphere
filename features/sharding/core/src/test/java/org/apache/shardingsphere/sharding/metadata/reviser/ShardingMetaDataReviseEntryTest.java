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

package org.apache.shardingsphere.sharding.metadata.reviser;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.metadata.reviser.column.ShardingColumnGeneratedReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.constraint.ShardingConstraintReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.index.ShardingIndexReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.schema.ShardingSchemaTableAggregationReviser;
import org.apache.shardingsphere.sharding.metadata.reviser.table.ShardingTableNameReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingMetaDataReviseEntryTest {
    
    private ShardingMetaDataReviseEntry reviseEntry;
    
    private ShardingRule shardingRule;
    
    @BeforeEach
    void setUp() {
        reviseEntry = new ShardingMetaDataReviseEntry();
        shardingRule = createShardingRule();
    }
    
    @Test
    void assertGetIndexReviser() {
        Optional<ShardingIndexReviser> actual = reviseEntry.getIndexReviser(shardingRule, "t_order0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getClass(), is(ShardingIndexReviser.class));
    }
    
    @Test
    void assertGetColumnGeneratedReviser() {
        Optional<ShardingColumnGeneratedReviser> actual = reviseEntry.getColumnGeneratedReviser(shardingRule, "t_order0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getClass(), is(ShardingColumnGeneratedReviser.class));
    }
    
    @Test
    void assertGetConstraintReviser() {
        Optional<ShardingConstraintReviser> actual = reviseEntry.getConstraintReviser(shardingRule, "t_order1");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getClass(), is(ShardingConstraintReviser.class));
    }
    
    @Test
    void assertGetTableNameReviser() {
        Optional<ShardingTableNameReviser> actual = reviseEntry.getTableNameReviser();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getClass(), is(ShardingTableNameReviser.class));
    }
    
    @Test
    void assertGetSchemaTableAggregationReviser() {
        Optional<ShardingSchemaTableAggregationReviser> actual = reviseEntry.getSchemaTableAggregationReviser(new ConfigurationProperties(null));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getClass(), is(ShardingSchemaTableAggregationReviser.class));
    }
    
    private ShardingRule createShardingRule() {
        InstanceContext instanceContext = mock(InstanceContext.class);
        when(instanceContext.getWorkerId()).thenReturn(0);
        return new ShardingRule(createShardingRuleConfiguration(), Collections.singletonMap("ds", new MockedDataSource()), instanceContext);
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds.t_order${0..1}"));
        return result;
    }
}
