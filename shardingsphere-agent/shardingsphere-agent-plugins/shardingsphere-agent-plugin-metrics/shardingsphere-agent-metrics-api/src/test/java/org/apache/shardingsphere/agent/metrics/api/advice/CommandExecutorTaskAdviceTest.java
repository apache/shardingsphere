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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CommandExecutorTaskAdviceTest extends MetricsAdviceBaseTest {
    
    private final CommandExecutorTaskAdvice commandExecutorTaskAdvice = new CommandExecutorTaskAdvice();
    
    @Mock
    private Method run;
    
    @Mock
    private Method processException;
    
    @Test
    public void assertExecuteLatency() {
        when(run.getName()).thenReturn(CommandExecutorTaskAdvice.COMMAND_EXECUTOR_RUN);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        commandExecutorTaskAdvice.beforeMethod(targetObject, run, new Object[]{}, new MethodInvocationResult());
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }
        commandExecutorTaskAdvice.afterMethod(targetObject, run, new Object[]{}, new MethodInvocationResult());
        FixtureWrapper requestWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).isPresent());
        assertThat(requestWrapper.getFixtureValue(), greaterThan(0.0));
    }
    
    @Test
    public void assertExecuteErrorTotal() {
        when(processException.getName()).thenReturn(CommandExecutorTaskAdvice.COMMAND_EXECUTOR_EXCEPTION);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        commandExecutorTaskAdvice.afterMethod(targetObject, processException, new Object[]{}, new MethodInvocationResult());
        FixtureWrapper requestWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_ERROR).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_EXECUTE_ERROR).isPresent());
        assertThat(requestWrapper.getFixtureValue(), greaterThan(0.0));
    }
}
