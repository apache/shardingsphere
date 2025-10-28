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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.jdbc;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.plugin.core.context.ShardingSphereDataSourceContext;
import org.apache.shardingsphere.agent.plugin.core.holder.ShardingSphereDataSourceContextHolder;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(AgentReflectionUtils.class)
class ShardingSphereDataSourceAdviceTest {
    
    private final TargetAdviceObjectFixture fixture = new TargetAdviceObjectFixture();
    
    private final String databaseName = "sharding_db";
    
    private final String instanceId = UUID.randomUUID().toString();
    
    @AfterEach
    void clean() {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
    }
    
    @Test
    void assertBeforeMethod() {
        ContextManager contextManager = mockContextManager();
        when(AgentReflectionUtils.getFieldValue(fixture, "contextManager")).thenReturn(contextManager);
        ShardingSphereDataSourceContextHolder.put(instanceId, new ShardingSphereDataSourceContext(databaseName, mock(ContextManager.class, RETURNS_DEEP_STUBS)));
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().size(), is(1));
        TargetAdviceMethod method = mock(TargetAdviceMethod.class);
        when(method.getName()).thenReturn("close");
        ShardingSphereDataSourceAdvice advice = new ShardingSphereDataSourceAdvice();
        advice.beforeMethod(fixture, method, new Object[]{}, "FIXTURE");
        assertTrue(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().isEmpty());
    }
    
    @Test
    void assertAfterMethod() {
        assertTrue(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().isEmpty());
        when(AgentReflectionUtils.getFieldValue(fixture, "databaseName")).thenReturn(databaseName);
        TargetAdviceMethod method = mock(TargetAdviceMethod.class);
        when(method.getName()).thenReturn("createContextManager");
        ShardingSphereDataSourceAdvice advice = new ShardingSphereDataSourceAdvice();
        ContextManager contextManager = mockContextManager();
        advice.afterMethod(fixture, method, new Object[]{}, contextManager, "FIXTURE");
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().size(), is(1));
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().keySet().iterator().next(), is(instanceId));
        assertThat(ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().get(instanceId).getDatabaseName(), is(databaseName));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn(instanceId);
        return result;
    }
}
