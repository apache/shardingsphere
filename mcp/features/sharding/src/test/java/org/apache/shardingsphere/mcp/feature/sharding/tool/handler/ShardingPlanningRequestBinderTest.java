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

package org.apache.shardingsphere.mcp.feature.sharding.tool.handler;

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingPlanningRequestBinderTest {
    
    @Test
    void assertBindTableRule() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindTableRule(Map.of(
                "database", "logic_db",
                "structured_intent_evidence", Map.of("table", "t_order", "column", "order_id", "algorithm_type", "INLINE", "auditors", "evidence_auditor"),
                "algorithm_properties", Map.of("algorithm-expression", "t_order_${order_id % 2}"),
                "auditors", "dml_auditor"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getAlgorithmType(), is("INLINE"));
        assertThat(actual.getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "t_order_${order_id % 2}")));
        assertThat(actual.getAuditorNames(), is(List.of("dml_auditor")));
    }
    
    @Test
    void assertBindTableReferenceRule() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindTableReferenceRule(Map.of(
                "database", "logic_db", "rule", "ref_rule", "reference_tables", "t_order,t_order_item",
                "structured_intent_evidence", Map.of("reference_tables", "evidence_table")));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getRuleName(), is("ref_rule"));
        assertThat(actual.getReferenceTables(), is(List.of("t_order", "t_order_item")));
    }
    
    @Test
    void assertBindDefaultStrategy() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindDefaultStrategy(Map.of(
                "database", "logic_db", "default_strategy_type", "DATABASE", "structured_intent_evidence", Map.of("column", "order_id"),
                "algorithm_properties", Map.of("algorithm-expression", "ds_${order_id % 2}")));
        assertThat(actual.getDefaultStrategyType(), is("DATABASE"));
        assertThat(actual.getColumn(), is("order_id"));
        assertThat(actual.getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "ds_${order_id % 2}")));
    }
    
    @Test
    void assertBindKeyGenerator() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindKeyGenerator(Map.of(
                "database", "logic_db", "key_generator", "snowflake_generator",
                "key_generator_type", "SNOWFLAKE",
                "key_generator_properties", Map.of("worker-id", "1")));
        assertThat(actual.getKeyGeneratorName(), is("snowflake_generator"));
        assertThat(actual.getKeyGeneratorType(), is("SNOWFLAKE"));
        assertThat(actual.getKeyGeneratorProperties(), is(Map.of("worker-id", "1")));
    }
    
    @Test
    void assertBindKeyGenerateStrategy() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindKeyGenerateStrategy(Map.of(
                "database", "logic_db", "key_generate_strategy", "order_key_strategy", "structured_intent_evidence",
                Map.of("table", "t_order", "column", "id", "key_generator", "snowflake_generator")));
        assertThat(actual.getKeyGenerateStrategyName(), is("order_key_strategy"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getKeyGeneratorName(), is("snowflake_generator"));
    }
    
    @Test
    void assertBindSequenceKeyGenerateStrategy() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindKeyGenerateStrategy(Map.of(
                "database", "logic_db", "key_generate_strategy", "order_sequence_strategy", "sequence", "order_seq",
                "key_generator", "snowflake_generator"));
        assertThat(actual.getKeyGenerateStrategyName(), is("order_sequence_strategy"));
        assertThat(actual.getSequenceName(), is("order_seq"));
        assertThat(actual.getKeyGeneratorName(), is("snowflake_generator"));
    }
    
    @Test
    void assertBindRuleComponentCleanup() {
        ShardingWorkflowRequest actual = new ShardingPlanningRequestBinder().bindRuleComponentCleanup(Map.of(
                "database", "logic_db", "component_type", "algorithm", "component_name", "unused_algorithm"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getComponentType(), is("algorithm"));
        assertThat(actual.getComponentName(), is("unused_algorithm"));
    }
}
