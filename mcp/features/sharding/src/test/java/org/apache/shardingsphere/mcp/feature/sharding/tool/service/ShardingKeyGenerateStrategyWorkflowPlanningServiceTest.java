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

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGenerateStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingKeyGenerateStrategyWorkflowPlanningServiceTest {
    
    @Test
    void assertPlan() {
        ShardingWorkflowPlanningService delegate = mock(ShardingWorkflowPlanningService.class);
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        WorkflowContextSnapshot expected = new WorkflowContextSnapshot();
        when(delegate.planKeyGenerateStrategy(eq(workflowSessionContext), eq(queryFacade), eq("session-1"), any())).thenReturn(expected);
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGenerateStrategyName("order_key_strategy");
        WorkflowContextSnapshot actual = new ShardingKeyGenerateStrategyWorkflowPlanningService(delegate)
                .plan(workflowSessionContext, queryFacade, "session-1", new ShardingKeyGenerateStrategyWorkflowRequest(request));
        ArgumentCaptor<ShardingWorkflowRequest> requestCaptor = ArgumentCaptor.forClass(ShardingWorkflowRequest.class);
        verify(delegate).planKeyGenerateStrategy(eq(workflowSessionContext), eq(queryFacade), eq("session-1"), requestCaptor.capture());
        assertThat(actual, is(expected));
        assertThat(requestCaptor.getValue().getKeyGenerateStrategyName(), is("order_key_strategy"));
    }
}
