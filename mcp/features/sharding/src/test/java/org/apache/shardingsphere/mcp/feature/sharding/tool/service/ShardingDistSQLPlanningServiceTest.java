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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShardingDistSQLPlanningServiceTest {
    
    @Test
    void assertPlanTableRuleCreate() {
        RuleArtifact actual = new ShardingDistSQLPlanningService().planTableRule(createTableRuleRequest(), "create");
        assertThat(actual.getSql(), is("CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                + "TABLE_STRATEGY(TYPE='standard', SHARDING_COLUMN=`order_id`, "
                + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))), "
                + "KEY_GENERATE_STRATEGY(COLUMN=`id`, TYPE(NAME='snowflake')))"));
        assertNoForbiddenArtifacts(actual.getSql());
    }
    
    @Test
    void assertPlanTableRuleCreateFormatsReservedIdentifiers() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setTable("table");
        request.setColumn("from");
        request.setKeyGenerateColumn("key");
        RuleArtifact actual = new ShardingDistSQLPlanningService().planTableRule(request, "create");
        assertThat(actual.getSql(), is("CREATE SHARDING TABLE RULE `table`(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                + "TABLE_STRATEGY(TYPE='standard', SHARDING_COLUMN=`from`, "
                + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))), "
                + "KEY_GENERATE_STRATEGY(COLUMN=`key`, TYPE(NAME='snowflake')))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithComplexStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("complex");
        request.setShardingColumns("order_id, user_id");
        assertThat(new ShardingDistSQLPlanningService().planTableRule(request, "create").getSql(),
                is("CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                        + "TABLE_STRATEGY(TYPE='complex', SHARDING_COLUMNS=`order_id`, `user_id`, "
                        + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))), "
                        + "KEY_GENERATE_STRATEGY(COLUMN=`id`, TYPE(NAME='snowflake')))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithHintStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("hint");
        assertThat(new ShardingDistSQLPlanningService().planTableRule(request, "create").getSql(),
                is("CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                        + "TABLE_STRATEGY(TYPE='hint', SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))), "
                        + "KEY_GENERATE_STRATEGY(COLUMN=`id`, TYPE(NAME='snowflake')))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithNoneStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setStrategyType("none");
        request.setAlgorithmType("");
        assertThat(new ShardingDistSQLPlanningService().planTableRule(request, "create").getSql(),
                is("CREATE SHARDING TABLE RULE `t_order`(DATANODES('ds_${0..1}.t_order_${0..1}'), KEY_GENERATE_STRATEGY(COLUMN=`id`, TYPE(NAME='snowflake')))"));
    }
    
    @Test
    void assertPlanTableRuleCreateWithAutoTable() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setStorageUnits("ds_0, ds_1");
        request.setAlgorithmType("HASH_MOD");
        request.putAlgorithmProperties(Map.of("sharding-count", "4"));
        request.setKeyGenerateColumn("id");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        assertThat(new ShardingDistSQLPlanningService().planTableRule(request, "create").getSql(),
                is("CREATE SHARDING TABLE RULE `t_order`(STORAGE_UNITS(`ds_0`, `ds_1`), SHARDING_COLUMN=`order_id`, TYPE(NAME='hash_mod', PROPERTIES('sharding-count'='4')), "
                        + "KEY_GENERATE_STRATEGY(COLUMN=`id`, TYPE(NAME='snowflake', PROPERTIES('worker-id'='1'))))"));
    }
    
    @Test
    void assertPlanTableRuleDrop() {
        assertThat(new ShardingDistSQLPlanningService().planTableRule(createTableRuleRequest(), "drop").getSql(), is("DROP SHARDING TABLE RULE `t_order`"));
    }
    
    @Test
    void assertPlanTableReferenceRule() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setRuleName("ref_rule");
        request.getReferenceTables().addAll(List.of("t_order", "t_order_item"));
        assertThat(new ShardingDistSQLPlanningService().planTableReferenceRule(request, "create").getSql(),
                is("CREATE SHARDING TABLE REFERENCE RULE `ref_rule`(`t_order`, `t_order_item`)"));
    }
    
    @Test
    void assertPlanDefaultStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        assertThat(new ShardingDistSQLPlanningService().planDefaultStrategy(request, "create").getSql(),
                is("CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='standard', SHARDING_COLUMN=`order_id`, "
                        + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"));
    }
    
    @Test
    void assertPlanDefaultStrategyWithComplexStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("complex");
        request.setShardingColumns("order_id, user_id");
        assertThat(new ShardingDistSQLPlanningService().planDefaultStrategy(request, "create").getSql(),
                is("CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='complex', SHARDING_COLUMNS=`order_id`, `user_id`, "
                        + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"));
    }
    
    @Test
    void assertPlanDefaultStrategyWithHintStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("hint");
        assertThat(new ShardingDistSQLPlanningService().planDefaultStrategy(request, "create").getSql(),
                is("CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='hint', "
                        + "SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"));
    }
    
    @Test
    void assertPlanDefaultStrategyWithNoneStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setColumn("");
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("none");
        request.setAlgorithmType("");
        assertThat(new ShardingDistSQLPlanningService().planDefaultStrategy(request, "create").getSql(), is("CREATE DEFAULT SHARDING DATABASE STRATEGY (TYPE='none')"));
    }
    
    @Test
    void assertPlanKeyGenerator() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGeneratorName("snowflake_generator");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        assertThat(new ShardingDistSQLPlanningService().planKeyGenerator(request, "create").getSql(),
                is("CREATE SHARDING KEY GENERATOR `snowflake_generator`(TYPE(NAME='snowflake', PROPERTIES('worker-id'='1')))"));
    }
    
    @Test
    void assertPlanKeyGenerateStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateStrategyName("order_key_strategy");
        request.setKeyGenerateColumn("");
        assertThat(new ShardingDistSQLPlanningService().planKeyGenerateStrategy(request, "create").getSql(),
                is("CREATE SHARDING KEY GENERATE STRATEGY `order_key_strategy`(TABLE=`t_order`, COLUMN=`order_id`, TYPE(NAME='snowflake'))"));
    }
    
    @Test
    void assertPlanSequenceKeyGenerateStrategy() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGenerateStrategyName("order_sequence_strategy");
        request.setSequenceName("order_seq");
        request.setKeyGeneratorName("snowflake_generator");
        assertThat(new ShardingDistSQLPlanningService().planKeyGenerateStrategy(request, "create").getSql(),
                is("CREATE SHARDING KEY GENERATE STRATEGY `order_sequence_strategy`(SEQUENCE='order_seq', GENERATOR=`snowflake_generator`)"));
    }
    
    @Test
    void assertPlanComponentCleanup() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setComponentType("key_generator");
        request.setComponentName("snowflake_generator");
        assertThat(new ShardingDistSQLPlanningService().planComponentCleanup(request).getSql(), is("DROP SHARDING KEY GENERATOR `snowflake_generator`"));
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setTable("t_order");
        result.setColumn("order_id");
        result.setDataNodes("ds_${0..1}.t_order_${0..1}");
        result.setStrategyType("standard");
        result.setAlgorithmType("INLINE");
        result.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        result.setKeyGenerateColumn("id");
        result.setKeyGeneratorType("SNOWFLAKE");
        return result;
    }
    
    private void assertNoForbiddenArtifacts(final String sql) {
        assertFalse(sql.contains("CREATE TABLE"));
        assertFalse(sql.contains("CREATE INDEX"));
        assertFalse(sql.contains("REGISTER STORAGE UNIT"));
        assertFalse(sql.contains("MIGRATE"));
        assertFalse(sql.contains("BACKFILL"));
    }
}
