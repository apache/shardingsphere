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

package org.apache.shardingsphere.sharding.metadata.converter;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingNodeConverterTest {
    
    @Test
    void assertGetTableNamePath() {
        assertThat(ShardingNodeConverter.getTableNamePath("foo_table"), is("tables/foo_table"));
    }
    
    @Test
    void assertGetAutoTableNamePath() {
        assertThat(ShardingNodeConverter.getAutoTableNamePath("foo_auto_table"), is("auto_tables/foo_auto_table"));
    }
    
    @Test
    void assertGetBindingTableNamePath() {
        assertThat(ShardingNodeConverter.getBindingTableNamePath("foo_binding_table"), is("binding_tables/foo_binding_table"));
    }
    
    @Test
    void assertGetBroadcastTableNamePath() {
        assertThat(ShardingNodeConverter.getBroadcastTablesPath(), is("broadcast_tables"));
    }
    
    @Test
    void assertGetDefaultDatabaseStrategyPath() {
        assertThat(ShardingNodeConverter.getDefaultDatabaseStrategyPath(), is("default_strategies/default_database_strategy"));
    }
    
    @Test
    void assertGetDefaultTableStrategyPath() {
        assertThat(ShardingNodeConverter.getDefaultTableStrategyPath(), is("default_strategies/default_table_strategy"));
    }
    
    @Test
    void assertGetDefaultKeyGenerateStrategyPath() {
        assertThat(ShardingNodeConverter.getDefaultKeyGenerateStrategyPath(), is("default_strategies/default_key_generate_strategy"));
    }
    
    @Test
    void assertGetDefaultAuditStrategyPath() {
        assertThat(ShardingNodeConverter.getDefaultAuditStrategyPath(), is("default_strategies/default_audit_strategy"));
    }
    
    @Test
    void assertGetDefaultShardingColumnPath() {
        assertThat(ShardingNodeConverter.getDefaultShardingColumnPath(), is("default_strategies/default_sharding_column"));
    }
    
    @Test
    void assertGetShardingAlgorithmPath() {
        assertThat(ShardingNodeConverter.getShardingAlgorithmPath("MOD"), is("algorithms/MOD"));
    }
    
    @Test
    void assertGetKeyGeneratorPath() {
        assertThat(ShardingNodeConverter.getKeyGeneratorPath("DEFAULT"), is("key_generators/DEFAULT"));
    }
    
    @Test
    void assertGetAuditorPath() {
        assertThat(ShardingNodeConverter.getAuditorPath("DML_SHARDING_CONDITIONS"), is("auditors/DML_SHARDING_CONDITIONS"));
    }
    
    @Test
    void assertGetShardingCachePath() {
        assertThat(ShardingNodeConverter.getShardingCachePath(), is("sharding_cache"));
    }
    
    @Test
    void assertCheckIsTargetRuleByRulePath() {
        assertTrue(ShardingNodeConverter.isShardingPath("/metadata/foo_db/rules/sharding/tables/foo_table"));
        assertFalse(ShardingNodeConverter.isShardingPath("/metadata/foo_db/rules/foo/tables/foo_table"));
        assertTrue(ShardingNodeConverter.isTablePath("/metadata/foo_db/rules/sharding/tables/foo_table"));
        assertFalse(ShardingNodeConverter.isTablePath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isAutoTablePath("/metadata/foo_db/rules/sharding/auto_tables/foo_table"));
        assertFalse(ShardingNodeConverter.isAutoTablePath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isBindingTablePath("/metadata/foo_db/rules/sharding/binding_tables/foo_table"));
        assertFalse(ShardingNodeConverter.isBindingTablePath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isBroadcastTablePath("/metadata/foo_db/rules/sharding/broadcast_tables/foo_table"));
        assertFalse(ShardingNodeConverter.isBroadcastTablePath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isDefaultDatabaseStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_database_strategy"));
        assertFalse(ShardingNodeConverter.isDefaultDatabaseStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_database_strategy/foo"));
        assertTrue(ShardingNodeConverter.isDefaultTableStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_table_strategy"));
        assertFalse(ShardingNodeConverter.isDefaultTableStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_table_strategy/foo"));
        assertTrue(ShardingNodeConverter.isDefaultKeyGenerateStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_key_generate_strategy"));
        assertFalse(ShardingNodeConverter.isDefaultKeyGenerateStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_key_generate_strategy/foo"));
        assertTrue(ShardingNodeConverter.isDefaultAuditStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_audit_strategy"));
        assertFalse(ShardingNodeConverter.isDefaultAuditStrategyPath("/metadata/foo_db/rules/sharding/default_strategies/default_audit_strategy/foo"));
        assertTrue(ShardingNodeConverter.isDefaultShardingColumnPath("/metadata/foo_db/rules/sharding/default_strategies/default_sharding_column"));
        assertFalse(ShardingNodeConverter.isDefaultShardingColumnPath("/metadata/foo_db/rules/sharding/default_strategies/default_sharding_column/foo"));
        assertTrue(ShardingNodeConverter.isShardingAlgorithmPath("/metadata/foo_db/rules/sharding/algorithms/foo_table"));
        assertFalse(ShardingNodeConverter.isShardingAlgorithmPath("/metadata/foo_db/rules/sharding/key_generators/foo"));
        assertTrue(ShardingNodeConverter.isKeyGeneratorPath("/metadata/foo_db/rules/sharding/key_generators/foo"));
        assertFalse(ShardingNodeConverter.isKeyGeneratorPath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isAuditorPath("/metadata/foo_db/rules/sharding/auditors/foo"));
        assertFalse(ShardingNodeConverter.isAuditorPath("/metadata/foo_db/rules/sharding/algorithms/MD5"));
        assertTrue(ShardingNodeConverter.isShardingCachePath("/metadata/foo_db/rules/sharding/sharding_cache"));
        assertFalse(ShardingNodeConverter.isShardingCachePath("/metadata/foo_db/rules/sharding/sharding_cache/foo"));
    }
    
    @Test
    void assertGetTableNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getTableName("/metadata/foo_db/rules/sharding/tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetAutoTableNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getAutoTableName("/metadata/foo_db/rules/sharding/auto_tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetBindingTableNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getBindingTableName("/metadata/foo_db/rules/sharding/binding_tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetBroadcastTableNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getBroadcastTableName("/metadata/foo_db/rules/sharding/broadcast_tables/foo_table");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetAlgorithmNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getShardingAlgorithmName("/metadata/foo_db/rules/sharding/algorithms/foo");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo"));
    }
    
    @Test
    void assertGetKeyGeneratorNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getKeyGeneratorName("/metadata/foo_db/rules/sharding/key_generators/foo");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo"));
    }
    
    @Test
    void assertGetAuditorNameByRulePath() {
        Optional<String> actual = ShardingNodeConverter.getAuditorName("/metadata/foo_db/rules/sharding/auditors/foo");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo"));
    }
}
