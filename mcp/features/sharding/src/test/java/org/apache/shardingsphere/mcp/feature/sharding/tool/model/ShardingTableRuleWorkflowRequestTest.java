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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingTableRuleWorkflowRequestTest {
    
    @Test
    void assertToWorkflowRequest() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setAlgorithmType("INLINE");
        request.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        ShardingTableRuleWorkflowRequest tableRuleRequest = new ShardingTableRuleWorkflowRequest(request);
        request.setTable("t_changed");
        ShardingWorkflowRequest actual = tableRuleRequest.toWorkflowRequest();
        actual.setTable("t_actual_changed");
        assertThat(tableRuleRequest.toWorkflowRequest().getTable(), is("t_order"));
        assertThat(tableRuleRequest.toWorkflowRequest().getPrimaryAlgorithmProperties(), is(Map.of("algorithm-expression", "t_order_${order_id % 2}")));
    }
}
