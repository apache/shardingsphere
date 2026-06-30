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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShardingKeyGenerateStrategyWorkflowRequestTest {
    
    @Test
    void assertToWorkflowRequest() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGenerateStrategyName("order_key_strategy");
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        ShardingKeyGenerateStrategyWorkflowRequest strategyRequest = new ShardingKeyGenerateStrategyWorkflowRequest(request);
        request.setKeyGenerateStrategyName("changed_strategy");
        ShardingWorkflowRequest actual = strategyRequest.toWorkflowRequest();
        actual.setKeyGenerateStrategyName("actual_changed_strategy");
        assertThat(strategyRequest.toWorkflowRequest().getKeyGenerateStrategyName(), is("order_key_strategy"));
        assertThat(strategyRequest.toWorkflowRequest().getKeyGeneratorName(), is("snowflake_generator"));
    }
}
