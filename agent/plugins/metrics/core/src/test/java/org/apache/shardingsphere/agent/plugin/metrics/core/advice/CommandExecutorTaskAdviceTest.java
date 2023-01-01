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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CommandExecutorTaskAdviceTest extends MetricsAdviceBaseTest {
    
    @Mock
    private Method run;
    
    @Mock
    private Method processException;
    
    @Test
    public void assertExecuteLatency() {
        when(run.getName()).thenReturn(CommandExecutorTaskAdvice.COMMAND_EXECUTOR_RUN);
        CommandExecutorTaskAdvice advice = new CommandExecutorTaskAdvice();
        MockTargetAdviceObject targetObject = new MockTargetAdviceObject();
        advice.beforeMethod(targetObject, run, new Object[]{});
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            ex.printStackTrace();
        }
        advice.afterMethod(targetObject, run, new Object[]{}, null);
        FixtureWrapper requestWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_EXECUTE_LATENCY_MILLIS).isPresent());
        assertThat(requestWrapper.getFixtureValue(), greaterThan(0.0));
    }
    
    @Test
    public void assertExecuteErrorTotal() {
        when(processException.getName()).thenReturn(CommandExecutorTaskAdvice.COMMAND_EXECUTOR_EXCEPTION);
        MockTargetAdviceObject targetObject = new MockTargetAdviceObject();
        new CommandExecutorTaskAdvice().afterMethod(targetObject, processException, new Object[]{}, null);
        FixtureWrapper requestWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PROXY_EXECUTE_ERROR).get();
        assertTrue(MetricsPool.get(MetricIds.PROXY_EXECUTE_ERROR).isPresent());
        assertThat(requestWrapper.getFixtureValue(), greaterThan(0.0));
    }
}
