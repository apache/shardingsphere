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

package org.apache.shardingsphere.mcp.feature.sharding.tool.model;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingWorkflowRequestTest {
    
    @Test
    void assertCopy() {
        ShardingWorkflowRequest request = createRequest();
        ShardingWorkflowRequest actual = request.copy();
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getShardingColumns(), is("order_id, user_id"));
        assertThat(actual.getRuleName(), is("ref_rule"));
        assertThat(actual.getReferenceTables(), is(List.of("t_order", "t_order_item")));
        assertThat(actual.getAuditorNames(), is(List.of("dml_auditor")));
        assertThat(actual.getKeyGeneratorProperties(), is(Map.of("worker-id", "1")));
    }
    
    @Test
    void assertMerge() {
        ShardingWorkflowRequest current = new ShardingWorkflowRequest();
        current.setTable("t_order_item");
        current.setShardingColumns("order_item_id, user_id");
        current.setAlgorithmType("MOD");
        current.getReferenceTables().add("t_order_item");
        current.putKeyGeneratorProperties(Map.of("worker-id", "2"));
        ShardingWorkflowRequest previous = createRequest();
        ShardingWorkflowRequest actual = ShardingWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTable(), is("t_order_item"));
        assertThat(actual.getShardingColumns(), is("order_item_id, user_id"));
        assertThat(actual.getAlgorithmType(), is("MOD"));
        assertThat(actual.getReferenceTables(), is(List.of("t_order_item")));
        assertThat(actual.getKeyGeneratorProperties(), is(Map.of("worker-id", "2")));
    }
    
    @Test
    void assertMergeWithGenericPrevious() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setPlanId("plan-1");
        previous.setDatabase("logic_db");
        previous.setTable("t_order");
        previous.setOperationType("create");
        ShardingWorkflowRequest current = new ShardingWorkflowRequest();
        current.setRuleName("ref_rule");
        current.setShardingColumns("order_id");
        ShardingWorkflowRequest actual = ShardingWorkflowRequest.merge(previous, current);
        assertThat(actual.getPlanId(), is("plan-1"));
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getOperationType(), is("create"));
        assertThat(actual.getRuleName(), is("ref_rule"));
        assertThat(actual.getShardingColumns(), is("order_id"));
    }
    
    private ShardingWorkflowRequest createRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase(" logic_db ");
        result.setTable(" t_order ");
        result.setColumn("order_id");
        result.setRuleName(" ref_rule ");
        result.setDataNodes("ds_${0..1}.t_order_${0..1}");
        result.setStorageUnits("ds_0,ds_1");
        result.setStrategyType("standard");
        result.setShardingColumns("order_id, user_id");
        result.setDefaultStrategyType("DATABASE");
        result.setKeyGenerateColumn("id");
        result.setKeyGeneratorName("snowflake_generator");
        result.setKeyGeneratorType("SNOWFLAKE");
        result.setSequenceName("order_sequence");
        result.setKeyGenerateStrategyName("order_key_strategy");
        result.setComponentType("algorithm");
        result.setComponentName("unused_algorithm");
        result.setAllowHintDisable("true");
        result.setAlgorithmType("INLINE");
        result.getReferenceTables().addAll(List.of("t_order", "t_order_item"));
        result.getAuditorNames().add("dml_auditor");
        result.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        result.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        return result;
    }
}
